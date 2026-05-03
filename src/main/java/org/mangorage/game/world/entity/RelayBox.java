package org.mangorage.game.world.entity;

import org.mangorage.game.input.MouseButton;
import org.mangorage.game.render.RenderContext;
import org.mangorage.game.world.misc.BoundingBox;
import org.mangorage.game.world.misc.INode;
import org.mangorage.game.world.misc.Item;
import org.mangorage.game.world.World;
import org.mangorage.game.world.registeries.Entities;
import java.awt.*;

public final class RelayBox extends Entity implements IItemReceiver, INode {
    private IItemReceiver next;
    private Item heldItem;
    private double progress = 0.0;
    private final double travelDuration = 5.0;

    private boolean hasInputFilled = false;

    public RelayBox(World world, BoundingBox box) {
        super(Entities.RELAY_BOX_ENTITY_TYPE, world, box);
    }

    @Override
    public int getMaxInputs() {
        return 1;
    }

    @Override
    public int getMaxOutputs() {
        return 1;
    }

    @Override
    public int getInputCount() {
        return hasInputFilled ? 1 : 0;
    }

    @Override
    public int getOutputCount() {
        return next == null ? 0 : 1;
    }

    public void onClick(MouseButton mouseButton) {
        if (mouseButton == MouseButton.MIDDLE) {
            next = null;
        }
    }

    @Override
    public void onClickWithSelected(Entity selected, Entity clicked) {
        // If I am selected and I click something else, try to send to it
        if (selected == this && clicked instanceof INode target && clicked != this) {
            if (this.canAddMoreOutputs() && target.canAcceptMoreInputs()) {
                this.connect(target);
            }
        }
    }

    @Override
    public boolean connect(INode target) {
        if (target instanceof IItemReceiver receiver) {
            this.next = receiver;
            return true;
        }
        return false;
    }

    @Override
    public void registerInput() {
        hasInputFilled = true;
    }

    @Override
    public boolean acceptItem(Item item) {
        if (heldItem != null) return false;
        heldItem = item;
        progress = 0.0;
        return true;
    }

    @Override
    public void update(double delta) {
        if (heldItem == null) return;
        progress += delta * (1.0 / travelDuration);
        if (progress >= 1.0) {
            if (next != null && next.acceptItem(heldItem)) {
                heldItem = null;
                progress = 0.0;
            } else {
                progress = 1.0;
            }
        }
    }

    @Override
    public void render(RenderContext ctx) {
        Point center = getCenter();
        if (next instanceof Entity e) {
            Point t = e.getCenter();
            ctx.submit(g -> {
                g.setColor(Color.GRAY);
                g.drawLine(center.x, center.y, t.x, t.y);
            });
            if (heldItem != null) {
                int cx = (int) (center.x + (t.x - center.x) * progress);
                int cy = (int) (center.y + (t.y - center.y) * progress);
                ctx.submit(g -> { g.setColor(Color.YELLOW); g.fillOval(cx-8, cy-8, 16, 16); });
            }
        }
        ctx.submit(g -> {
            var b = getBoundingBox();
            g.setColor(Color.DARK_GRAY);
            g.fillRect(b.x(), b.y(), b.width(), b.height());
            g.setColor(Color.LIGHT_GRAY);
            g.drawRect(b.x(), b.y(), b.width(), b.height());
        });
    }
}