package org.mangorage.game.world.entity;

import org.mangorage.game.world.misc.Item;

public interface IItemReceiver {
    boolean acceptItem(Item item);
}
