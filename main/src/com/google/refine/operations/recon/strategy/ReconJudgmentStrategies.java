
package com.google.refine.operations.recon.strategy;

import java.util.EnumMap;
import java.util.Map;

import com.google.refine.model.Recon.Judgment;

public final class ReconJudgmentStrategies {

    private static final Map<Judgment, ReconJudgmentStrategy>
            STRATEGIES = new EnumMap<>(Judgment.class);

    static {
        STRATEGIES.put(
                Judgment.None,
                new NoneJudgmentStrategy()
        );

        STRATEGIES.put(
                Judgment.New,
                new NewJudgmentStrategy()
        );

        STRATEGIES.put(
                Judgment.Matched,
                new MatchedJudgmentStrategy()
        );
    }

    private ReconJudgmentStrategies() {
    }

    public static ReconJudgmentStrategy get(Judgment judgment) {
        if (judgment == Judgment.Error) {
            throw new IllegalArgumentException(
                    "Cannot manually set judgment to 'error'"
            );
        }

        ReconJudgmentStrategy strategy = STRATEGIES.get(judgment);

        if (strategy == null) {
            throw new IllegalArgumentException(
                    "Unsupported recon judgment: " + judgment
            );
        }

        return strategy;
    }
}
