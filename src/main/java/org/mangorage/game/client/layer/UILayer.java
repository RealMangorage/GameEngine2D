package org.mangorage.game.client.layer;

import org.mangorage.game.Game;
import org.mangorage.game.input.GameMouseEvent;
import org.mangorage.game.client.render.core.RenderContext;
import org.mangorage.game.world.registeries.Entities;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public final class UILayer extends Layer {

    private final WorldLayer worldLayer;

    private boolean menuOpen = false;

    private final List<Button> buttons = new ArrayList<>();

    public UILayer(WorldLayer worldLayer) {
        this.worldLayer = worldLayer;

        // Simple example buttons
        buttons.add(new Button(20, 200, 140, 30, "Spawn Entity", () -> {
            System.out.println("Spawn clicked");
        }));

        buttons.add(new Button(20, 240, 140, 30, "Clear World", () -> {
            System.out.println("Clear clicked");
        }));
    }

    /** Call this when E is pressed */
    public void toggleMenu() {
        menuOpen = !menuOpen;
    }

    @Override
    public void handleKeyEvent() {
        if (Game.getInstance().isKeyDown(KeyEvent.VK_E)) {
            toggleMenu();
        }
    }

    @Override
    public void update(double delta) {
    }

    @Override
    public boolean handleInput(double delta, Queue<GameMouseEvent> mouseEvents, int mouseX, int mouseY) {

        if (!menuOpen) {
            return false;
        }

        while (!mouseEvents.isEmpty()) {
            GameMouseEvent event = mouseEvents.poll();

            if (event.button() == 1) {
                for (Button button : buttons) {
                    if (button.contains(mouseX, mouseY)) {
                        button.onClick.run();
                        return true; // UI consumed input
                    }
                }

                // Clicked inside UI area but not on a button
                if (mouseX < 300 && mouseY < 400) {
                    return true;
                }
            }
        }

        return true; // menu open = UI blocks world input
    }

    @Override
    public void render(RenderContext context) {
        context.submit(g -> {

            g.setColor(Color.BLUE);

            g.drawString(
                    "Selected: " + (worldLayer.getSelected() == null
                            ? "NONE"
                            : worldLayer.getSelected().getType().getName()),
                    10, 20
            );

            g.drawString(
                    "Selected Type: " + Entities.ENTITY_TYPES.get(worldLayer.getSelectedType()).getName(),
                    10, 40
            );

            g.drawString("Placing Mode: " + worldLayer.getPlacingMode() + " (F4)", 10, 60);
            g.drawString("Rotation: " + (worldLayer.getSelected() != null
                    ? worldLayer.getSelected().getFacing()
                    : worldLayer.getCurrentRotation()), 10, 80);

            g.drawString("Snap: " + (worldLayer.isPlacementSnap() ? "ON (G)" : "OFF (G)"), 10, 100);
            g.drawString("SnapX: " + (worldLayer.isPlacementSnapX() ? "ON (X)" : "OFF (X)"), 10, 120);
            g.drawString("SnapY: " + (worldLayer.isPlacementSnapY() ? "ON (Y)" : "OFF (Y)"), 10, 140);
            g.drawString("Auto-Orient: " + (worldLayer.isPlacementAutoOrient() ? "ON (O)" : "OFF (O)"), 10, 160);

            if (menuOpen) {
                renderMenu(g);
            }
        });
    }

    private void renderMenu(Graphics g) {
        // Background panel
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(10, 180, 300, 250);

        g.setColor(Color.WHITE);
        g.drawString("MENU", 20, 200);

        for (Button button : buttons) {
            button.render(g);
        }
    }

    /** Simple UI button */
    private static final class Button {
        int x, y, w, h;
        String text;
        Runnable onClick;

        Button(int x, int y, int w, int h, String text, Runnable onClick) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.text = text;
            this.onClick = onClick;
        }

        boolean contains(int mx, int my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }

        void render(Graphics g) {
            g.setColor(Color.DARK_GRAY);
            g.fillRect(x, y, w, h);

            g.setColor(Color.WHITE);
            g.drawRect(x, y, w, h);
            g.drawString(text, x + 8, y + 20);
        }
    }
}