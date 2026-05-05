package org.mangorage.game.world.entity;

import org.mangorage.game.world.World;
import org.mangorage.game.world.pos.BoundingBox;
import org.mangorage.game.world.pos.Position;

public final class EntityType<T extends Entity> {

    public static <T extends Entity> EntityType<T> create(
            String name,
            int width,
            int height,
            IFactory<T> factory
    ) {
        return new EntityType<>(name, width, height, factory);
    }

    public static <T extends Entity> EntityType<T> create(
            String name,
            int width,
            int height
    ) {
        return new EntityType<>(name, width, height, null);
    }


    public interface IFactory<T extends Entity> {
        T create(World world, BoundingBox box);
    }

    public interface IBoxBuilder {
        void addPart(int offsetX, int offsetY, int width, int height);
    }

    public interface IBoxFactory {
        void build(IBoxBuilder builder, int width, int height);
    }

    private final String name;
    private final int width;
    private final int height;
    private final IFactory<T> factory;

    private IBoxFactory boxFactory = null;

    EntityType(String name, int width, int height, IFactory<T> factory) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.factory = factory;
    }

    // optional: attach shape builder
    public EntityType<T> withShape(IBoxFactory boxFactory) {
        this.boxFactory = boxFactory;
        return this;
    }

    public String getName() {
        return name;
    }

    public T create(World world, Position position) {

        BoundingBox box;

        if (boxFactory == null) {
            box = new BoundingBox(width, height);
        } else {
            box = new BoundingBox(width, height, (builder, w, h) -> boxFactory.build(builder::addPart, w, h));
        }

        T entity = factory.create(world, box);
        entity.setPosition(position);

        return entity;
    }
}