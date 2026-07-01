package com.clientreportportal.service;

import com.clientreportportal.dto.CalculatedReport;
import com.clientreportportal.model.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * All financial math lives here and nowhere else. The PDF layer and the API
 * layer only ever read the result of calculate(); they never re-derive numbers.
 *
 * Business rules taken directly from the transcript / PRD:
 *  - Excess = Inflow - Outflow
 *  - Reserve Target = 6 months of expenses + all insurance deductibles
 *  - Liabilities are shown separately and are NEVER subtracted from net worth
 *  - Trust is NOT included in the non-retirement total
 */
@Service
public class CalculationService {

    public CalculatedReport calculate(QuarterlyReport report) {
        Client client = report.getClient();

        Map<Long, AccountBalance> balancesByAccount = new HashMap<>();
        for (AccountBalance b : report.getAccountBalances()) {
            balancesByAccount.put(b.getAccount().getId(), b);
        }
        Map<Long, LiabilityBalance> liabilityBalancesById = new HashMap<>();
        for (LiabilityBalance lb : report.getLiabilityBalances()) {
            liabilityBalancesById.put(lb.getLiability().getId(), lb);
        }

        double inflow = report.getSalarySnapshot();
        double outflow = report.getExpenseBudgetSnapshot();
        double excessToReserve = inflow - outflow;

        double privateReserveBalance = 0.0;
        double investmentBalance = 0.0;
        double client1RetirementTotal = 0.0;
        double client2RetirementTotal = 0.0;
        double nonRetirementTotal = 0.0;

        for (Account account : client.getAccounts()) {
            AccountBalance balRow = balancesByAccount.get(account.getId());
            double balance = balRow != null ? balRow.getBalance() : 0.0;

            if (account.isPrivateReserve()) {
                privateReserveBalance += balance;
            }
            if (account.isInvestmentForTarget()) {
                investmentBalance += balance;
            }

            if (account.getBucket() == AccountBucket.RETIREMENT) {
                if (account.getOwner() == Owner.CLIENT_1) {
                    client1RetirementTotal += balance;
                } else if (account.getOwner() == Owner.CLIENT_2) {
                    client2RetirementTotal += balance;
                } else {
                    // joint retirement account (rare) - split isn't defined by
                    // the PRD, so we count it toward client 1.
                    client1RetirementTotal += balance;
                }
            } else if (account.getBucket() == AccountBucket.NON_RETIREMENT) {
                nonRetirementTotal += balance;
            }
            // TRUST accounts are not summed here - trustValue comes from the
            // report's trustPropertyValue field (Zillow entry), per PRD.
        }

        double trustValue = report.getTrustPropertyValue() != null ? report.getTrustPropertyValue() : 0.0;

        double grandTotalNetWorth = client1RetirementTotal + client2RetirementTotal + nonRetirementTotal + trustValue;

        double liabilitiesTotal = 0.0;
        for (Liability liability : client.getLiabilities()) {
            LiabilityBalance lbRow = liabilityBalancesById.get(liability.getId());
            liabilitiesTotal += lbRow != null ? lbRow.getBalance() : 0.0;
        }

        double reserveTarget = (6 * outflow) + report.getInsuranceDeductiblesSnapshot();
        boolean reserveFullyFunded = privateReserveBalance >= reserveTarget;

        return new CalculatedReport(
            report.getId(),
            client.getId(),
            report.getQuarter(),
            report.getYear(),
            inflow,
            outflow,
            excessToReserve,
            privateReserveBalance,
            investmentBalance,
            reserveTarget,
            reserveFullyFunded,
            client1RetirementTotal,
            client2RetirementTotal,
            nonRetirementTotal,
            trustValue,
            grandTotalNetWorth,
            liabilitiesTotal
        );
    }
}
