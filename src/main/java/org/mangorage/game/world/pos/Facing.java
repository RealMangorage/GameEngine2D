package org.mangorage.game.world.pos;

public enum Facing {
    NORTH, EAST, SOUTH, WEST;

    public Facing next() {
        Facing[] vals = values();
        return vals[(this.ordinal() + 1) % vals.length];
    }
}

