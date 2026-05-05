package org.mangorage.game.world.entity;

import org.mangorage.game.input.MouseButton;
import org.mangorage.game.client.render.core.RenderContext;
import org.mangorage.game.world.World;
import org.mangorage.game.world.pos.BoundingBox;
import org.mangorage.game.world.pos.Position;
import org.mangorage.game.world.pos.Facing;

import java.awt.*;

public abstract class Entity {

    private final EntityType<?> entityType;
    private final World world;

    private Position position;
    private final BoundingBox boundingBox;
    private Facing facing = Facing.EAST;

    public Entity(EntityType<?> entityType, World world, BoundingBox boundingBox) {
        this.entityType = entityType;
        this.world = world;
        this.boundingBox = boundingBox;
    }

    public EntityType<?> getType() {
        return entityType;
    }

    public World getWorld() {
        return world;
    }

    // ----------------------------
    // POSITION
    // ----------------------------

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position p) {
        this.position = p;
    }

    // ----------------------------
    // BOUNDING BOX (LOCAL SPACE)
    // ----------------------------

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    // ----------------------------
    // ORIENTATION
    // ----------------------------

    public Facing getFacing() {
        return facing;
    }

    public void setFacing(Facing facing) {
        this.facing = facing;
    }

    public void rotate() {
        // preserve visual center when rotating (swap extents as necessary)
        Point center = getCenter();

        this.facing = this.facing.next();

        if (position != null) {
            int newW = (facing == Facing.EAST || facing == Facing.WEST) ? boundingBox.width() : boundingBox.height();
            int newH = (facing == Facing.EAST || facing == Facing.WEST) ? boundingBox.height() : boundingBox.width();

            int newX = center.x - newW / 2;
            int newY = center.y - newH / 2;

            this.position = new Position(newX, newY, position.z());
        }
    }

    // ----------------------------
    // CENTER (FIXED)
    // ----------------------------

    public Point getCenter() {
        if (position == null) {
            return new Point(0, 0);
        }
        int x = position.x();
        int y = position.y();

        int w = (facing == Facing.EAST || facing == Facing.WEST) ? boundingBox.width() : boundingBox.height();
        int h = (facing == Facing.EAST || facing == Facing.WEST) ? boundingBox.height() : boundingBox.width();

        return new Point(
                x + w / 2,
                y + h / 2
        );
    }

    // ----------------------------
    // EVENTS
    // ----------------------------

    public void onClick(MouseButton mouseButton) {}

    public void onClickWithSelected(Entity selected, Entity clicked) {}

    public void update(double delta) {}

    public void render(RenderContext ctx) {}
}