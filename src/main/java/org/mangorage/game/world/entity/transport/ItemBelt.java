package org.mangorage.game.world.entity.transport;

import org.mangorage.game.render.RenderContext;
import org.mangorage.game.world.World;
import org.mangorage.game.world.entity.Entity;
import org.mangorage.game.world.pos.BoundingBox;
import org.mangorage.game.world.pos.Facing;
import org.mangorage.game.world.pos.Position;
import org.mangorage.game.world.resource.item.IItemReceiver;
import org.mangorage.game.world.resource.item.Item;
import org.mangorage.game.world.registeries.Entities;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class ItemBelt extends Entity implements IItemReceiver {

    private final List<MovingItem> items = new ArrayList<>();

    private Facing facing = Facing.EAST;

    private double beltSpeed = 0.9;
    private double spacing = 0.18;

    private static final int ITEM_RADIUS = 4;

    public ItemBelt(World world, BoundingBox box) {
        super(Entities.ITEM_BELT_ENTITY_TYPE, world, box);
    }

    @Override
    public boolean acceptItem(Item item) {
        if (!canAcceptNewItem()) return false;

        items.add(new MovingItem(item));
        return true;
    }

    private boolean canAcceptNewItem() {
        return items.isEmpty() || items.get(items.size() - 1).progress >= spacing;
    }

    @Override
    public void update(double delta) {
        if (items.isEmpty()) return;

        double dt = delta / 1000.0;

        for (int i = 0; i < items.size(); i++) {
            MovingItem current = items.get(i);

            boolean canMove =
                    i == 0 ||
                            items.get(i - 1).progress - current.progress >= spacing;

            if (i == 0 && current.progress >= 1.0) {
                Entity target = getTargetReceiver();

                if (target instanceof IItemReceiver receiver) {
                    if (receiver.acceptItem(current.item)) {
                        items.remove(0);
                        i--;
                    }
                }
                continue;
            }

            if (canMove) {
                current.progress = Math.min(1.0, current.progress + dt * beltSpeed);
            }
        }
    }

    // ----------------------------
    // WORLD HELPERS
    // ----------------------------

    private int worldX(int localX) {
        Position p = getPosition();
        return p.x() + localX;
    }

    private int worldY(int localY) {
        Position p = getPosition();
        return p.y() + localY;
    }

    // ----------------------------
    // TARGET DETECTION
    // ----------------------------

    private Entity getTargetReceiver() {
        var b = getBoundingBox();
        Position pos = getPosition();

        int probeX = pos.x() + b.width() / 2;
        int probeY = pos.y() + b.height() / 2;

        switch (facing) {
            case EAST -> probeX = pos.x() + b.width() + 1;
            case WEST -> probeX = pos.x() - 1;
            case SOUTH -> probeY = pos.y() + b.height() + 1;
            case NORTH -> probeY = pos.y() - 1;
        }

        return getWorld().getEntityAt(probeX, probeY);
    }

    // ----------------------------
    // RENDERING
    // ----------------------------

    @Override
    public void render(RenderContext ctx) {

        var box = getBoundingBox();
        Position pos = getPosition();

        ctx.submit(g -> {
            g.setColor(new Color(70, 70, 70));
            g.fillRect(pos.x(), pos.y(), box.width(), box.height());
        });

        ctx.submit(g -> renderArrow(g, pos, box));

        Point start = getBeltStart();
        Point end = getBeltEnd();

        for (MovingItem moving : items) {
            int cx = (int) (start.x + (end.x - start.x) * moving.progress);
            int cy = (int) (start.y + (end.y - start.y) * moving.progress);

            ctx.submit(g -> {
                g.setColor(Color.ORANGE);
                g.fillOval(
                        cx - ITEM_RADIUS,
                        cy - ITEM_RADIUS,
                        ITEM_RADIUS * 2,
                        ITEM_RADIUS * 2
                );
            });
        }

        ctx.submit(g -> {
            g.setColor(Color.WHITE);
            g.drawRect(pos.x(), pos.y(), box.width(), box.height());
        });
    }

    // ----------------------------
    // ARROW
    // ----------------------------

    private void renderArrow(Graphics2D g, Position pos, BoundingBox b) {

        int cx = pos.x() + b.width() / 2;
        int cy = pos.y() + b.height() / 2;
        int size = Math.min(b.width(), b.height()) / 4;

        g.setColor(Color.LIGHT_GRAY);

        Polygon arrow = new Polygon();

        switch (facing) {
            case EAST -> {
                arrow.addPoint(cx - size, cy - size);
                arrow.addPoint(cx - size, cy + size);
                arrow.addPoint(cx + size, cy);
            }
            case WEST -> {
                arrow.addPoint(cx + size, cy - size);
                arrow.addPoint(cx + size, cy + size);
                arrow.addPoint(cx - size, cy);
            }
            case NORTH -> {
                arrow.addPoint(cx - size, cy + size);
                arrow.addPoint(cx + size, cy + size);
                arrow.addPoint(cx, cy - size);
            }
            case SOUTH -> {
                arrow.addPoint(cx - size, cy - size);
                arrow.addPoint(cx + size, cy - size);
                arrow.addPoint(cx, cy + size);
            }
        }

        g.fillPolygon(arrow);
    }

    // ----------------------------
    // BELT ENDPOINTS (FIXED)
    // ----------------------------

    private Point getBeltStart() {
        var b = getBoundingBox();
        Position pos = getPosition();

        return switch (facing) {
            case EAST -> new Point(pos.x() + ITEM_RADIUS, pos.y() + b.height() / 2);
            case WEST -> new Point(pos.x() + b.width() - ITEM_RADIUS, pos.y() + b.height() / 2);
            case SOUTH -> new Point(pos.x() + b.width() / 2, pos.y() + ITEM_RADIUS);
            case NORTH -> new Point(pos.x() + b.width() / 2, pos.y() + b.height() - ITEM_RADIUS);
        };
    }

    private Point getBeltEnd() {
        var b = getBoundingBox();
        Position pos = getPosition();

        return switch (facing) {
            case EAST -> new Point(pos.x() + b.width() - ITEM_RADIUS, pos.y() + b.height() / 2);
            case WEST -> new Point(pos.x() + ITEM_RADIUS, pos.y() + b.height() / 2);
            case SOUTH -> new Point(pos.x() + b.width() / 2, pos.y() + b.height() - ITEM_RADIUS);
            case NORTH -> new Point(pos.x() + b.width() / 2, pos.y() + ITEM_RADIUS);
        };
    }

    // ----------------------------
    // DATA
    // ----------------------------

    private static final class MovingItem {
        final Item item;
        double progress = 0.0;

        MovingItem(Item item) {
            this.item = item;
        }
    }
}