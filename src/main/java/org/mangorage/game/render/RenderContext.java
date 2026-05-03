package org.mangorage.game.render;

import java.awt.Graphics2D;
import java.util.*;

public final class RenderContext {
    // A simple container for the command and its priority
    private record LayeredCommand(int layer, RenderCommand action) {}

    private final List<LayeredCommand> commandList = new ArrayList<>();
    private int layer = 0; // Default layer


    public void submit(RenderCommand action) {
        commandList.add(new LayeredCommand(layer, action));
    }

    public void push() {
        layer++;
    }

    public void pop() {
        if (layer > 0) {
            layer--;
        }
    }

    public void render(Graphics2D g) {
        // Sort everything: Lowest layer (0) to Highest layer (2)
        commandList.sort(Comparator.comparingInt(LayeredCommand::layer));

        for (LayeredCommand cmd : commandList) {
            cmd.action.execute(g);
        }

        commandList.clear();
    }
}