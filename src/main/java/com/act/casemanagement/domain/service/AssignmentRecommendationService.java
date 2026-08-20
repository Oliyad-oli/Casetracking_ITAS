package com.act.casemanagement.domain.service;

import com.act.casemanagement.domain.aggregate.AssignmentWeightsConfig;
import com.act.casemanagement.domain.valueobject.CaseCategory;
import com.act.casemanagement.domain.valueobject.Priority;
import com.act.casemanagement.domain.valueobject.RiskLevel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

/**
 * Domain service — ports exact weighted-scoring logic from the frontend's
 * {@code assignmentScorer.ts}. Reading weights from {@link AssignmentWeightsConfig},
 * not hardcoded.
 * <p>
 * Scoring dimensions:
 * <ol>
 *   <li>Capacity (100 = 0 active cases, 0 = at/over ceiling of 12)</li>
 *   <li>Specialisation match (department/title/specialisations vs case category)</li>
 *   <li>Region/Tax Centre match</li>
 *   <li>Caseload mix (penalise officers already carrying ≥2 CRITICAL/HIGH cases;
 *       bonus for Senior/Supervisor on CRITICAL incoming case)</li>
 * </ol>
 */
public class AssignmentRecommendationService {

    private static final int MAX_CAPACITY = 12;

    /** Input projection — a minimal read-model fed in by the use case. */
    public record OfficerSnapshot(
        UUID officerId,
        String officerName,
        String unit,
        String department,
        String title,
        String region,
        List<String> specializations,
        String roleCode,
        int activeCaseCount,
        int heavyCaseCount       // active cases that are CRITICAL or HIGH risk/priority
    ) {}

    public record RecommendationScore(
        UUID officerId,
        String officerName,
        int totalScore,
        int capacityScore,
        int specializationScore,
        int regionScore,
        int caseloadMixScore,
        int activeCaseCount,
        int utilizationPct,
        String specializationLabel,
        List<String> reasons,
        boolean isOverCapacity,
        boolean isSelfAssignment
    ) {}

    public List<RecommendationScore> recommend(
            CaseCategory category,
            Priority priority,
            BigDecimal estimatedAmountEtb,
            List<OfficerSnapshot> officers,
            UUID currentActorId,
            AssignmentWeightsConfig weights) {

        return officers.stream()
            .map(o -> score(o, category, priority, estimatedAmountEtb, currentActorId, weights))
            .sorted((a, b) -> Integer.compare(b.totalScore(), a.totalScore()))
            .toList();
    }

    private RecommendationScore score(OfficerSnapshot o, CaseCategory category,
            Priority priority, BigDecimal estimatedAmountEtb,
            UUID currentActorId, AssignmentWeightsConfig weights) {

        boolean isSelf = currentActorId != null && o.officerId().equals(currentActorId);

        // 1. Capacity score
        int activeCases    = o.activeCaseCount();
        int utilizationPct = Math.min((int) Math.round((activeCases * 100.0) / MAX_CAPACITY), 100);
        boolean overCap    = activeCases >= MAX_CAPACITY;
        int capacityScore  = overCap ? 0
            : Math.max(0, (int) Math.round(((double)(MAX_CAPACITY - activeCases) / MAX_CAPACITY) * 100));

        // 2. Specialisation score
        SpecMatch specMatch = matchSpecialisation(o, category);

        // 3. Region score
        int regionScore = 70;
        String centre = lower(o.region()) + " " + lower(o.unit());
        if (centre.contains("addis") || centre.contains("large")) regionScore = 90;

        // 4. Caseload mix score
        int heavyCases       = o.heavyCaseCount();
        int caseloadMixScore = 100;
        if      (heavyCases >= 4) caseloadMixScore = 30;
        else if (heavyCases >= 2) caseloadMixScore = 65;
        else if (heavyCases == 1) caseloadMixScore = 85;

        boolean isCriticalIncoming = priority == Priority.CRITICAL
            || (estimatedAmountEtb != null && estimatedAmountEtb.compareTo(BigDecimal.valueOf(10_000_000)) > 0);
        boolean isSenior = lower(o.title()).contains("senior")
            || "SUPERVISOR".equals(o.roleCode())
            || lower(o.title()).contains("lead");
        if (isCriticalIncoming && isSenior) {
            caseloadMixScore = Math.min(100, caseloadMixScore + 15);
        }

        // Weighted total
        double cw = weights.getCapacityWeight().doubleValue();
        double sw = weights.getSpecializationWeight().doubleValue();
        double rw = weights.getRegionWeight().doubleValue();
        double mw = weights.getCaseloadMixWeight().doubleValue();
        double totalWeight = cw + sw + rw + mw;
        if (totalWeight == 0) totalWeight = 1;

        double raw = (capacityScore * cw + specMatch.score() * sw
                    + regionScore * rw + caseloadMixScore * mw) / totalWeight;
        int totalScore = Math.min(100, Math.max(10, (int) Math.round(raw)));

        var reasons = new java.util.ArrayList<String>();
        if (isSelf) reasons.add("Self-Assignment: mandatory justification required (CTR0200/CTR0300)");
        reasons.add(specMatch.label());
        reasons.add("Capacity: " + activeCases + "/" + MAX_CAPACITY + " (" + utilizationPct + "% utilisation)");
        reasons.add(heavyCases > 0 ? heavyCases + " high-priority cases in current workload"
                                   : "No heavy backlog");
        reasons.add("Region: " + (o.region() != null ? o.region() : "Addis Ababa Central"));

        return new RecommendationScore(o.officerId(), o.officerName(), totalScore,
                capacityScore, specMatch.score(), regionScore, caseloadMixScore,
                activeCases, utilizationPct, specMatch.label(), reasons, overCap, isSelf);
    }

