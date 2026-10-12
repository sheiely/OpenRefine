
package com.google.refine.operations.recon.strategy;

import com.google.refine.model.Recon;
import com.google.refine.model.ReconCandidate;

public final class MatchedJudgmentStrategy
        implements ReconJudgmentStrategy {

    @Override
    public void apply(Recon recon, ReconCandidate match) {
        recon.judgment = Recon.Judgment.Matched;
        recon.match = match;
        recon.matchRank = -1;

        if (recon.candidates != null) {
            for (int i = 0; i < recon.candidates.size(); i++) {
                if (recon.candidates.get(i).id.equals(match.id)) {
                    recon.matchRank = i;
                    break;
                }
            }
        }
    }
}
