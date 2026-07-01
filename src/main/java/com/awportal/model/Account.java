package com.awportal.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @Enumerated(EnumType.STRING)
    private AccountBucket bucket;

    @Enumerated(EnumType.STRING)
    private Owner owner;

    private String accountType;
    private String accountNumberLast4;

    private boolean privateReserve;
    private boolean investmentForTarget;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AccountBalance> balances = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public AccountBucket getBucket() { return bucket; }
    public void setBucket(AccountBucket bucket) { this.bucket = bucket; }

    public Owner getOwner() { return owner; }
    public void setOwner(Owner owner) { this.owner = owner; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String v) { this.accountType = v; }

    public String getAccountNumberLast4() { return accountNumberLast4; }
    public void setAccountNumberLast4(String v) { this.accountNumberLast4 = v; }

    public boolean isPrivateReserve() { return privateReserve; }
    public void setPrivateReserve(boolean v) { this.privateReserve = v; }

    public boolean isInvestmentForTarget() { return investmentForTarget; }
    public void setInvestmentForTarget(boolean v) { this.investmentForTarget = v; }

    public List<AccountBalance> getBalances() { return balances; }
    public void setBalances(List<AccountBalance> v) { this.balances = v; }
}
