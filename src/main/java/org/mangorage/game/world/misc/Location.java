package org.mangorage.game.world.misc;

public record Location(int x, int y) {
    public BoundingBox of(int width, int height) {
        return new BoundingBox(x, y, width, height);
    }
}
