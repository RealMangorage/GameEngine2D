package org.mangorage.game.world.entity;

import org.mangorage.game.input.MouseButton;
import org.mangorage.game.render.RenderContext;
import org.mangorage.game.world.misc.BoundingBox;
import org.mangorage.game.world.misc.Item;
import org.mangorage.game.world.World;
import org.mangorage.game.world.registeries.Entities;

import java.awt.*;

public final class Spawner extends Entity implements IItemReceiver {
    private IItemReceiver target;

    private Item pendingItem;
    private double progress = 0.0;

    // Consistent with Wire: 1.0 / 5.0 seconds
    private final double speed = 1.0 / 5.0;

    public Spawner(World world, BoundingBox box) {
        super(Entities.SPAWNER_ENTITY_TYPE, world, box);
    }

    public void attachTo(IItemReceiver target) {
        this.target = target;
        // If the target is a Wire, tell it that this Spawner is its source
        if (target instanceof Wire wire) {
            wire.setSource(this);
        }
    }

    @Override
    public void onClick(Entity selected, MouseButton mouseButton) {
        if (selected != this && selected instanceof IItemReceiver iItemReceiver) {
            attachTo(iItemReceiver);
            return;
        }

        // Only spawn if we aren't already transporting an item
        if (pendingItem == null) {
            pendingItem = new Item("MangoItem");
            progress = 0.0;
        }
    }

    @Override
    public void update(double delta) {

        // Move the item toward the target
        if (progress < 1.0) {
            progress += delta * speed;
        }

        // Hand off logic
        if (progress >= 1.0) {
            progress = 1.0;
            if (target != null && target.acceptItem(pendingItem)) {
                pendingItem = null;
                progress = 0.0;
            }
        }
    }

    @Override
    public void render(RenderContext ctx) {
        final Point center = getCenter();
        final int cx = center.x, cy = center.y;

        if (target instanceof Entity targetEntity) {
            Point targetPoint = targetEntity.getCenter();
            final int tx = targetPoint.x, ty = targetPoint.y;

            // LAYER 0: Bottom
            ctx.submit(g -> {
                g.setColor(Color.GREEN);
                g.setStroke(new BasicStroke(2));
                g.drawLine(cx, cy, tx, ty);
            });

            if (pendingItem != null) {
                final int curX = (int) (cx + (tx - cx) * progress);
                final int curY = (int) (cy + (ty - cy) * progress);
                // LAYER 1: Middle
                ctx.submit(g -> {
                    g.setColor(Color.GREEN);
                    g.fillOval(curX - 8, curY - 8, 16, 16);
                });
            }
        }

        // LAYER 2: Top
        var box = getBoundingBox();
        ctx.submit(g -> {
            g.setColor(Color.BLACK); // Solid background to hide the line
            g.fillRect(box.x(), box.y(), box.width(), box.height());
            g.setColor(Color.GREEN);
            g.drawRect(box.x(), box.y(), box.width(), box.height());
        });
    }

    @Override
    public boolean acceptItem(Item item) {
        return false;
    }
}