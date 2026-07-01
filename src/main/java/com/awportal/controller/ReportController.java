package com.awportal.controller;

import com.awportal.dto.*;
import com.awportal.model.*;
import com.awportal.repository.AccountRepository;
import com.awportal.repository.ClientRepository;
import com.awportal.repository.LiabilityRepository;
import com.awportal.repository.QuarterlyReportRepository;
import com.awportal.service.CalculationService;
import com.awportal.service.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ReportController {

    private final ClientRepository clientRepository;
    private final QuarterlyReportRepository reportRepository;
    private final AccountRepository accountRepository;
    private final LiabilityRepository liabilityRepository;
    private final CalculationService calculationService;
    private final PdfService pdfService;

    public ReportController(ClientRepository clientRepository, QuarterlyReportRepository reportRepository,
                             AccountRepository accountRepository, LiabilityRepository liabilityRepository,
                             CalculationService calculationService, PdfService pdfService) {
        this.clientRepository = clientRepository;
        this.reportRepository = reportRepository;
        this.accountRepository = accountRepository;
        this.liabilityRepository = liabilityRepository;
        this.calculationService = calculationService;
        this.pdfService = pdfService;
    }

    @PostMapping("/clients/{clientId}/reports")
    public ReportResponse createReport(@PathVariable Long clientId, @RequestBody ReportRequest req) {
        Client client = clientRepository.findById(clientId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));

        QuarterlyReport report = new QuarterlyReport();
        report.setClient(client);
        report.setQuarter(req.quarter());
        report.setYear(req.year());
        report.setAsOfDate(LocalDate.now());
        report.setSalarySnapshot(client.getMonthlySalary());
        report.setExpenseBudgetSnapshot(client.getMonthlyExpenseBudget());
        report.setInsuranceDeductiblesSnapshot(client.getInsuranceDeductiblesTotal());
        report.setTrustPropertyValue(req.trustPropertyValue());
        report = reportRepository.save(report);

        if (req.accountBalances() != null) {
            for (BalanceEntry entry : req.accountBalances()) {
                Account account = accountRepository.findById(entry.accountId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found: " + entry.accountId()));
                AccountBalance ab = new AccountBalance();
                ab.setReport(report);
                ab.setAccount(account);
                ab.setBalance(entry.balance());
                ab.setCashBalance(entry.cashBalance());
                report.getAccountBalances().add(ab);
            }
        }
        if (req.liabilityBalances() != null) {
            for (LiabilityBalanceEntry entry : req.liabilityBalances()) {
                Liability liability = liabilityRepository.findById(entry.liabilityId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liability not found: " + entry.liabilityId()));
                LiabilityBalance lb = new LiabilityBalance();
                lb.setReport(report);
                lb.setLiability(liability);
                lb.setBalance(entry.balance());
                report.getLiabilityBalances().add(lb);
            }
        }

        report = reportRepository.save(report);
        return toReportResponse(report);
    }

    @GetMapping("/clients/{clientId}/reports")
    public List<ReportResponse> listReports(@PathVariable Long clientId) {
        return reportRepository.findByClientId(clientId).stream().map(this::toReportResponse).collect(Collectors.toList());
    }

    @GetMapping("/reports/{reportId}/calculate")
    public CalculatedReport calculateReport(@PathVariable Long reportId) {
        return calculationService.calculate(findReportOr404(reportId));
    }

    @GetMapping(value = "/reports/{reportId}/pdf/sacs", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> sacsPdf(@PathVariable Long reportId) {
        QuarterlyReport report = findReportOr404(reportId);
        CalculatedReport calc = calculationService.calculate(report);
        byte[] pdf = pdfService.generateSacsPdf(report.getClient(), report, calc);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=SACS_Q" + report.getQuarter() + "_" + report.getYear() + ".pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }

    @GetMapping(value = "/reports/{reportId}/pdf/tcc", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> tccPdf(@PathVariable Long reportId) {
        QuarterlyReport report = findReportOr404(reportId);
        CalculatedReport calc = calculationService.calculate(report);
        byte[] pdf = pdfService.generateTccPdf(report.getClient(), report, calc);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=TCC_Q" + report.getQuarter() + "_" + report.getYear() + ".pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }

    @GetMapping("/health")
    public java.util.Map<String, String> health() {
        return java.util.Map.of("status", "ok");
    }

    private QuarterlyReport findReportOr404(Long id) {
        return reportRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));
    }

    private ReportResponse toReportResponse(QuarterlyReport r) {
        return new ReportResponse(
            r.getId(), r.getClient().getId(), r.getQuarter(), r.getYear(), r.getAsOfDate(),
            r.getSalarySnapshot(), r.getExpenseBudgetSnapshot(), r.getInsuranceDeductiblesSnapshot(), r.getTrustPropertyValue()
        );
    }
}
