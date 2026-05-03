package org.mangorage.game.world.entity;

import org.mangorage.game.input.MouseButton;
import org.mangorage.game.render.RenderContext;
import org.mangorage.game.world.misc.BoundingBox;
import org.mangorage.game.world.misc.Item;
import org.mangorage.game.world.World;
import org.mangorage.game.world.registeries.Entities;

import java.awt.*;

public final class Wire extends Entity implements IItemReceiver {
    // Connection tracking
    private IItemReceiver source; // Where items come from
    private IItemReceiver next;   // Where items go

    private Item heldItem;
    private double progress = 0.0;
    private final double travelDuration = 5.0; // Seconds

    public Wire(World world, BoundingBox box) {
        super(Entities.WIRE_ENTITY_TYPE, world, box);
    }

    // Standard setters for connectivity
    public void setSource(IItemReceiver source) {
        this.source = source;
    }

    public void setNext(IItemReceiver next) {
        this.next = next;
    }

    public IItemReceiver getSource() {
        return source;
    }

    public IItemReceiver getNext() {
        return next;
    }

    @Override
    public void onClick(Entity selected, MouseButton mouseButton) {

        if (mouseButton == MouseButton.MIDDLE) {
            setSource(null);
            setNext(null);
        }

        if (selected != this && selected instanceof IItemReceiver iItemReceiver) {

            if (mouseButton == MouseButton.LEFT) {
                setSource(iItemReceiver);
            }

            if (mouseButton == MouseButton.RIGHT) {
                setNext(iItemReceiver);
            }

            setSource(iItemReceiver);
        }
    }

    @Override
    public boolean acceptItem(Item item) {
        if (this.heldItem == null) {
            this.heldItem = item;
            this.progress = 0.0;
            return true;
        }
        return false;
    }

    @Override
    public void update(double delta) {
        if (heldItem == null) return;

        if (progress < 1.0) {
            double speed = 1.0 / travelDuration;
            progress += delta * speed;
        }

        if (progress >= 1.0) {
            progress = 1.0;
            if (next != null && next.acceptItem(heldItem)) {
                heldItem = null;
                progress = 0.0;
            }
        }
    }

    @Override
    public void render(RenderContext ctx) {
        Point center = getCenter();

        // LAYER 0: Connection Line (Lowest)
        if (next instanceof Entity nextEntity) {
            Point target = nextEntity.getCenter();
            ctx.submit(g -> {
                g.setColor(Color.GRAY);
                g.setStroke(new BasicStroke(2));
                g.drawLine(center.x, center.y, target.x, target.y);
            });

            // LAYER 1: Traveling Item (Middle)
            ctx.push();
            if (heldItem != null) {
                int curX = (int) (center.x + (target.x - center.x) * progress);
                int curY = (int) (center.y + (target.y - center.y) * progress);
                ctx.submit(g -> {
                    g.setColor(Color.YELLOW);
                    g.fillOval(curX - 8, curY - 8, 16, 16);
                });
            }
            ctx.pop(); // Reset back to Layer 0
        }

        // LAYER 2: Wire Node (Top)
        // We push to 2 manually here to ensure it's ALWAYS on top

        ctx.push();
        ctx.submit(g -> {
            var box = getBoundingBox();
            g.setColor(Color.DARK_GRAY);
            g.fillRect(box.x(), box.y(), box.width(), box.height());
            g.setColor(Color.LIGHT_GRAY);
            g.drawRect(box.x(), box.y(), box.width(), box.height());
        });
        ctx.pop();
    }
}
