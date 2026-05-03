package org.mangorage.game.world.pos;

public record Location(int x, int y) {
    public BoundingBox of(int width, int height) {
        return new BoundingBox(this, width, height);
    }
}
