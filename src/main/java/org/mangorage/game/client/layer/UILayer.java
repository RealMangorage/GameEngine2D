package org.mangorage.game.client.layer;

import org.mangorage.game.input.GameMouseEvent;
import org.mangorage.game.client.render.core.RenderContext;
import org.mangorage.game.world.registeries.Entities;

import java.awt.*;
import java.util.Queue;

public final class UILayer extends Layer {

    private final WorldLayer worldLayer;

    public UILayer(WorldLayer worldLayer) {
        this.worldLayer = worldLayer;
    }

    @Override
    public void update(double delta) {
        // UI might not need updating logic right now
    }

    @Override
    public void handleInput(double delta, Queue<GameMouseEvent> mouseEvents) {
        // UI could capture clicks here to prevent them going to the world layer later
    }

    @Override
    public void render(RenderContext context) {
        context.submit(g -> {
            // We draw directly to the Graphics object so it stays static on the screen
            g.setColor(Color.BLUE);
            g.drawString(
                    "Selected: " + (worldLayer.getSelected() == null ? "NONE" : worldLayer.getSelected().getType().getName()),
                    10, 20
            );
            g.drawString(
                    "Selected Type: " + Entities.ENTITY_TYPES.get(worldLayer.getSelectedType()).getName(),
                    10, 40
            );
            g.drawString("Placing Mode: " + worldLayer.getPlacingMode() + " (F4)", 10, 60);
            g.drawString("Rotation: " + (worldLayer.getSelected() != null ? worldLayer.getSelected().getFacing() : worldLayer.getCurrentRotation()), 10, 80);
            g.drawString("Snap: " + (worldLayer.isPlacementSnap() ? "ON (G)" : "OFF (G)"), 10, 100);
            g.drawString("SnapX: " + (worldLayer.isPlacementSnapX() ? "ON (X)" : "OFF (X)"), 10, 120);
            g.drawString("SnapY: " + (worldLayer.isPlacementSnapY() ? "ON (Y)" : "OFF (Y)"), 10, 140);
            g.drawString("Auto-Orient: " + (worldLayer.isPlacementAutoOrient() ? "ON (O)" : "OFF (O)"), 10, 160);
        });
    }
}
