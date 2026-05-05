package org.mangorage.game.world.entity;

import org.mangorage.game.input.MouseButton;
import org.mangorage.game.render.RenderContext;
import org.mangorage.game.world.World;
import org.mangorage.game.world.pos.BoundingBox;
import org.mangorage.game.world.pos.Position;

import java.awt.*;

public abstract class Entity {

    private final EntityType<?> entityType;
    private final World world;

    private Position position;
    private final BoundingBox boundingBox;

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
    // CENTER (FIXED)
    // ----------------------------

    public Point getCenter() {
        if (position == null) {
            return new Point(0, 0);
        }

        int x = position.x();
        int y = position.y();

        return new Point(
                x + boundingBox.width() / 2,
                y + boundingBox.height() / 2
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