package com.google.refine.operations.recon.strategy;

import com.google.refine.model.Recon;
import com.google.refine.model.ReconCandidate;

public interface ReconJudgmentStrategy {

    void apply(Recon recon, ReconCandidate match);
}
