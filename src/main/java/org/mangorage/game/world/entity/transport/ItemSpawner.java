package org.mangorage.game.world.entity.transport;

import org.mangorage.game.client.render.core.RenderContext;
import org.mangorage.game.world.World;
import org.mangorage.game.world.entity.Entity;
import org.mangorage.game.world.pos.BoundingBox;
import org.mangorage.game.world.pos.Position;
import org.mangorage.game.world.pos.Facing;
import org.mangorage.game.world.registeries.Entities;
import org.mangorage.game.input.MouseButton;
import org.mangorage.game.world.resource.item.IItemReceiver;
import org.mangorage.game.world.resource.item.Item;

import java.awt.*;

/**
 * Simple item spawner that emits Items to the entity directly in front of it.
 */
public final class ItemSpawner extends Entity {

    public ItemSpawner(World world, BoundingBox box) {
        super(Entities.ITEM_SPAWNER_ENTITY_TYPE, world, box);
    }

    @Override
    public void update(double delta) {
        // no auto logic
    }

    @Override
    public void onClick(MouseButton mouseButton) {
        if (mouseButton != MouseButton.LEFT) return;

        Position pos = getPosition();
        var box = getBoundingBox();

        int probeX = pos.x() + box.width() / 2;
        int probeY = pos.y() + box.height() / 2;

        switch (getFacing()) {
            case EAST -> probeX = pos.x() + box.width() + 1;
            case WEST -> probeX = pos.x() - 1;
            case SOUTH -> probeY = pos.y() + box.height() + 1;
            case NORTH -> probeY = pos.y() - 1;
        }

        var target = getWorld().getEntityAt(probeX, probeY);

        Item item = new Item("Spawned");

        if (target instanceof IItemReceiver receiver) {
            Point c = getCenter();
            receiver.acceptItem(item, new Position(c.x, c.y, 0));
        }
    }

    @Override
    public void render(RenderContext ctx) {

        ctx.submit(g -> {
            Position pos = getPosition();
            var box = getBoundingBox();
            g.setColor(Color.BLUE);

            if (box.parts().isEmpty()) {
                int w = (getFacing() == Facing.EAST || getFacing() == Facing.WEST) ? box.width() : box.height();
                int h = (getFacing() == Facing.EAST || getFacing() == Facing.WEST) ? box.height() : box.width();
                g.fillRect(pos.x(), pos.y(), w, h);
            } else {
                for (var p : box.parts(getFacing())) {
                    g.fillRect(pos.x() + p.offsetX(), pos.y() + p.offsetY(), p.width(), p.height());
                }
            }

            g.setColor(Color.WHITE);
            g.drawString("IS", pos.x() + 4, pos.y() + 12);
        });
    }
}