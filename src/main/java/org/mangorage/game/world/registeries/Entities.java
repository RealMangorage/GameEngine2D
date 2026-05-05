package org.mangorage.game.world.registeries;

import org.mangorage.game.world.entity.Entity;
import org.mangorage.game.world.entity.EntityType;
import org.mangorage.game.world.entity.io.*;
import org.mangorage.game.world.entity.transport.*;

import java.util.ArrayList;
import java.util.List;

public final class Entities {

    public static final List<EntityType<?>> ENTITY_TYPES = new ArrayList<>();

    public static <T extends Entity> EntityType<T> register(EntityType<T> type) {
        ENTITY_TYPES.add(type);
        return type;
    }

    // -------------------------
    // IO ENTITIES
    // -------------------------

    public static final EntityType<Spawner> SPAWNER_ENTITY_TYPE = register(EntityType.create("spawner", 32, 32, Spawner::new));

    public static final EntityType<Trash> TRASH_ENTITY_TYPE = register(EntityType.create("trash", 32, 32, Trash::new));

    public static final EntityType<Splitter> SPLITTER_ENTITY_TYPE = register(EntityType.create("splitter", 32, 32, Splitter::new));

    public static final EntityType<RelayBox> RELAY_BOX_ENTITY_TYPE = register(
            EntityType.create(
                    "relay",
                    96, 96,
                    RelayBox::new
            ).withShape((builder, w, h) -> {
                int topBarH = Math.max(1, h / 4);
                int stemW = Math.max(1, w / 4);
                int stemH = Math.max(1, h - topBarH);
                int stemX = (w - stemW) / 2;

                builder.addPart(0, 0, w, topBarH);
                builder.addPart(stemX, topBarH, stemW, stemH);
            })
    );

    // -------------------------
    // TRANSPORT ENTITIES
    // -------------------------

    public static final EntityType<ItemSpawner> ITEM_SPAWNER_ENTITY_TYPE = register(EntityType.create("item_spawner", 32, 32, ItemSpawner::new));

    public static final EntityType<ItemTrash> ITEM_TRASH_ENTITY_TYPE = register(EntityType.create("item_trash", 32, 32, ItemTrash::new));

    public static final EntityType<ItemBelt> ITEM_BELT_ENTITY_TYPE = register(EntityType.create("item_belt", 48, 16, ItemBelt::new));
}