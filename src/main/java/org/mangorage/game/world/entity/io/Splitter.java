package org.mangorage.game.world.entity.io;

import org.mangorage.game.input.MouseButton;
import org.mangorage.game.client.render.core.RenderContext;
import org.mangorage.game.world.World;
import org.mangorage.game.world.pos.BoundingBox;
import org.mangorage.game.world.pos.Position;
import org.mangorage.game.world.pos.Facing;
import org.mangorage.game.world.registeries.Entities;

import java.awt.*;

public final class Splitter extends EntityIO {

    public Splitter(World world, BoundingBox box) {
        // Inputs: 1, Outputs: 4, Speed: Fast, Spacing: 0.18
        super(
                Entities.SPLITTER_ENTITY_TYPE,
                world,
                box,
                1,
                4,
                1.0 / 23.0,
                0.18
        );
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

        renderConnectionsAndItems(ctx, Color.GRAY, Color.ORANGE, 12);

        ctx.submit(g -> {
            Position pos = getPosition();
            var box = getBoundingBox();

            int x = pos.x();
            int y = pos.y();

            // body
            g.setColor(new Color(60, 60, 100));

            if (box.parts().isEmpty()) {
                int w = (getFacing() == Facing.EAST || getFacing() == Facing.WEST) ? box.width() : box.height();
                int h = (getFacing() == Facing.EAST || getFacing() == Facing.WEST) ? box.height() : box.width();
                g.fillRect(x, y, w, h);
                g.setColor(Color.WHITE);
                g.drawRect(x, y, w, h);
                g.drawString("S", x + 4, y + 12);
                return;
            }

            // multipart rendering (rotation-aware)
            for (var p : box.parts(getFacing())) {
                g.fillRect(
                        x + p.offsetX(),
                        y + p.offsetY(),
                        p.width(),
                        p.height()
                );

                g.setColor(Color.WHITE);
                g.drawRect(
                        x + p.offsetX(),
                        y + p.offsetY(),
                        p.width(),
                        p.height()
                );
            }

            g.setColor(Color.WHITE);
            g.drawString("S", x + 4, y + 12);
        });
    }
}