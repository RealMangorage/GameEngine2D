package org.mangorage.game.world.entity;

import org.mangorage.game.world.misc.BoundingBox;
import org.mangorage.game.world.misc.Item;
import org.mangorage.game.world.World;
import org.mangorage.game.world.registeries.Entities;

public final class Trash extends Entity implements IItemReceiver {
    public Trash(World world, BoundingBox box) {
        super(Entities.TRASH_ENTITY_TYPE, world, box);
    }

    @Override
    public boolean acceptItem(Item item) {
        // Log it or just let it be garbage collected
        System.out.println("Item voided: " + item.getName());
        return true;
    }
}