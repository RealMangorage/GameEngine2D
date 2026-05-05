package org.mangorage.game.world.entity.io;

import org.mangorage.game.render.RenderContext;
import org.mangorage.game.world.World;
import org.mangorage.game.world.entity.Entity;
import org.mangorage.game.world.entity.EntityType;
import org.mangorage.game.world.resource.item.IItemReceiver;
import org.mangorage.game.world.pos.BoundingBox;
import org.mangorage.game.world.misc.INode;
import org.mangorage.game.world.resource.item.Item;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class EntityIO extends Entity implements IItemReceiver, INode {
    protected final List<IItemReceiver> outputs = new ArrayList<>();
    protected final List<MovingItem> items = new ArrayList<>();

    private final int maxInputs;
    private final int maxOutputs;
    private final double moveSpeed;
    private final double itemSpacing;
    private int nextOutputIndex = 0;
    private int inputCount = 0;

    public EntityIO(EntityType<?> type, World world, BoundingBox box, int maxIn, int maxOut, double speed, double spacing) {
        super(type, world, box);
        this.maxInputs = maxIn;
        this.maxOutputs = maxOut;
        this.moveSpeed = speed;
        this.itemSpacing = spacing;
    }

    // Shared Node Logic
    @Override public int getMaxInputs() { return maxInputs; }
    @Override public int getMaxOutputs() { return maxOutputs; }
    @Override public int getInputCount() { return inputCount; }
    @Override public int getOutputCount() { return outputs.size(); }
    @Override public void registerInput() { if (inputCount < maxInputs) inputCount++; }

    @Override
    public boolean connect(INode target) {
        if (target instanceof IItemReceiver receiver && outputs.size() < maxOutputs) {
            if (!outputs.contains(receiver)) {
                outputs.add(receiver);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClickWithSelected(Entity selected, Entity clicked) {
        if (selected == this && clicked instanceof INode target && clicked != this) {
            if (outputs.size() < maxOutputs && target.canAcceptMoreInputs()) {
                if (this.connect(target)) target.registerInput();
            }
        }
    }

    @Override
    public boolean acceptItem(Item item, org.mangorage.game.world.pos.Position source) {
        if (outputs.isEmpty() || !canAcceptNewItem()) return false;
        items.add(new MovingItem(item, outputs.get(nextOutputIndex)));
        nextOutputIndex = (nextOutputIndex + 1) % outputs.size();
        return true;
    }

    protected boolean canAcceptNewItem() {
        return items.isEmpty() || items.get(items.size() - 1).progress >= itemSpacing;
    }

    @Override
    public void update(double delta) {
        double dt = delta / 1000.0;
        for (int i = 0; i < items.size(); i++) {
            MovingItem current = items.get(i);

            double proposed = Math.min(1.0, current.progress + dt * moveSpeed);

            if (i == 0) {
                current.progress = proposed;

                if (current.progress >= 1.0) {
                    // pass this entity's center as the source position for the receiver
                    Point c = getCenter();
                    if (current.target.acceptItem(current.item, new org.mangorage.game.world.pos.Position(c.x, c.y, 0))) {
                        items.remove(0);
                        i--;
                    } else {
                        current.progress = 1.0;
                    }
                }
            } else {
                MovingItem prev = items.get(i - 1);
                double maxAllowed = prev.progress - itemSpacing;
                double newProgress = Math.min(proposed, maxAllowed);
                if (newProgress > current.progress) {
                    current.progress = newProgress;
                }
            }
        }
    }

    protected void renderConnectionsAndItems(RenderContext ctx, Color pathColor, Color itemColor, int size) {
        Point center = getCenter();

        // Lines
        ctx.pop();
        for (IItemReceiver out : outputs) {
            if (out instanceof Entity e) {
                Point t = e.getCenter();
                ctx.submit(g -> { g.setColor(pathColor); g.drawLine(center.x, center.y, t.x, t.y); });
            }
        }
        ctx.push();

        // Items
        for (MovingItem moving : items) {
            if (moving.target instanceof Entity e) {
                Point t = e.getCenter();
                int cx = (int) (center.x + (t.x - center.x) * moving.progress);
                int cy = (int) (center.y + (t.y - center.y) * moving.progress);
                ctx.submit(g -> { g.setColor(itemColor); g.fillOval(cx - (size/2), cy - (size/2), size, size); });
            }
        }
    }

    protected static class MovingItem {
        final Item item;
        final IItemReceiver target;
        double progress = 0.0;
        MovingItem(Item item, IItemReceiver target) { this.item = item; this.target = target; }
    }
}