package com.minetree.task;

import com.minetree.MineTreeException;

public interface Step {
    StepResult tick() throws MineTreeException;

    default String describe() {
        return getClass().getSimpleName();
    }
}
