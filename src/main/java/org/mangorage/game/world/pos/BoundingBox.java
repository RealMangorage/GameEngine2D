package org.mangorage.game.world.pos;

public record BoundingBox(Location location, int width, int height) {
    // Checks if a given X and Y coordinate is inside this bounding box
    public boolean contains(int px, int py) {
        return px >= location.x() && px <= location.x() + width && py >= location.y() && py <= location().y() + height;
    }

    public String format() {
        return "(x=%d, y=%d, width=%d, height=%d)".formatted(location.x(), location.y(), width, height);
    }

    public int x() {
        return location().x();
    }

    public int y() {
        return location().y();
    }
}
