package org.mangorage.game.world.entity;

import org.mangorage.game.input.MouseButton;
import org.mangorage.game.render.RenderContext;
import org.mangorage.game.world.World;
import org.mangorage.game.world.misc.BoundingBox;
import org.mangorage.game.world.registeries.Entities;

import java.awt.*;

public final class RelayBox extends EntityIO {
    public RelayBox(World world, BoundingBox box) {
        super(Entities.RELAY_BOX_ENTITY_TYPE, world, box, 1, 1, 1.0/225.0, 0.18);
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
        ctx.submit(g -> {
            var b = getBoundingBox();
            g.setColor(Color.DARK_GRAY);
            g.fillRect(b.x(), b.y(), b.width(), b.height());
        });
    }
}