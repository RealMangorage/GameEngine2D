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
    public boolean acceptItem(Item item) {
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
        for (int i = 0; i < items.size(); i++) {
            MovingItem current = items.get(i);
            boolean canMove = (i == 0) || (items.get(i - 1).progress - current.progress >= itemSpacing);

            if (i == 0 && current.progress >= 1.0) {
                if (current.target.acceptItem(current.item)) {
                    items.remove(0);
                    i--;
                }
                continue;
            }

            if (canMove) current.progress = Math.min(1.0, current.progress + delta * moveSpeed);
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