package org.mangorage.game.world.resource.item;

import org.mangorage.game.world.pos.Position;

public interface IItemReceiver {
    /**
     * Accept an item delivered from the given world position (source).
     * The source position is used by receivers (like belts) to determine where along
     * their surface the item should be inserted.
     */
    boolean acceptItem(Item item, Position source);
}
