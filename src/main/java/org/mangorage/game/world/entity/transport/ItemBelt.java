package org.mangorage.game.world.entity.transport;

import org.mangorage.game.render.RenderContext;
import org.mangorage.game.world.World;
import org.mangorage.game.world.entity.Entity;
import org.mangorage.game.world.pos.BoundingBox;
import org.mangorage.game.world.pos.Position;
import org.mangorage.game.world.pos.Facing;
import org.mangorage.game.world.resource.item.IItemReceiver;
import org.mangorage.game.world.resource.item.Item;
import org.mangorage.game.world.registeries.Entities;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class ItemBelt extends Entity implements IItemReceiver {

    private final List<MovingItem> items = new ArrayList<>();

    // use Entity-facing so rotation is universal

    private double beltSpeed = 0.9;
    private double spacing = 0.18;

    private static final int ITEM_RADIUS = 4;

    public ItemBelt(World world, BoundingBox box) {
        super(Entities.ITEM_BELT_ENTITY_TYPE, world, box);
    }

    @Override
    public boolean acceptItem(Item item, org.mangorage.game.world.pos.Position source) {
        // place item at projected position along belt based on source position
        Point start = getBeltStart();
        Point end = getBeltEnd();

        double dx = end.x - start.x;
        double dy = end.y - start.y;
        double len2 = dx * dx + dy * dy;

        double t = 0.0;
        if (len2 > 0.0001) {
            double sx = source.x() - start.x;
            double sy = source.y() - start.y;
            t = (sx * dx + sy * dy) / len2;
        }

        double desired = Math.max(0.0, Math.min(1.0, t));

        // ensure spacing: find insertion index
        if (items.isEmpty()) {
            items.add(new MovingItem(item, desired));
            return true;
        }
        // find index to keep items sorted DESCENDING by progress (front-most at index 0)
        int idx = 0;
        while (idx < items.size() && items.get(idx).progress > desired) idx++;

        // check previous (ahead)
        if (idx - 1 >= 0) {
            if (items.get(idx - 1).progress - desired < spacing) return false;
        }

        // check next (behind)
        if (idx < items.size()) {
            if (desired - items.get(idx).progress < spacing) return false;
        }

        items.add(idx, new MovingItem(item, desired));
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

            double proposed = Math.min(1.0, current.progress + dt * beltSpeed);

            if (i == 0) {
                // front-most moves freely up to proposed
                current.progress = proposed;

                if (current.progress >= 1.0) {
                    Entity target = getTargetReceiver();
                    Point end = getBeltEnd();
                    if (target instanceof IItemReceiver receiver) {
                        if (receiver.acceptItem(current.item, new Position(end.x, end.y, 0))) {
                            items.remove(0);
                            i--;
                        } else {
                            // hold at the end
                            current.progress = 1.0;
                        }
                    } else {
                        current.progress = 1.0;
                    }
                }
            } else {
                MovingItem prev = items.get(i - 1);
                double maxAllowed = prev.progress - spacing;
                double newProgress = Math.min(proposed, maxAllowed);
                if (newProgress > current.progress) {
                    current.progress = newProgress;
                }
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

        int w = (getFacing() == Facing.EAST || getFacing() == Facing.WEST) ? b.width() : b.height();
        int h = (getFacing() == Facing.EAST || getFacing() == Facing.WEST) ? b.height() : b.width();

        int probeX = pos.x() + w / 2;
        int probeY = pos.y() + h / 2;

        switch (getFacing()) {
            case EAST -> probeX = pos.x() + w + 1;
            case WEST -> probeX = pos.x() - 1;
            case SOUTH -> probeY = pos.y() + h + 1;
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
            int w = (getFacing() == Facing.EAST || getFacing() == Facing.WEST) ? box.width() : box.height();
            int h = (getFacing() == Facing.EAST || getFacing() == Facing.WEST) ? box.height() : box.width();
            g.fillRect(pos.x(), pos.y(), w, h);
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
            int w2 = (getFacing() == Facing.EAST || getFacing() == Facing.WEST) ? box.width() : box.height();
            int h2 = (getFacing() == Facing.EAST || getFacing() == Facing.WEST) ? box.height() : box.width();
            g.drawRect(pos.x(), pos.y(), w2, h2);
        });
    }

    // ----------------------------
    // ARROW
    // ----------------------------

    private void renderArrow(Graphics2D g, Position pos, BoundingBox b) {

        int w = (getFacing() == Facing.EAST || getFacing() == Facing.WEST) ? b.width() : b.height();
        int h = (getFacing() == Facing.EAST || getFacing() == Facing.WEST) ? b.height() : b.width();

        int cx = pos.x() + w / 2;
        int cy = pos.y() + h / 2;
        int size = Math.min(w, h) / 4;

        g.setColor(Color.LIGHT_GRAY);

        Polygon arrow = new Polygon();

        switch (getFacing()) {
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

        int w2 = (getFacing() == Facing.EAST || getFacing() == Facing.WEST) ? b.width() : b.height();
        int h2 = (getFacing() == Facing.EAST || getFacing() == Facing.WEST) ? b.height() : b.width();

        return switch (getFacing()) {
            case EAST -> new Point(pos.x() + ITEM_RADIUS, pos.y() + h2 / 2);
            case WEST -> new Point(pos.x() + w2 - ITEM_RADIUS, pos.y() + h2 / 2);
            case SOUTH -> new Point(pos.x() + w2 / 2, pos.y() + ITEM_RADIUS);
            case NORTH -> new Point(pos.x() + w2 / 2, pos.y() + h2 - ITEM_RADIUS);
        };
    }

    private Point getBeltEnd() {
        var b = getBoundingBox();
        Position pos = getPosition();

        int w3 = (getFacing() == Facing.EAST || getFacing() == Facing.WEST) ? b.width() : b.height();
        int h3 = (getFacing() == Facing.EAST || getFacing() == Facing.WEST) ? b.height() : b.width();

        return switch (getFacing()) {
            case EAST -> new Point(pos.x() + w3 - ITEM_RADIUS, pos.y() + h3 / 2);
            case WEST -> new Point(pos.x() + ITEM_RADIUS, pos.y() + h3 / 2);
            case SOUTH -> new Point(pos.x() + w3 / 2, pos.y() + h3 - ITEM_RADIUS);
            case NORTH -> new Point(pos.x() + w3 / 2, pos.y() + ITEM_RADIUS);
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

        MovingItem(Item item, double progress) {
            this.item = item;
            this.progress = progress;
        }
    }
}