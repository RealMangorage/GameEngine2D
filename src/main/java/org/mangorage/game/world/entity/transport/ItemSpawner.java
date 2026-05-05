package org.mangorage.game.world.entity.transport;

import org.mangorage.game.render.RenderContext;
import org.mangorage.game.world.World;
import org.mangorage.game.world.entity.Entity;
import org.mangorage.game.world.registeries.Entities;
import org.mangorage.game.world.pos.BoundingBox;
import org.mangorage.game.input.MouseButton;
import org.mangorage.game.world.resource.item.IItemReceiver;
import org.mangorage.game.world.resource.item.Item;
import org.mangorage.game.world.entity.transport.ItemBelt;

import java.awt.*;

/**
 * Simple item spawner that periodically emits Items to the entity directly in front of it.
 */
public final class ItemSpawner extends Entity {

    // Spawning is manual via click now.

    public ItemSpawner(World world, BoundingBox box) {
        super(Entities.ITEM_SPAWNER_ENTITY_TYPE, world, box);
    }

    @Override
    public void update(double delta) {
        // no automatic spawning; manual via onClick
    }

    @Override
    public void onClick(MouseButton mouseButton) {
        // spawn a single item when clicked with left mouse button
        if (mouseButton != MouseButton.LEFT) return;

        var b = getBoundingBox();
        int probeX = b.x() + b.width() + 1;
        int probeY = b.y() + (b.height() / 2);
        var target = getWorld().getEntityAt(probeX, probeY);
        var item = new Item("Spawned");
        if (target instanceof ItemBelt belt) {
            var c = getCenter();
            belt.acceptItem(item);
        } else if (target instanceof IItemReceiver receiver) {
            receiver.acceptItem(item);
        }
    }

    @Override
    public void render(RenderContext ctx) {
        ctx.submit(g -> {
            var b = getBoundingBox();
            g.setColor(Color.BLUE);
            g.fillRect(b.x(), b.y(), b.width(), b.height());
            g.setColor(Color.WHITE);
            g.drawString("IS", b.x() + 4, b.y() + 12);
        });
    }
}


