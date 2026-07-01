package com.clientreportportal.service;

import com.clientreportportal.dto.CalculatedReport;
import com.clientreportportal.model.*;
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
    private static final Color DARK = new Color(0x21, 0x25, 0x29);
    private static final Color LIGHT_GRAY = new Color(0xe9, 0xec, 0xef);
    private static final Color PANEL_GRAY = new Color(0xf8, 0xf9, 0xfa);
    private static final Color LINE_GRAY = new Color(0xde, 0xe2, 0xe6);
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

            List<Account> client1Ret = client.getAccounts().stream()
                .filter(a -> a.getBucket() == AccountBucket.RETIREMENT && a.getOwner() == Owner.CLIENT_1)
                .collect(Collectors.toList());
            List<Account> client2Ret = client.getAccounts().stream()
                .filter(a -> a.getBucket() == AccountBucket.RETIREMENT && a.getOwner() == Owner.CLIENT_2)
                .collect(Collectors.toList());
            List<Account> nonRet = client.getAccounts().stream()
                .filter(a -> a.getBucket() == AccountBucket.NON_RETIREMENT)
                .collect(Collectors.toList());

            // ── Client info bubbles ──────────────────────────────────────────
            float infoR = 38;
            float infoY = height - 140;
            drawInfoBubble(cb, client.getClient1Name(), client.getClient1Dob(),
                client.getClient1SsnLast4(), width / 2 - 120, infoY, infoR, GREEN);
            drawInfoBubble(cb, client.getClient2Name(), client.getClient2Dob(),
                client.getClient2SsnLast4(), width / 2 + 120, infoY, infoR, GREEN);

            // ── Retirement rows (top) ────────────────────────────────────────
            float retY = height - 255;
            drawRetirementSection(cb, "Client 1 Retirement", money(calc.client1RetirementTotal()),
                client1Ret, balancesByAccount, width / 4, retY, BLUE);
            drawRetirementSection(cb, "Client 2 Retirement", money(calc.client2RetirementTotal()),
                client2Ret, balancesByAccount, 3 * width / 4, retY, GREEN);

            // ── Trust bubble (center) ────────────────────────────────────────
            float trustR = 50;
            float trustY = height - 390;
            drawBubble(cb, width / 2, trustY, trustR, TRUST_GRAY);
            text(cb, "TRUST", width / 2, trustY + 14, GRAY, 9, true, PdfContentByte.ALIGN_CENTER);
            text(cb, money(calc.trustValue()), width / 2, trustY - 1, DARK, 10, true, PdfContentByte.ALIGN_CENTER);
            text(cb, "Home / Zillow", width / 2, trustY - 15, GRAY, 7, false, PdfContentByte.ALIGN_CENTER);

            // ── Summary boxes — retirement totals ────────────────────────────
            drawSummaryBox(cb, "Client 1 Retirement Total", money(calc.client1RetirementTotal()),
                50, height - 470, 220, 36);
            drawSummaryBox(cb, "Client 2 Retirement Total", money(calc.client2RetirementTotal()),
                width - 270, height - 470, 220, 36);

            // ── Non-retirement bubbles (bottom) ──────────────────────────────
            float nonRetY = height - 545;
            text(cb, "Non-Retirement", width / 2, nonRetY + 20, GRAY, 9, true, PdfContentByte.ALIGN_CENTER);
            drawAccountBubblesRow(cb, nonRet, balancesByAccount, width / 2, nonRetY - 14, 38, 95);

            // ── Summary boxes — non-ret + grand total ────────────────────────
            drawSummaryBox(cb, "Non-Retirement Total", money(calc.nonRetirementTotal()),
                50, height - 638, 220, 36);
            drawSummaryBox(cb, "Grand Total Net Worth", money(calc.grandTotalNetWorth()),
                width - 270, height - 638, 220, 36);

            // ── Liabilities (separate section, bottom) ───────────────────────
            Map<Long, LiabilityBalance> liabilityBalancesById = new HashMap<>();
            for (LiabilityBalance lb : report.getLiabilityBalances()) {
                liabilityBalancesById.put(lb.getLiability().getId(), lb);
            }
            float liabBoxY = height - 700;
            drawRoundRect(cb, 50, liabBoxY - 30, width - 100, 80, WHITE, LINE_GRAY, 8);
            text(cb, "Liabilities  (tracked separately — not subtracted from net worth)",
                width / 2, liabBoxY + 36, GRAY, 8, false, PdfContentByte.ALIGN_CENTER);
            text(cb, money(calc.liabilitiesTotal()), width / 2, liabBoxY + 20, RED, 12, true, PdfContentByte.ALIGN_CENTER);

            float lx = 70;
            float ly = liabBoxY - 4;
            for (Liability liability : client.getLiabilities()) {
                LiabilityBalance lbRow = liabilityBalancesById.get(liability.getId());
                double bal = lbRow != null ? lbRow.getBalance() : 0.0;
                String label = safeLabel(liability.getLiabilityType(), "Liability")
                    + "  " + liability.getInterestRate() + "%";
                text(cb, label, lx, ly, GRAY, 8, false, PdfContentByte.ALIGN_LEFT);
                text(cb, money(bal), lx + 160, ly, BLACK, 8, false, PdfContentByte.ALIGN_LEFT);
                lx += 200;
                if (lx > width - 100) break;
            }

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate TCC PDF", e);
        }
        return baos.toByteArray();
    }

    private void drawInfoBubble(PdfContentByte cb, String name, java.time.LocalDate dob,
                                String ssnLast4, float cx, float cy, float r, Color color) {
        if (name == null || name.isBlank()) return;
        Color tint = new Color(color.getRed(), color.getGreen(), color.getBlue(), 40);
        drawBubble(cb, cx, cy, r, tint);
        cb.setColorStroke(color);
        cb.setLineWidth(1.5f);
        cb.ellipse(cx - r, cy - r, cx + r, cy + r);
        cb.stroke();
        text(cb, name, cx, cy + 18, DARK, 8, true, PdfContentByte.ALIGN_CENTER);
        if (dob != null) {
            text(cb, "DOB: " + dob, cx, cy + 4, GRAY, 7, false, PdfContentByte.ALIGN_CENTER);
        }
        if (ssnLast4 != null && !ssnLast4.isBlank()) {
            text(cb, "SSN: xxx-" + ssnLast4, cx, cy - 10, GRAY, 7, false, PdfContentByte.ALIGN_CENTER);
        }
    }

    private void drawRetirementSection(PdfContentByte cb, String label, String total,
                                       List<Account> accounts, Map<Long, AccountBalance> balByAcc,
                                       float cx, float cy, Color color) {
        text(cb, label, cx, cy + 16, color, 9, true, PdfContentByte.ALIGN_CENTER);
        text(cb, total, cx, cy + 3, DARK, 9, true, PdfContentByte.ALIGN_CENTER);
        // leave 12pt gap then draw bubbles below
        drawAccountBubblesRow(cb, accounts, balByAcc, cx, cy - 50, 34, 82);
    }

    private void drawAccountBubblesRow(PdfContentByte cb, List<Account> accounts,
                                        Map<Long, AccountBalance> balByAcc,
                                        float cx, float cy, float r, float spacing) {
        int n = accounts.size();
        if (n == 0) {
            drawBubble(cb, cx, cy, r, LIGHT_GRAY);
            text(cb, "—", cx, cy - 4, GRAY, 8, false, PdfContentByte.ALIGN_CENTER);
            return;
        }
        float totalWidth = (n - 1) * spacing;
        float startX = cx - totalWidth / 2f;
        for (int i = 0; i < n; i++) {
            Account acc = accounts.get(i);
            float bx = startX + i * spacing;
            AccountBalance balRow = balByAcc.get(acc.getId());
            double bal = balRow != null ? balRow.getBalance() : 0.0;
            drawBubble(cb, bx, cy, r, LIGHT_GRAY);
            String lbl = truncate(safeLabel(acc.getAccountType(), "Acct"), 10);
            text(cb, lbl, bx, cy + 8, DARK, 6, true, PdfContentByte.ALIGN_CENTER);
            if (acc.getAccountNumberLast4() != null && !acc.getAccountNumberLast4().isBlank()) {
                text(cb, "..." + acc.getAccountNumberLast4(), bx, cy - 2, GRAY, 6, false, PdfContentByte.ALIGN_CENTER);
            }
            text(cb, money(bal), bx, cy - 13, BLACK, 6, false, PdfContentByte.ALIGN_CENTER);
        }
    }

    private void drawSummaryBox(PdfContentByte cb, String label, String value,
                                float x, float y, float w, float h) {
        drawRoundRect(cb, x, y, w, h, LIGHT_GRAY, LINE_GRAY, 6);
        text(cb, label, x + 10, y + h - 14, GRAY, 7, false, PdfContentByte.ALIGN_LEFT);
        text(cb, value, x + w - 10, y + 10, DARK, 10, true, PdfContentByte.ALIGN_RIGHT);
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

    private void drawRoundRect(PdfContentByte cb, float llx, float lly, float w, float h, Color fill, Color stroke, float radius) {
        cb.setColorFill(fill);
        cb.setColorStroke(stroke);
        cb.setLineWidth(0.8f);
        cb.roundRectangle(llx, lly, w, h, radius);
        cb.fillStroke();
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

    private String safeLabel(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, Math.max(0, max - 1)) + ".";
    }
}
