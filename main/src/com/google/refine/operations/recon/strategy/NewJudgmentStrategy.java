
package com.google.refine.operations.recon.strategy;

import com.google.refine.model.Recon;
import com.google.refine.model.ReconCandidate;

public final class NewJudgmentStrategy
        implements ReconJudgmentStrategy {

    @Override
    public void apply(Recon recon, ReconCandidate match) {
        recon.judgment = Recon.Judgment.New;
        recon.match = null;
        recon.matchRank = -1;
    }
}
