package org.mangorage.game.world.entity;

import org.mangorage.game.input.MouseButton;
import org.mangorage.game.render.RenderContext;
import org.mangorage.game.world.World;
import org.mangorage.game.world.misc.BoundingBox;
import org.mangorage.game.world.misc.INode;
import org.mangorage.game.world.misc.Item;
import org.mangorage.game.world.registeries.Entities;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class Splitter extends EntityIO {

    public Splitter(World world, BoundingBox box) {
        // Inputs: 1, Outputs: 4, Speed: Fast, Spacing: 0.18
        super(Entities.SPLITTER_ENTITY_TYPE, world, box, 1, 4, 1.0 / 23.0, 0.18);
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
        // Custom colors for Splitter: Orange items, 12px size
        renderConnectionsAndItems(ctx, Color.GRAY, Color.ORANGE, 12);

        ctx.submit(g -> {
            var b = getBoundingBox();
            g.setColor(new Color(60, 60, 100));
            g.fillRect(b.x(), b.y(), b.width(), b.height());

            g.setColor(Color.WHITE);
            g.drawRect(b.x(), b.y(), b.width(), b.height());
            g.drawString("S", b.x() + 4, b.y() + 12);
        });
    }
}