package org.mangorage.game.world.entity;

import org.mangorage.game.input.MouseButton;
import org.mangorage.game.render.RenderContext;
import org.mangorage.game.world.World;
import org.mangorage.game.world.misc.BoundingBox;
import org.mangorage.game.world.misc.INode;
import org.mangorage.game.world.misc.Item;
import org.mangorage.game.world.registeries.Entities;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class Splitter extends Entity implements IItemReceiver, INode {
    private final List<IItemReceiver> outputs = new ArrayList<>();
    private int nextOutputIndex = 0;
    private Item heldItem;
    private double progress = 0.0;

    public Splitter(World world, BoundingBox box) {
        super(Entities.SPLITTER_ENTITY_TYPE, world, box);
    }

    @Override public int getMaxInputs() { return 1; }
    @Override public int getMaxOutputs() { return 4; }
    @Override public int getInputCount() { return 0; }
    @Override public int getOutputCount() { return outputs.size(); }

    public void onClick(MouseButton mouseButton) {
        if (mouseButton == MouseButton.MIDDLE) {
            outputs.clear();
            nextOutputIndex = 0;
        }
    }

    @Override
    public void onClickWithSelected(Entity selected, Entity clicked) {
        if (selected == this && clicked instanceof INode target && clicked != this) {
            if (this.canAddMoreOutputs() && target.canAcceptMoreInputs()) {
                this.connect(target);
            }
        }
    }

    @Override
    public boolean connect(INode target) {
        if (target instanceof IItemReceiver receiver && !outputs.contains(receiver)) {
            outputs.add(receiver);
            return true;
        }
        return false;
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
        if (heldItem == null || outputs.isEmpty()) return;
        progress += delta * (1.0 / 3.0);
        if (progress >= 1.0) {
            IItemReceiver target = outputs.get(nextOutputIndex);
            if (target.acceptItem(heldItem)) {
                heldItem = null;
                nextOutputIndex = (nextOutputIndex + 1) % outputs.size();
                progress = 0.0;
            } else {
                progress = 1.0;
            }
        }
    }

    @Override
    public void render(RenderContext ctx) {
        Point center = getCenter();
        for (IItemReceiver out : outputs) {
            if (out instanceof Entity e) {
                Point t = e.getCenter();
                ctx.submit(g -> { g.setColor(Color.GRAY); g.drawLine(center.x, center.y, t.x, t.y); });
            }
        }
        if (heldItem != null && !outputs.isEmpty()) {
            if (outputs.get(nextOutputIndex) instanceof Entity e) {
                Point t = e.getCenter();
                int cx = (int) (center.x + (t.x - center.x) * progress);
                int cy = (int) (center.y + (t.y - center.y) * progress);
                ctx.submit(g -> { g.setColor(Color.ORANGE); g.fillOval(cx-6, cy-6, 12, 12); });
            }
        }
        ctx.submit(g -> {
            var b = getBoundingBox();
            g.setColor(new Color(60, 60, 100));
            g.fillRect(b.x(), b.y(), b.width(), b.height());
            g.setColor(Color.WHITE);
            g.drawRect(b.x(), b.y(), b.width(), b.height());
            g.drawString("S", b.x() + 4, b.y() + 12);
        });
    }
}