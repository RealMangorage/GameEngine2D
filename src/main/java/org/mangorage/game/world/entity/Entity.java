package org.mangorage.game.world.entity;

import org.mangorage.game.input.MouseButton;
import org.mangorage.game.render.RenderContext;
import org.mangorage.game.world.pos.BoundingBox;
import org.mangorage.game.world.pos.Position;
import org.mangorage.game.world.World;

import java.awt.*;
import java.util.List;

public abstract class Entity {

    private final EntityType<?> entityType;
    private final World world;

    // Entity position (in world coordinates). Z is the render layer.
    private Position position;

    // The bounding box instance for this entity. It may contain parts (relative to the box's location).
    private BoundingBox boundingBox;

    public Entity(EntityType<?> entityType, World world, BoundingBox boundingBox) {
        this.entityType = entityType;
        this.world = world;

        // Initialize position from the provided bounding box location (z defaults to 0)
        this.position = new Position(boundingBox.x(), boundingBox.y(), 0);
        // Store bounding box instance. It may later be modified by subclasses (add/clear parts).
        this.boundingBox = boundingBox;
    }

    public EntityType<?> getType() {
        return entityType;
    }

    public World getWorld() {
        return world;
    }

    public Point getCenter() {
        var b = getBoundingBox();
        return new Point(
                b.x() + (b.width() / 2),
                b.y() + (b.height() / 2)
        );
    }

    public void onClick(MouseButton mouseButton) {

    }

    public void onClickWithSelected(Entity selected, Entity clicked) {
    }


    public void update(double delta) {

    }

    public void render(RenderContext ctx) {}

    // Get the entity world position
    public Position getPosition() { 
        return position; 
    }

    // Set the entity position; bounding box parts are relative and remain unchanged. When position is set
    // the computed absolute bounding box will reflect the new position.
    public void setPosition(Position p) { 
        this.position = p; 
    }

    // Return the overall axis-aligned bounding box. Delegates to the boundingBox instance.
    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    // Note: bounding-box operations (parts, containment) are handled by the BoundingBox class.
}
