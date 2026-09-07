package claimshield;

import java.util.List;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 *
 * Dedicated reporting and analytics engine for administrative managerial summaries,
 * including timeframe financial totals (payouts and co-pays), officer productivity,
 * and membership tier financial breakdowns.
 */
public class FinancialReportService {

    private final AppContext context;

    public FinancialReportService(AppContext context) {
        this.context = context;
    }

    /**
     * Prints an overall breakdown of claims volume by status.
     */
    public void printOverallClaimsVolumeReport() {
        System.out.println("\n===== OVERALL CLAIMS VOLUME & BREAKDOWN =====");
        double totalApproved = 0.0;
        double totalProcessing = 0.0;
        double totalNew = 0.0;
        int countApproved = 0;
        int countProcessing = 0;
        int countNew = 0;

        for (Claim c : context.getClaimRepository().getAll()) {
            if (c.getStatus() == ClaimStatus.DONE) {
                totalApproved += c.getClaimAmount();
                countApproved++;
            } else if (c.getStatus() == ClaimStatus.PROCESSING) {
                totalProcessing += c.getClaimAmount();
                countProcessing++;
            } else if (c.getStatus() == ClaimStatus.NEW) {
                totalNew += c.getClaimAmount();
                countNew++;
            }
        }

        System.out.println("Total Approved Claims (DONE)       : " + countApproved + " claim(s) | " + String.format("%,.2f VND", totalApproved));
        System.out.println("Total Processing Claims            : " + countProcessing + " claim(s) | " + String.format("%,.2f VND", totalProcessing));
        System.out.println("Total Pending Claims (NEW)         : " + countNew + " claim(s) | " + String.format("%,.2f VND", totalNew));
        System.out.println("Cumulative Claim Volume            : " + (countApproved + countProcessing + countNew) + " claim(s) | "
                + String.format("%,.2f VND", (totalApproved + totalProcessing + totalNew)));
    }

    /**
     * Prints claims officer financial productivity report: payouts and co-pays per officer.
     */
    public void printPayoutPerOfficerReport() {
        System.out.println("\n===== TOTAL CLAIM PAYOUTS & CO-PAYS PROCESSED PER CLAIMS OFFICER & STAFF =====");
        List<User> allUsers = context.getUserRepository().getAll();

        for (User u : allUsers) {
            if (u.getRole() == UserRole.OFFICER || u.getRole() == UserRole.ADMIN) {
                List<Claim> staffClaims = context.getClaimRepository().getApprovedClaimsByOfficer(u.getUserId());
                if (!staffClaims.isEmpty()) {
                    double staffPayout = context.getClaimRepository().getApprovedPayoutByOfficer(u.getUserId());
                    double staffCopay = context.getClaimRepository().getApprovedCopayByOfficer(u.getUserId());
                    double staffBilled = staffPayout + staffCopay;
                    String roleLabel = (u.getRole() == UserRole.ADMIN) ? "Admin" : "Officer";
                    System.out.println("\n" + roleLabel + " : " + u.getFullName() + " (ID: " + u.getUserId() + ")");
                    System.out.println("Approved Claims Processed   : " + staffClaims.size());
                    System.out.println("Total Gross Billed Expense  : " + String.format("%,.2f VND", staffBilled));
                    System.out.println("Total Insurance Payout      : " + String.format("%,.2f VND", staffPayout));
                    System.out.println("Total Customer Co-Pay       : " + String.format("%,.2f VND", staffCopay));
                    System.out.println("Processed Claim IDs Details :");
                    for (Claim oc : staffClaims) {
                        Customer cust = context.getCustomerRepository().getById(oc.getInsuredPersonId());
                        double cPayout = (cust != null) ? cust.calculateInsurancePayout(oc.getClaimAmount())
                                : oc.getClaimAmount() * (1.0 - MembershipTier.STANDARD.getEffectiveCopayRate());
                        double cCopay = (cust != null) ? cust.calculatePatientCopay(oc.getClaimAmount())
                                : oc.getClaimAmount() * MembershipTier.STANDARD.getEffectiveCopayRate();
                        System.out.println("  -> Claim " + oc.getId() + " | Billed: " + String.format("%,.2f VND", oc.getClaimAmount())
                                + " | Payout: " + String.format("%,.2f VND", cPayout)
                                + " | Co-pay: " + String.format("%,.2f VND", cCopay)
                                + " | Insured: " + oc.getInsuredPersonId() + " | Date: " + oc.getClaimDate());
                    }
                }
            }
        }

        double grandTotalApprovedPayout = 0.0;
        double grandTotalApprovedCopay = 0.0;
        int grandTotalApprovedCount = 0;
        double unassignedPayout = 0.0;
        double unassignedCopay = 0.0;
        int unassignedCount = 0;

        for (Claim c : context.getClaimRepository().getAll()) {
            if (c.getStatus() == ClaimStatus.DONE) {
                Customer cust = context.getCustomerRepository().getById(c.getInsuredPersonId());
                double cPayout = (cust != null) ? cust.calculateInsurancePayout(c.getClaimAmount())
                        : c.getClaimAmount() * (1.0 - MembershipTier.STANDARD.getEffectiveCopayRate());
                double cCopay = (cust != null) ? cust.calculatePatientCopay(c.getClaimAmount())
                        : c.getClaimAmount() * MembershipTier.STANDARD.getEffectiveCopayRate();
                grandTotalApprovedPayout += cPayout;
                grandTotalApprovedCopay += cCopay;
                grandTotalApprovedCount++;
                if (c.getProcessedByUserId() == null || c.getProcessedByUserId().trim().isEmpty()) {
                    unassignedPayout += cPayout;
                    unassignedCopay += cCopay;
                    unassignedCount++;
                }
            }
        }

        if (unassignedCount > 0) {
            System.out.println("\nLegacy / Unassigned Approved Claims (pre-existing before Phase 7 tracking):");
            System.out.println("Count: " + unassignedCount + " claim(s) | Payout: " + String.format("%,.2f VND", unassignedPayout)
                    + " | Co-pay: " + String.format("%,.2f VND", unassignedCopay));
        }

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("Grand Total Approved Claims   : " + grandTotalApprovedCount);
        System.out.println("Grand Total Gross Billed      : " + String.format("%,.2f VND", (grandTotalApprovedPayout + grandTotalApprovedCopay)));
        System.out.println("Grand Total Insurance Payout  : " + String.format("%,.2f VND", grandTotalApprovedPayout));
        System.out.println("Grand Total Customer Co-Pay   : " + String.format("%,.2f VND", grandTotalApprovedCopay));
        System.out.println("--------------------------------------------------------------------------------");
    }

