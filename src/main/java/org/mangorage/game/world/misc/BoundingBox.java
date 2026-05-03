package org.mangorage.game.world.misc;

public record BoundingBox(int x, int y, int width, int height) {
    // Checks if a given X and Y coordinate is inside this bounding box
    public boolean contains(int px, int py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }
}
