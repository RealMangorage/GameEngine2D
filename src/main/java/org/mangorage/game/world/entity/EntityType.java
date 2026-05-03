package org.mangorage.game.world.entity;

import org.mangorage.game.world.pos.BoundingBox;
import org.mangorage.game.world.pos.Location;
import org.mangorage.game.world.World;

public final class EntityType<T extends Entity> {

    public static <T extends Entity> EntityType<T> create(String name, IEntitySupplier<T> supplier, int width, int height) {
        return new EntityType<>(name, supplier, width, height);
    }

    private final String name;
    private final IEntitySupplier<T> supplier;
    private int width, height;

    EntityType(String name, IEntitySupplier<T> supplier, int width, int height) {
        this.name = name;
        this.supplier = supplier;
        this.width = width;
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public T create(World world, Location location) {
        return supplier.get(world, location.of(width, height));
    }

    public interface IEntitySupplier<T extends Entity> {
        T get(World world, BoundingBox boundingBox);
    }
}
