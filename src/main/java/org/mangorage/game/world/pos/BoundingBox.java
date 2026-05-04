package org.mangorage.game.world.pos;

import java.util.ArrayList;
import java.util.List;

/**
 * Mutable bounding box with optional "parts". Parts are rectangles defined relative to the box's
 * top-left location. If parts are present, containment tests use the parts; otherwise the whole box
 * is used.
 */
public class BoundingBox {
    private Location location;
    private int width;
    private int height;

    // Parts relative to the bounding box location
    private final List<Part> parts = new ArrayList<>();

    public BoundingBox(Location location, int width, int height) {
        this.location = location;
        this.width = width;
        this.height = height;
    }

    // Checks if a given X and Y coordinate is inside this bounding box or any of its parts
    public boolean contains(int px, int py) {
        if (parts.isEmpty()) {
            return px >= location.x() && px <= location.x() + width && py >= location.y() && py <= location.y() + height;
        }

        for (Part p : parts) {
            int bx = location.x() + p.offsetX;
            int by = location.y() + p.offsetY;
            if (px >= bx && px <= bx + p.width && py >= by && py <= by + p.height) return true;
        }
        return false;
    }

    public String format() {
        return "(x=%d, y=%d, width=%d, height=%d)".formatted(location.x(), location.y(), width, height);
    }

    public int x() { return location.x(); }
    public int y() { return location.y(); }
    public int width() { return width; }
    public int height() { return height; }

    public Location location() { return location; }

    public void setLocation(Location loc) { this.location = loc; }

    // Parts API
    public void addPart(int offsetX, int offsetY, int w, int h) { parts.add(new Part(offsetX, offsetY, w, h)); }
    public void clearParts() { parts.clear(); }

    /**
     * Returns absolute bounding boxes for each part (in world coordinates). If no parts are defined,
     * returns a single-element list containing this bounding box (copy) to keep callers simple.
     */
    public List<BoundingBox> getPartsAbsolute() {
        var out = new ArrayList<BoundingBox>();
        if (parts.isEmpty()) {
            out.add(new BoundingBox(location, width, height));
            return out;
        }

        for (Part p : parts) {
            out.add(new BoundingBox(new Location(location.x() + p.offsetX, location.y() + p.offsetY), p.width, p.height));
        }
        return out;
    }

    private static final class Part {
        final int offsetX, offsetY, width, height;
        Part(int offsetX, int offsetY, int width, int height) { this.offsetX = offsetX; this.offsetY = offsetY; this.width = width; this.height = height; }
    }
}
