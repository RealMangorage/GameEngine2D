package org.mangorage.game.world.entity.io;

import org.mangorage.game.client.render.core.RenderContext;
import org.mangorage.game.world.World;
import org.mangorage.game.world.pos.BoundingBox;
import org.mangorage.game.world.pos.Position;
import org.mangorage.game.world.pos.Facing;
import org.mangorage.game.world.resource.item.Item;
import org.mangorage.game.world.registeries.Entities;

import java.awt.*;

public final class Trash extends EntityIO {

    public Trash(World world, BoundingBox box) {
        super(
                Entities.TRASH_ENTITY_TYPE,
                world,
                box,
                Integer.MAX_VALUE,
                0,
                0,
                0
        );
    }

    @Override
    public boolean acceptItem(Item item, org.mangorage.game.world.pos.Position source) {
        return true;
    }

    @Override
    public void update(double delta) {
        // no-op
    }

    @Override
    public void render(RenderContext ctx) {

        ctx.submit(g -> {
            Position pos = getPosition();
            var box = getBoundingBox();

            int x = pos.x();
            int y = pos.y();

            g.setColor(Color.RED);

            // If no parts defined, draw full rect but account for rotation (swap extents for N/W)
            if (box.parts().isEmpty()) {
                int w = (getFacing() == Facing.EAST || getFacing() == Facing.WEST) ? box.width() : box.height();
                int h = (getFacing() == Facing.EAST || getFacing() == Facing.WEST) ? box.height() : box.width();
                g.fillRect(x, y, w, h);
                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(x, y, w, h);
                g.setColor(Color.WHITE);
                g.drawString("VOID", x + 2, y + 15);
                return;
            }

            // multipart-safe rendering (rotation-aware)
            for (var p : box.parts(getFacing())) {
                int px = x + p.offsetX();
                int py = y + p.offsetY();

                g.fillRect(px, py, p.width(), p.height());

                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(px, py, p.width(), p.height());

                g.setColor(Color.WHITE);
                g.drawString("VOID", px + 2, py + 15);
            }
        });
    }
}