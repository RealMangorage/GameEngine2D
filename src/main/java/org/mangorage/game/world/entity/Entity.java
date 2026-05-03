package org.mangorage.game.world.entity;

import org.mangorage.game.input.MouseButton;
import org.mangorage.game.render.RenderContext;
import org.mangorage.game.world.misc.BoundingBox;
import org.mangorage.game.world.World;

import java.awt.*;

public abstract class Entity {

    private final EntityType<?> entityType;
    private final BoundingBox boundingBox;
    private final World world;

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

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public Point getCenter() {
        return new Point(
                boundingBox.x() + (boundingBox.width() / 2),
                boundingBox.y() + (boundingBox.height() / 2)
        );
    }

    /**
     *
     * @param selected -> Selected Entity from player
     */
    public void onClick(Entity selected, MouseButton mouseButton) {

    }


    public void update(double delta) {

    }

    public void render(RenderContext ctx) {}
}
