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

public final class Spawner extends Entity implements IItemReceiver, INode {

    private static final double MOVE_SPEED = 1.0 / 215.0; // 5 sec end-to-end
    private static final double ITEM_SPACING = 0.18;    // spacing between items

    private IItemReceiver target;

    private final List<MovingItem> items = new ArrayList<>();

    public Spawner(World world, BoundingBox box) {
        super(Entities.SPAWNER_ENTITY_TYPE, world, box);
    }

    @Override
    public int getMaxInputs() {
        return 0;
    }

    @Override
    public int getMaxOutputs() {
        return 1;
    }

    @Override
    public int getInputCount() {
        return 0;
    }

    @Override
    public int getOutputCount() {
        return target == null ? 0 : 1;
    }

    @Override
    public void onClick(MouseButton mouseButton) {
        if (mouseButton == MouseButton.MIDDLE) {
            target = null;
            return;
        }

        // Spawn new item only if entrance has room
        if (canSpawnNewItem()) {
            items.add(new MovingItem(new Item("MangoItem")));
        }
    }

    private boolean canSpawnNewItem() {
        if (items.isEmpty()) return true;

        MovingItem last = items.get(items.size() - 1);
        return last.progress >= ITEM_SPACING;
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

    @Override
    public boolean acceptItem(Item item) {
        return false;
    }

    @Override
    public void update(double delta) {
        if (items.isEmpty()) return;

        // Update front -> back so each item knows if it can move
        for (int i = 0; i < items.size(); i++) {
            MovingItem current = items.get(i);

            boolean canMove = true;

            if (i > 0) {
                MovingItem previous = items.get(i - 1);

                // maintain spacing behind previous item
                if ((previous.progress - current.progress) < ITEM_SPACING) {
                    canMove = false;
                }
            }

            if (i == 0 && current.progress >= 1.0) {
                // front item tries to enter target
                if (target != null && target.acceptItem(current.item)) {
                    items.remove(0);
                    i--; // adjust index after removal
                }
                continue;
            }

            if (canMove) {
                current.progress += delta * MOVE_SPEED;
                if (current.progress > 1.0) {
                    current.progress = 1.0;
                }
            }
        }
    }

    @Override
    public void render(RenderContext ctx) {
        Point center = getCenter();

        Point targetPoint = null;

        if (target instanceof Entity e) {
            targetPoint = e.getCenter();
            Point finalTargetPoint = targetPoint;

            ctx.submit(g -> {
                g.setColor(Color.GRAY);
                g.drawLine(center.x, center.y, finalTargetPoint.x, finalTargetPoint.y);
            });
        }

        Point finalTargetPoint1 = targetPoint;
        for (MovingItem moving : items) {
            if (finalTargetPoint1 == null) break;

            int cx = (int) (center.x + (finalTargetPoint1.x - center.x) * moving.progress);
            int cy = (int) (center.y + (finalTargetPoint1.y - center.y) * moving.progress);

            ctx.submit(g -> {
                g.setColor(Color.YELLOW);
                g.fillOval(cx - 8, cy - 8, 16, 16);
            });
        }

        ctx.submit(g -> {
            var b = getBoundingBox();
            g.setColor(Color.BLUE);
            g.fillRect(b.x(), b.y(), b.width(), b.height());
        });
    }

    private static final class MovingItem {
        final Item item;
        double progress = 0.0;

        MovingItem(Item item) {
            this.item = item;
        }
    }
}