    /**
     * Generates a tier-based financial summary showing total claims, insurance payouts,
     * and customer co-pay savings categorized by Membership Tier (Standard, Silver, Gold, Platinum).
     */
    public void printTierBasedFinancialSummaryReport() {
        System.out.println("\n===== TIER-BASED FINANCIAL SUMMARY REPORT =====");
        System.out.println("Categorized by Customer Membership Tier (Standard, Silver, Gold, Platinum)\n");

        MembershipTier[] tiers = MembershipTier.values();
        int[] claimCounts = new int[tiers.length];
        double[] grossBilled = new double[tiers.length];
        double[] insurancePayouts = new double[tiers.length];
        double[] customerCopays = new double[tiers.length];
        double[] copaySavings = new double[tiers.length];
        int[] customerCounts = new int[tiers.length];

        for (Customer cust : context.getCustomerRepository().getAll()) {
            MembershipTier tier = cust.getMembershipTier();
            customerCounts[tier.ordinal()]++;
        }

        for (Claim c : context.getClaimRepository().getAll()) {
            if (c.getStatus() == ClaimStatus.DONE) {
                Customer cust = context.getCustomerRepository().getById(c.getInsuredPersonId());
                MembershipTier tier = (cust != null) ? cust.getMembershipTier() : MembershipTier.STANDARD;
                int idx = tier.ordinal();

                double billed = c.getClaimAmount();
                double actualCopay = (cust != null) ? cust.calculatePatientCopay(billed)
                        : billed * tier.getEffectiveCopayRate();
                double payout = (cust != null) ? cust.calculateInsurancePayout(billed)
                        : billed - actualCopay;
                double baselineCopay = billed * MembershipTier.BASE_COPAY_RATE;
                double saving = baselineCopay - actualCopay;

                claimCounts[idx]++;
                grossBilled[idx] += billed;
                insurancePayouts[idx] += payout;
                customerCopays[idx] += actualCopay;
                copaySavings[idx] += saving;
            }
        }

        System.out.printf("%-16s | %-10s | %-12s | %-20s | %-20s | %-20s | %-18s%n",
                "Tier", "Customers", "Done Claims", "Gross Billed", "Insurance Payout", "Customer Co-Pay", "Co-Pay Savings");
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------");

        int totalClaims = 0;
        double totalBilled = 0.0;
        double totalPayout = 0.0;
        double totalCopay = 0.0;
        double totalSavings = 0.0;
        int totalCustomers = 0;

        for (MembershipTier tier : tiers) {
            int idx = tier.ordinal();
            System.out.printf("%-16s | %-10d | %-12d | %-20s | %-20s | %-20s | %-18s%n",
                    tier.name() + " (" + String.format("%.1f%%", tier.getEffectiveCopayRate() * 100) + ")",
                    customerCounts[idx],
                    claimCounts[idx],
                    String.format("%,.2f VND", grossBilled[idx]),
                    String.format("%,.2f VND", insurancePayouts[idx]),
                    String.format("%,.2f VND", customerCopays[idx]),
                    String.format("%,.2f VND", copaySavings[idx]));

            totalCustomers += customerCounts[idx];
            totalClaims += claimCounts[idx];
            totalBilled += grossBilled[idx];
            totalPayout += insurancePayouts[idx];
            totalCopay += customerCopays[idx];
            totalSavings += copaySavings[idx];
        }

        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-16s | %-10d | %-12d | %-20s | %-20s | %-20s | %-18s%n",
                "TOTAL",
                totalCustomers,
                totalClaims,
                String.format("%,.2f VND", totalBilled),
                String.format("%,.2f VND", totalPayout),
                String.format("%,.2f VND", totalCopay),
                String.format("%,.2f VND", totalSavings));
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------");
    }
}