    private record SpecMatch(int score, String label) {}

    private SpecMatch matchSpecialisation(OfficerSnapshot o, CaseCategory category) {
        String dept  = lower(o.department()) + " " + lower(o.unit());
        String title = lower(o.title());
        var specs    = o.specializations() == null ? List.<String>of()
                       : o.specializations().stream().map(String::toLowerCase).toList();

        return switch (category) {
            case AUDIT -> {
                if (dept.contains("audit") || title.contains("audit") || dept.contains("investigation")
                        || specs.stream().anyMatch(s -> s.contains("audit")))
                    yield new SpecMatch(100, "Direct: Tax Audit & Forensic Investigation");
                if (dept.contains("revenue") || dept.contains("compliance"))
                    yield new SpecMatch(75, "Related: Revenue Operations & Compliance");
                yield new SpecMatch(40, "General Tax Administration");
            }
            case OBJECTION, APPEAL, APPEAL_REVIEW -> {
                if (dept.contains("objection") || dept.contains("appeal") || dept.contains("legal"))
                    yield new SpecMatch(100, "Direct: Tax Objections & Appeals Directorate");
                if (dept.contains("audit"))
                    yield new SpecMatch(70, "Cross-functional: Prior Audit & Assessment");
                yield new SpecMatch(40, "General Tax Administration");
            }
            case LITIGATION -> {
                if (dept.contains("legal") || dept.contains("litigation") || title.contains("counsel"))
                    yield new SpecMatch(100, "Direct: Legal Affairs & Court Litigation");
                if (dept.contains("objection"))
                    yield new SpecMatch(65, "Related: Administrative Appeals");
                yield new SpecMatch(30, "Non-Legal Background");
            }
            case DEBT, DELINQUENCY -> {
                if (dept.contains("debt") || dept.contains("recovery") || dept.contains("enforcement"))
                    yield new SpecMatch(100, "Direct: Debt Recovery & Enforcement");
                if (dept.contains("revenue") || dept.contains("compliance"))
                    yield new SpecMatch(70, "Related: Revenue Operations");
                yield new SpecMatch(40, "General Tax Administration");
            }
            case COMPLIANCE -> {
                if (dept.contains("compliance") || dept.contains("filing"))
                    yield new SpecMatch(100, "Direct: Taxpayer Compliance & Monitoring");
                if (dept.contains("audit"))
                    yield new SpecMatch(80, "Related: Audit & Examination");
                yield new SpecMatch(50, "General Tax Administration");
            }
            case INTELLIGENCE, INVESTIGATION -> {
                if (dept.contains("intelligence") || dept.contains("investigation") || dept.contains("fraud"))
                    yield new SpecMatch(100, "Direct: Intelligence & Risk Analysis");
                if (dept.contains("audit"))
                    yield new SpecMatch(75, "Related: Forensic Audit");
                yield new SpecMatch(40, "General Tax Administration");
            }
            default -> new SpecMatch(60, "Standard Case Administration");
        };
    }

    private static String lower(String s) { return s == null ? "" : s.toLowerCase(); }
}
