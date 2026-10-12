
package com.google.refine.operations.recon.strategy;

import com.google.refine.model.Recon;
import com.google.refine.model.ReconCandidate;

public final class NoneJudgmentStrategy
        implements ReconJudgmentStrategy {

    @Override
    public void apply(Recon recon, ReconCandidate match) {
        recon.judgment = recon.error == null
                ? Recon.Judgment.None
                : Recon.Judgment.Error;

        recon.match = null;
        recon.matchRank = -1;
    }
}
