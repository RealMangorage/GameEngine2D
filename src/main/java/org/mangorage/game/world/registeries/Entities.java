package org.mangorage.game.world.registeries;

import org.mangorage.game.world.entity.Entity;
import org.mangorage.game.world.entity.EntityType;
import org.mangorage.game.world.entity.Spawner;
import org.mangorage.game.world.entity.Trash;
import org.mangorage.game.world.entity.Wire;

import java.util.ArrayList;
import java.util.List;

public final class Entities {
    public static final List<EntityType<?>> ENTITY_TYPES = new ArrayList<>();

    public static <T extends Entity> EntityType<T> registerEntityType(EntityType<T> entityType) {
        ENTITY_TYPES.add(entityType);
        return entityType;
    }


    public static final EntityType<Spawner> SPAWNER_ENTITY_TYPE = registerEntityType(EntityType.create("spawner", Spawner::new, 32, 32));
    public static final EntityType<Trash> TRASH_ENTITY_TYPE = registerEntityType(EntityType.create("trash", Trash::new, 32, 32));
    public static final EntityType<Wire> WIRE_ENTITY_TYPE = registerEntityType(EntityType.create("wire", Wire::new, 32, 32));
}
