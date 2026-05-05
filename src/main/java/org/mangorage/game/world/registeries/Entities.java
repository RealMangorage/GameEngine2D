package org.mangorage.game.world.registeries;

import org.mangorage.game.world.entity.Entity;
import org.mangorage.game.world.entity.EntityType;
import org.mangorage.game.world.entity.io.Spawner;
import org.mangorage.game.world.entity.io.Splitter;
import org.mangorage.game.world.entity.io.Trash;
import org.mangorage.game.world.entity.io.RelayBox;
import org.mangorage.game.world.entity.transport.ItemSpawner;
import org.mangorage.game.world.entity.transport.ItemTrash;
import org.mangorage.game.world.entity.transport.ItemBelt;

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
    public static final EntityType<RelayBox> RELAY_BOX_ENTITY_TYPE = registerEntityType(EntityType.create("relay", RelayBox::new, 32*3, 32*3));
    public static final EntityType<Splitter> SPLITTER_ENTITY_TYPE = registerEntityType(EntityType.create("splitter", Splitter::new, 32, 32));

    // Transport entities
    public static final EntityType<ItemSpawner> ITEM_SPAWNER_ENTITY_TYPE = registerEntityType(EntityType.create("item_spawner", ItemSpawner::new, 32, 32));
    public static final EntityType<ItemTrash> ITEM_TRASH_ENTITY_TYPE = registerEntityType(EntityType.create("item_trash", ItemTrash::new, 32, 32));
    public static final EntityType<ItemBelt> ITEM_BELT_ENTITY_TYPE = registerEntityType(EntityType.create("item_belt", ItemBelt::new, 48, 16));
}
