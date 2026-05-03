package org.mangorage.game.world.misc;

public class Item {
    private final String name;

    // You can add render logic or metadata here later
    public Item(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}