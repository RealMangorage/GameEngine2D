package org.mangorage.game.world.entity;

import org.mangorage.game.render.RenderContext;
import org.mangorage.game.world.misc.BoundingBox;
import org.mangorage.game.world.misc.INode;
import org.mangorage.game.world.misc.Item;
import org.mangorage.game.world.World;
import org.mangorage.game.world.registeries.Entities;

import java.awt.*;

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

    @Override
    public void render(RenderContext ctx) {
        ctx.submit(g -> {
            var box = getBoundingBox();
            g.setColor(Color.RED);
            g.fillRect(box.x(), box.y(), box.width(), box.height());
            g.setColor(Color.LIGHT_GRAY);
            g.drawRect(box.x(), box.y(), box.width(), box.height());
        });
    }
}