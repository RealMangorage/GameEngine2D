package org.mangorage.game.world.entity;

import org.mangorage.game.input.MouseButton;
import org.mangorage.game.render.RenderContext;
import org.mangorage.game.world.misc.BoundingBox;
import org.mangorage.game.world.misc.INode;
import org.mangorage.game.world.misc.Item;
import org.mangorage.game.world.World;
import org.mangorage.game.world.registeries.Entities;
import java.awt.*;

public final class Spawner extends Entity implements IItemReceiver, INode {
    private IItemReceiver target;
    private Item pendingItem;
    private double progress = 0.0;

    public Spawner(World world, BoundingBox box) {
        super(Entities.SPAWNER_ENTITY_TYPE, world, box);
    }

    @Override public int getMaxInputs() { return 0; }
    @Override public int getMaxOutputs() { return 1; }
    @Override public int getInputCount() { return 0; }
    @Override public int getOutputCount() { return target == null ? 0 : 1; }

    public void onClick(MouseButton mouseButton) {
        if (mouseButton == MouseButton.MIDDLE) {
            target = null;
        } else if (pendingItem == null) {
            pendingItem = new Item("MangoItem");
            progress = 0.0;
        }
    }

    @Override
    public void onClickWithSelected(Entity selected, Entity clicked) {
        if (selected == this && clicked instanceof INode dest && clicked != this) {
            if (this.canAddMoreOutputs() && dest.canAcceptMoreInputs()) {
                this.connect(dest);
                dest.registerInput();
            }
        }
    }

    @Override
    public boolean connect(INode node) {
        if (node instanceof IItemReceiver receiver) {
            this.target = receiver;
            return true;
        }
        return false;
    }

    @Override public boolean acceptItem(Item item) { return false; }

    @Override
    public void update(double delta) {
        if (pendingItem == null) return;
        progress += delta * (1.0 / 5.0);
        if (progress >= 1.0) {
            if (target != null && target.acceptItem(pendingItem)) {
                pendingItem = null;
                progress = 0.0;
            } else {
                progress = 1.0;
            }
        }
    }

    @Override
    public void render(RenderContext ctx) {
        Point center = getCenter();
        if (target instanceof Entity e) {
            Point t = e.getCenter();
            ctx.submit(g -> { g.setColor(Color.GRAY); g.drawLine(center.x, center.y, t.x, t.y); });
            if (pendingItem != null) {
                int cx = (int) (center.x + (t.x - center.x) * progress);
                int cy = (int) (center.y + (t.y - center.y) * progress);
                ctx.submit(g -> { g.setColor(Color.YELLOW); g.fillOval(cx-8, cy-8, 16, 16); });
            }
        }
        ctx.submit(g -> {
            var b = getBoundingBox();
            g.setColor(Color.BLUE);
            g.fillRect(b.x(), b.y(), b.width(), b.height());
        });
    }
}