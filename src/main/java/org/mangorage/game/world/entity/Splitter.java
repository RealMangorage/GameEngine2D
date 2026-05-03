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

    private static final double MOVE_SPEED = 1.0 / 23.0;
    private static final double ITEM_SPACING = 0.18;

    private final List<IItemReceiver> outputs = new ArrayList<>();
    private final List<MovingItem> items = new ArrayList<>();

    private int nextOutputIndex = 0;

    public Splitter(World world, BoundingBox box) {
        super(Entities.SPLITTER_ENTITY_TYPE, world, box);
    }

    @Override
    public int getMaxInputs() {
        return 1;
    }

    @Override
    public int getMaxOutputs() {
        return 4;
    }

    @Override
    public int getInputCount() {
        return 0;
    }

    @Override
    public int getOutputCount() {
        return outputs.size();
    }

    @Override
    public void onClick(MouseButton mouseButton) {
        if (mouseButton == MouseButton.MIDDLE) {
            outputs.clear();
            items.clear();
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
        if (outputs.isEmpty()) return false;
        if (!canAcceptNewItem()) return false;

        IItemReceiver assignedTarget = outputs.get(nextOutputIndex);

        items.add(new MovingItem(item, assignedTarget));

        nextOutputIndex = (nextOutputIndex + 1) % outputs.size();
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

            // spacing check
            if (i > 0) {
                MovingItem previous = items.get(i - 1);
                if ((previous.progress - current.progress) < ITEM_SPACING) {
                    canMove = false;
                }
            }

            // front item tries delivery
            if (i == 0 && current.progress >= 1.0) {
                if (current.target.acceptItem(current.item)) {
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

        // Render all output connections
        for (IItemReceiver out : outputs) {
            if (out instanceof Entity e) {
                Point t = e.getCenter();

                ctx.submit(g -> {
                    g.setColor(Color.GRAY);
                    g.drawLine(center.x, center.y, t.x, t.y);
                });
            }
        }

        // Render all moving items
        for (MovingItem moving : items) {
            if (!(moving.target instanceof Entity e)) continue;

            Point t = e.getCenter();

            int cx = (int) (center.x + (t.x - center.x) * moving.progress);
            int cy = (int) (center.y + (t.y - center.y) * moving.progress);

            ctx.submit(g -> {
                g.setColor(Color.ORANGE);
                g.fillOval(cx - 6, cy - 6, 12, 12);
            });
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

    private static final class MovingItem {
        final Item item;
        final IItemReceiver target;
        double progress = 0.0;

        MovingItem(Item item, IItemReceiver target) {
            this.item = item;
            this.target = target;
        }
    }
}