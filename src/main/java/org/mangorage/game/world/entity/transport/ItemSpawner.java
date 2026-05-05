package org.mangorage.game.world.entity.transport;

import org.mangorage.game.render.RenderContext;
import org.mangorage.game.world.World;
import org.mangorage.game.world.entity.Entity;
import org.mangorage.game.world.pos.BoundingBox;
import org.mangorage.game.world.pos.Position;
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

        int probeX = pos.x() + box.width() + 1;
        int probeY = pos.y() + box.height() / 2;

        var target = getWorld().getEntityAt(probeX, probeY);

        Item item = new Item("Spawned");

        if (target instanceof IItemReceiver receiver) {
            receiver.acceptItem(item);
        }
    }

    @Override
    public void render(RenderContext ctx) {

        ctx.submit(g -> {
            Position pos = getPosition();
            var box = getBoundingBox();

            g.setColor(Color.BLUE);
            g.fillRect(pos.x(), pos.y(), box.width(), box.height());

            g.setColor(Color.WHITE);
            g.drawString("IS", pos.x() + 4, pos.y() + 12);
        });
    }
}