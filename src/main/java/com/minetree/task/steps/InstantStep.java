package com.minetree.task.steps;

import com.minetree.MineTreeException;
import com.minetree.task.Step;
import com.minetree.task.StepResult;

public final class InstantStep implements Step {

    @FunctionalInterface
    public interface Action {
        void run() throws MineTreeException;
    }

    private final Action action;
    private final String label;

    public InstantStep(String label, Action action) {
        this.label = label;
        this.action = action;
    }

    @Override
    public StepResult tick() throws MineTreeException {
        action.run();
        return StepResult.DONE;
    }

    @Override
    public String describe() {
        return "InstantStep(" + label + ")";
    }
}
