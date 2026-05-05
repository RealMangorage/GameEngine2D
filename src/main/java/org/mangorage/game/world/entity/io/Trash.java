package org.mangorage.game.world.entity.io;

import org.mangorage.game.render.RenderContext;
import org.mangorage.game.world.World;
import org.mangorage.game.world.pos.BoundingBox;
import org.mangorage.game.world.pos.Position;
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
    public boolean acceptItem(Item item) {
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

            if (box.parts().isEmpty()) {
                g.fillRect(x, y, box.width(), box.height());
                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(x, y, box.width(), box.height());
                g.setColor(Color.WHITE);
                g.drawString("VOID", x + 2, y + 15);
                return;
            }

            // multipart-safe rendering (future-proof)
            for (var p : box.parts()) {
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