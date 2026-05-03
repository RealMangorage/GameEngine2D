package org.mangorage.game.world.entity.io;

import org.mangorage.game.render.RenderContext;
import org.mangorage.game.world.pos.BoundingBox;
import org.mangorage.game.world.item.Item;
import org.mangorage.game.world.World;
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
    public void update(double delta) {}

    @Override
    public void render(RenderContext ctx) {
        ctx.submit(g -> {
            var box = getBoundingBox();
            g.setColor(Color.RED);
            g.fillRect(box.x(), box.y(), box.width(), box.height());

            g.setColor(Color.LIGHT_GRAY);
            g.drawRect(box.x(), box.y(), box.width(), box.height());

            g.setColor(Color.WHITE);
            g.drawString("VOID", box.x() + 2, box.y() + 15);
        });
    }
}