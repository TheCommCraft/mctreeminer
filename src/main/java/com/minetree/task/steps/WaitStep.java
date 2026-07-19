package com.minetree.task.steps;

import com.minetree.MineTreeException;
import com.minetree.task.Step;
import com.minetree.task.StepResult;

public final class WaitStep implements Step {

    @FunctionalInterface
    public interface Action {
        void run() throws MineTreeException;
    }

    private boolean done;
    private final String label;

    public WaitStep(String label) {
        this.label = label;
        this.done = false;
    }

    @Override
    public StepResult tick() throws MineTreeException {
        if (done) {
            return StepResult.DONE;
        }
        done = true;
        return StepResult.IN_PROGRESS;
    }

    @Override
    public String describe() {
        return "WaitStep(" + label + ")";
    }
}
