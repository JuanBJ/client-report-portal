package com.awportal.service;

import com.awportal.dto.CalculatedReport;
import com.awportal.model.*;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Renders the SACS (cashflow) and TCC (net worth circle chart) PDFs.
 * Takes a CalculatedReport (never re-computes anything) plus the raw
 * client/account data needed for labels and per-account bubbles.
 *
 * This is a first-pass visual approximation of the reference templates -
 * getting it pixel-identical to the Canva originals is a follow-up polish
 * pass, not an architecture change (see README).
 */
@Service
public class PdfService {

    private static final Color GREEN = new Color(0x2f, 0x9e, 0x44);
    private static final Color RED = new Color(0xe0, 0x31, 0x31);
    private static final Color BLUE = new Color(0x19, 0x71, 0xc2);
    private static final Color GRAY = new Color(0x49, 0x50, 0x57);
    private static final Color LIGHT_GRAY = new Color(0xe9, 0xec, 0xef);
    private static final Color TRUST_GRAY = new Color(0xf1, 0xf3, 0xf5);
    private static final Color WHITE = Color.WHITE;
    private static final Color BLACK = Color.BLACK;

    public byte[] generateSacsPdf(Client client, QuarterlyReport report, CalculatedReport calc) {
        Document document = new Document(PageSize.LETTER);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();
            PdfContentByte cb = writer.getDirectContent();

            float width = PageSize.LETTER.getWidth();
            float height = PageSize.LETTER.getHeight();

            drawHeader(cb, client, report, "SACS - Simple Automated Cash Flow", width, height);

            float cx = width / 2;
            float topY = height - 180;

            drawCircle(cb, cx, topY, 55, GREEN);
            text(cb, "INFLOW", cx, topY + 8, WHITE, 10, true, PdfContentByte.ALIGN_CENTER);
            text(cb, money(calc.inflow()), cx, topY - 8, WHITE, 10, true, PdfContentByte.ALIGN_CENTER);

            cb.setColorStroke(GRAY);
            cb.moveTo(cx, topY - 60);
            cb.lineTo(cx, topY - 110);
            cb.stroke();

            float midY = topY - 165;
            drawCircle(cb, cx, midY, 55, RED);
            text(cb, "OUTFLOW", cx, midY + 8, WHITE, 10, true, PdfContentByte.ALIGN_CENTER);
            text(cb, money(calc.outflow()), cx, midY - 8, WHITE, 10, true, PdfContentByte.ALIGN_CENTER);

            cb.setColorStroke(GRAY);
            cb.moveTo(cx, midY - 60);
            cb.lineTo(cx, midY - 110);
            cb.stroke();

            float botY = midY - 165;
            drawCircle(cb, cx, botY, 55, BLUE);
            text(cb, "PRIVATE RESERVE", cx, botY + 8, WHITE, 10, true, PdfContentByte.ALIGN_CENTER);
            text(cb, "excess " + money(calc.excessToReserve()) + "/mo", cx, botY - 8, WHITE, 9, true, PdfContentByte.ALIGN_CENTER);

            text(cb, "Automated transfer = Inflow - Outflow", cx, botY - 80, GRAY, 9, false, PdfContentByte.ALIGN_CENTER);

            document.newPage();

            drawHeader(cb, client, report, "SACS - Private Reserve Status", width, height);
            float y = height - 160;
            String[] labels = {
                "Private Reserve Balance",
                "Investment Account (Schwab)",
                "Target (6 mo expenses + deductibles)"
            };
            double[] vals = {calc.privateReserveBalance(), calc.investmentBalance(), calc.reserveTarget()};
            for (int i = 0; i < labels.length; i++) {
                text(cb, labels[i], 60, y, GRAY, 11, false, PdfContentByte.ALIGN_LEFT);
                text(cb, money(vals[i]), width - 60, y, BLACK, 11, false, PdfContentByte.ALIGN_RIGHT);
                y -= 30;
            }

            String status = calc.reserveFullyFunded() ? "FULLY FUNDED" : "BELOW TARGET";
            Color statusColor = calc.reserveFullyFunded() ? GREEN : RED;
            text(cb, "Status: " + status, 60, y - 10, statusColor, 12, true, PdfContentByte.ALIGN_LEFT);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate SACS PDF", e);
        }
        return baos.toByteArray();
    }

    public byte[] generateTccPdf(Client client, QuarterlyReport report, CalculatedReport calc) {
        Document document = new Document(PageSize.LETTER);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();
            PdfContentByte cb = writer.getDirectContent();

            float width = PageSize.LETTER.getWidth();
            float height = PageSize.LETTER.getHeight();

            drawHeader(cb, client, report, "TCC - Total Client Chart", width, height);

            Map<Long, AccountBalance> balancesByAccount = new HashMap<>();
            for (AccountBalance b : report.getAccountBalances()) {
                balancesByAccount.put(b.getAccount().getId(), b);
            }

            drawRect(cb, width / 2 - 70, height - 150, 140, 34, GRAY);
            text(cb, "GRAND TOTAL", width / 2, height - 128, WHITE, 9, true, PdfContentByte.ALIGN_CENTER);
            text(cb, money(calc.grandTotalNetWorth()), width / 2, height - 142, WHITE, 9, true, PdfContentByte.ALIGN_CENTER);

            List<Account> client1Ret = client.getAccounts().stream()
                .filter(a -> a.getBucket() == AccountBucket.RETIREMENT && a.getOwner() == Owner.CLIENT_1)
                .collect(Collectors.toList());
            List<Account> client2Ret = client.getAccounts().stream()
                .filter(a -> a.getBucket() == AccountBucket.RETIREMENT && a.getOwner() == Owner.CLIENT_2)
                .collect(Collectors.toList());
            List<Account> nonRet = client.getAccounts().stream()
                .filter(a -> a.getBucket() == AccountBucket.NON_RETIREMENT)
                .collect(Collectors.toList());

            if (!client1Ret.isEmpty()) {
                drawAccountBubbles(cb, client1Ret, balancesByAccount, width / 4, height - 260, 32, 95);
            }
            drawRect(cb, 40, height - 320, 130, 26, GRAY);
            text(cb, "C1 Retirement: " + money(calc.client1RetirementTotal()), 105, height - 305, WHITE, 8, true, PdfContentByte.ALIGN_CENTER);

            if (!client2Ret.isEmpty()) {
                drawAccountBubbles(cb, client2Ret, balancesByAccount, 3 * width / 4, height - 260, 32, 95);
            }
            drawRect(cb, width - 170, height - 320, 130, 26, GRAY);
            text(cb, "C2 Retirement: " + money(calc.client2RetirementTotal()), width - 105, height - 305, WHITE, 8, true, PdfContentByte.ALIGN_CENTER);

            drawBubble(cb, width / 2, height - 380, 45, TRUST_GRAY);
            text(cb, "TRUST", width / 2, height - 376, BLACK, 9, true, PdfContentByte.ALIGN_CENTER);
            text(cb, money(calc.trustValue()), width / 2, height - 390, BLACK, 9, true, PdfContentByte.ALIGN_CENTER);

            if (!nonRet.isEmpty()) {
                drawAccountBubbles(cb, nonRet, balancesByAccount, width / 2, height - 460, 32, 110);
            }
            drawRect(cb, width / 2 - 90, height - 500, 180, 24, GRAY);
            text(cb, "Non-Retirement Total: " + money(calc.nonRetirementTotal()), width / 2, height - 486, WHITE, 8, true, PdfContentByte.ALIGN_CENTER);

            float y = height - 560;
            text(cb, "Liabilities", 50, y, BLACK, 10, true, PdfContentByte.ALIGN_LEFT);
            y -= 18;

            Map<Long, LiabilityBalance> liabilityBalancesById = new HashMap<>();
            for (LiabilityBalance lb : report.getLiabilityBalances()) {
                liabilityBalancesById.put(lb.getLiability().getId(), lb);
            }
            for (Liability liability : client.getLiabilities()) {
                LiabilityBalance lbRow = liabilityBalancesById.get(liability.getId());
                double bal = lbRow != null ? lbRow.getBalance() : 0.0;
                text(cb, liability.getLiabilityType() + " (" + liability.getInterestRate() + "%): " + money(bal),
                    60, y, BLACK, 9, false, PdfContentByte.ALIGN_LEFT);
                y -= 14;
            }
            text(cb, "Total Liabilities: " + money(calc.liabilitiesTotal()), 60, y - 4, BLACK, 9, true, PdfContentByte.ALIGN_LEFT);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate TCC PDF", e);
        }
        return baos.toByteArray();
    }

    private void drawAccountBubbles(PdfContentByte cb, List<Account> accounts, Map<Long, AccountBalance> balancesByAccount,
                                     float centerX, float centerY, float radius, float spread) {
        int n = accounts.size();
        for (int i = 0; i < n; i++) {
            Account acc = accounts.get(i);
            float offset = (i - (n - 1) / 2.0f) * spread;
            float bx = centerX + offset;
            float by = centerY;
            AccountBalance balRow = balancesByAccount.get(acc.getId());
            double bal = balRow != null ? balRow.getBalance() : 0.0;
            drawBubble(cb, bx, by, radius, LIGHT_GRAY);
            String label = acc.getAccountType().length() > 14 ? acc.getAccountType().substring(0, 14) : acc.getAccountType();
            text(cb, label, bx, by + 6, BLACK, 7, true, PdfContentByte.ALIGN_CENTER);
            text(cb, money(bal), bx, by - 6, BLACK, 7, false, PdfContentByte.ALIGN_CENTER);
        }
    }

    private void drawHeader(PdfContentByte cb, Client client, QuarterlyReport report, String title, float width, float height) {
        text(cb, title, width / 2, height - 50, BLACK, 16, true, PdfContentByte.ALIGN_CENTER);
        String name = client.getClient1Name();
        if (client.getClient2Name() != null && !client.getClient2Name().isBlank()) {
            name += " & " + client.getClient2Name();
        }
        text(cb, "Client: " + name, 50, height - 75, BLACK, 10, false, PdfContentByte.ALIGN_LEFT);
        text(cb, "Date: " + report.getAsOfDate() + "  |  Q" + report.getQuarter() + " " + report.getYear(),
            50, height - 90, BLACK, 10, false, PdfContentByte.ALIGN_LEFT);
    }

    private void drawCircle(PdfContentByte cb, float cx, float cy, float r, Color fill) {
        cb.setColorFill(fill);
        cb.ellipse(cx - r, cy - r, cx + r, cy + r);
        cb.fill();
    }

    private void drawBubble(PdfContentByte cb, float cx, float cy, float r, Color fill) {
        cb.setColorFill(fill);
        cb.setColorStroke(GRAY);
        cb.setLineWidth(0.5f);
        cb.ellipse(cx - r, cy - r, cx + r, cy + r);
        cb.fillStroke();
    }

    private void drawRect(PdfContentByte cb, float llx, float lly, float w, float h, Color fill) {
        cb.setColorFill(fill);
        cb.rectangle(llx, lly, w, h);
        cb.fill();
    }

    private void text(PdfContentByte cb, String value, float x, float y, Color color, float size, boolean bold, int align) {
        try {
            BaseFont bf = BaseFont.createFont(bold ? BaseFont.HELVETICA_BOLD : BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            cb.beginText();
            cb.setFontAndSize(bf, size);
            cb.setColorFill(color);
            cb.showTextAligned(align, value, x, y, 0);
            cb.endText();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String money(double v) {
        return String.format("$%,.2f", v);
    }
}
