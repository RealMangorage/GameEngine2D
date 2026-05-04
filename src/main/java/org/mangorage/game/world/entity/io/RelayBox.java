package org.mangorage.game.world.entity.io;

import org.mangorage.game.input.MouseButton;
import org.mangorage.game.render.RenderContext;
import org.mangorage.game.world.World;
import org.mangorage.game.world.pos.BoundingBox;
import org.mangorage.game.world.registeries.Entities;

import java.awt.*;

public final class RelayBox extends EntityIO {
    public RelayBox(World world, BoundingBox box) {
        super(
                Entities.RELAY_BOX_ENTITY_TYPE,
                world,
                box,
                1,
                1,
                1.0/225.0,
                0.18
        );
        // Make this entity a 'T' shape by replacing the default box with multiple parts.
        // Use the provided bounding box width/height (expected 32x32) so parts scale if sizes change.
        box.clearParts();
        int w = box.width();
        int h = box.height();

        // Horizontal top bar: full width, 1/4 of total height
        int topBarH = Math.max(1, h / 4);
        box.addPart(0, 0, w, topBarH);

        // Vertical stem: centered, occupies remaining height below the top bar, width = 1/4 of total width
        int stemW = Math.max(1, w / 4);
        int stemH = Math.max(1, h - topBarH);
        int stemX = (w - stemW) / 2;
        box.addPart(stemX, topBarH, stemW, stemH);
    }

    @Override
    public void onClick(MouseButton mouseButton) {
        if (mouseButton == MouseButton.MIDDLE) {
            outputs.clear();
            items.clear();
        }
    }

    @Override
    public void render(RenderContext ctx) {
        renderConnectionsAndItems(ctx, Color.GRAY, Color.YELLOW, 16);
        // Render each defined bounding-box part so the 'T' shape is visible (not just the overall AABB)
        ctx.submit(g -> {
            var parts = getBoundingBox().getPartsAbsolute();
            g.setColor(Color.DARK_GRAY);
            for (var p : parts) {
                g.fillRect(p.x(), p.y(), p.width(), p.height());
            }

            // optional outline to make parts clear
            g.setColor(Color.WHITE);
            for (var p : parts) {
                g.drawRect(p.x(), p.y(), p.width(), p.height());
            }
        });
    }
}