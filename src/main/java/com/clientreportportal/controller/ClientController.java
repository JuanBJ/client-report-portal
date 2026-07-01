package com.clientreportportal.controller;

import com.clientreportportal.dto.*;
import com.clientreportportal.model.*;
import com.clientreportportal.repository.AccountRepository;
import com.clientreportportal.repository.ClientRepository;
import com.clientreportportal.repository.LiabilityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final LiabilityRepository liabilityRepository;

    public ClientController(ClientRepository clientRepository, AccountRepository accountRepository, LiabilityRepository liabilityRepository) {
        this.clientRepository = clientRepository;
        this.accountRepository = accountRepository;
        this.liabilityRepository = liabilityRepository;
    }

    @GetMapping
    public List<ClientResponse> listClients() {
        return clientRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @PostMapping
    public ClientResponse createClient(@RequestBody ClientRequest req) {
        Client client = new Client();
        applyRequest(client, req);
        client = clientRepository.save(client);
        return toResponse(client);
    }

    @GetMapping("/{id}")
    public ClientResponse getClient(@PathVariable Long id) {
        return toResponse(findClientOr404(id));
    }

    @PutMapping("/{id}")
    public ClientResponse updateClient(@PathVariable Long id, @RequestBody ClientRequest req) {
        Client client = findClientOr404(id);
        applyRequest(client, req);
        client = clientRepository.save(client);
        return toResponse(client);
    }

    @PostMapping("/{clientId}/accounts")
    public AccountResponse addAccount(@PathVariable Long clientId, @RequestBody AccountRequest req) {
        Client client = findClientOr404(clientId);
        Account account = new Account();
        account.setClient(client);
        account.setBucket(AccountBucket.valueOf(req.bucket()));
        account.setOwner(Owner.valueOf(req.owner()));
        account.setAccountType(req.accountType());
        account.setAccountNumberLast4(req.accountNumberLast4());
        account.setPrivateReserve(req.isPrivateReserve());
        account.setInvestmentForTarget(req.isInvestmentForTarget());
        account = accountRepository.save(account);
        return toAccountResponse(account);
    }

    @PostMapping("/{clientId}/liabilities")
    public LiabilityResponse addLiability(@PathVariable Long clientId, @RequestBody LiabilityRequest req) {
        Client client = findClientOr404(clientId);
        Liability liability = new Liability();
        liability.setClient(client);
        liability.setLiabilityType(req.liabilityType());
        liability.setInterestRate(req.interestRate());
        liability = liabilityRepository.save(liability);
        return toLiabilityResponse(liability);
    }

    private Client findClientOr404(Long id) {
        return clientRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));
    }

    private void applyRequest(Client client, ClientRequest req) {
        client.setClient1Name(req.client1Name());
        client.setClient1Dob(req.client1Dob());
        client.setClient1SsnLast4(req.client1SsnLast4());
        client.setClient2Name(req.client2Name());
        client.setClient2Dob(req.client2Dob());
        client.setClient2SsnLast4(req.client2SsnLast4());
        client.setMonthlySalary(req.monthlySalary());
        client.setMonthlyExpenseBudget(req.monthlyExpenseBudget());
        client.setInsuranceDeductiblesTotal(req.insuranceDeductiblesTotal());
    }

    private ClientResponse toResponse(Client c) {
        List<AccountResponse> accounts = c.getAccounts().stream().map(this::toAccountResponse).collect(Collectors.toList());
        List<LiabilityResponse> liabilities = c.getLiabilities().stream().map(this::toLiabilityResponse).collect(Collectors.toList());
        return new ClientResponse(
            c.getId(), c.getClient1Name(), c.getClient1Dob(), c.getClient1SsnLast4(),
            c.getClient2Name(), c.getClient2Dob(), c.getClient2SsnLast4(),
            c.getMonthlySalary(), c.getMonthlyExpenseBudget(), c.getInsuranceDeductiblesTotal(),
            accounts, liabilities
        );
    }

    private AccountResponse toAccountResponse(Account a) {
        return new AccountResponse(
            a.getId(), a.getBucket().name(), a.getOwner().name(), a.getAccountType(),
            a.getAccountNumberLast4(), a.isPrivateReserve(), a.isInvestmentForTarget()
        );
    }

    private LiabilityResponse toLiabilityResponse(Liability l) {
        return new LiabilityResponse(l.getId(), l.getLiabilityType(), l.getInterestRate());
    }
}
