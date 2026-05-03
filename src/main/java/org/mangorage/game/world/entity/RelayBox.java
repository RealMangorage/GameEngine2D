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

public final class RelayBox extends Entity implements IItemReceiver, INode {

    private static final double MOVE_SPEED = 1.0 / 225.0;
    private static final double ITEM_SPACING = 0.18;

    private IItemReceiver next;
    private final List<MovingItem> items = new ArrayList<>();

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

    @Override
    public void onClick(MouseButton mouseButton) {
        if (mouseButton == MouseButton.MIDDLE) {
            next = null;
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
        if (!canAcceptNewItem()) return false;

        items.add(new MovingItem(item));
        return true;
    }

    private boolean canAcceptNewItem() {
        if (items.isEmpty()) return true;

        MovingItem last = items.get(items.size() - 1);
        return last.progress >= ITEM_SPACING;
    }

    @Override
    public void update(double delta) {
        if (items.isEmpty()) return;

        for (int i = 0; i < items.size(); i++) {
            MovingItem current = items.get(i);

            boolean canMove = true;

            // Maintain spacing behind previous item
            if (i > 0) {
                MovingItem previous = items.get(i - 1);
                if ((previous.progress - current.progress) < ITEM_SPACING) {
                    canMove = false;
                }
            }

            // Front item attempts transfer
            if (i == 0 && current.progress >= 1.0) {
                if (next != null && next.acceptItem(current.item)) {
                    items.remove(0);
                    i--;
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

        if (next instanceof Entity e) {
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

            g.setColor(Color.DARK_GRAY);
            g.fillRect(b.x(), b.y(), b.width(), b.height());

            g.setColor(Color.LIGHT_GRAY);
            g.drawRect(b.x(), b.y(), b.width(), b.height());
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