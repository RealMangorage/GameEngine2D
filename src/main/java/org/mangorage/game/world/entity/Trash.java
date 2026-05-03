package org.mangorage.game.world.entity;

import org.mangorage.game.world.misc.BoundingBox;
import org.mangorage.game.world.misc.INode;
import org.mangorage.game.world.misc.Item;
import org.mangorage.game.world.World;
import org.mangorage.game.world.registeries.Entities;

public final class Trash extends Entity implements IItemReceiver, INode {
    public Trash(World world, BoundingBox box) {
        super(Entities.TRASH_ENTITY_TYPE, world, box);
    }

    @Override public int getMaxInputs() { return Integer.MAX_VALUE; }
    @Override public int getMaxOutputs() { return 0; }
    @Override public int getInputCount() { return 0; }
    @Override public int getOutputCount() { return 0; }

    @Override
    public boolean acceptItem(Item item) {
        return true; // Always eats items
    }

    @Override
    public boolean connect(INode node) { return false; }
}