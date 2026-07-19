package com.minetree.task;

import baritone.api.BaritoneAPI;
import com.minetree.MineTreeException;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public abstract class MineTreeTask {

    private final List<Step> steps;
    private int index = 0;
    private Consumer<Text> prevLoggerValue;

    protected MineTreeTask(List<Step> steps) {
        this.steps = steps;
        prevLoggerValue = BaritoneAPI.getSettings().logger.value;
        BaritoneAPI.getSettings().logger.value = (_msg) -> {

        };
    }

    public final boolean tick() throws MineTreeException {
        if (index >= steps.size()) {
            BaritoneAPI.getSettings().logger.value = prevLoggerValue;
            return true;
        }
        Step current = steps.get(index);
        StepResult result = current.tick();
        while (result == StepResult.DONE) {
            index++;
            if (index >= steps.size()) {
                BaritoneAPI.getSettings().logger.value = prevLoggerValue;
                return true;
            }
            current = steps.get(index);
            result = current.tick();
        }
        return false;
    }

    public int totalSteps() {
        return steps.size();
    }

    public int currentStepIndex() {
        return index;
    }

    public String currentStepDescription() {
        return index < steps.size() ? steps.get(index).describe() : "done";
    }
}
