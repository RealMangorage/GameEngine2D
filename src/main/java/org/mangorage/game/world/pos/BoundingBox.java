package org.mangorage.game.world.pos;

import java.util.ArrayList;
import java.util.List;

public final class BoundingBox {

    public interface PartBuilder {
        void addPart(int offsetX, int offsetY, int width, int height);
    }

    public interface BoundingBoxMultiPartBuilder {
        void build(PartBuilder partBuilder, int width, int height);
    }

    public record Part(int offsetX, int offsetY, int width, int height) {}

    private final int width;
    private final int height;
    private final List<Part> parts;

    public BoundingBox(int width, int height) {
        this(width, height, null);
    }

    public BoundingBox(int width, int height, BoundingBoxMultiPartBuilder builder) {
        this.width = width;
        this.height = height;

        if (builder == null) {
            this.parts = List.of();
        } else {
            List<Part> tmp = new ArrayList<>();
            builder.build((ox, oy, w, h) -> tmp.add(new Part(ox, oy, w, h)), width, height);
            this.parts = List.copyOf(tmp);
        }
    }

    // GLOBAL CHECK (entity position provided externally)
    public boolean contains(int entityX, int entityY, int px, int py) {
        return contains(entityX, entityY, px, py, Facing.EAST);
    }

    public boolean contains(int entityX, int entityY, int px, int py, Facing facing) {
        // no multipart -> simple rect check, width/height swap for 90/270 rotations
        if (parts.isEmpty()) {
            int w = (facing == Facing.EAST || facing == Facing.WEST) ? width : height;
            int h = (facing == Facing.EAST || facing == Facing.WEST) ? height : width;
            return px >= entityX && px <= entityX + w
                    && py >= entityY && py <= entityY + h;
        }

        for (Part p : parts(facing)) {
            int bx = entityX + p.offsetX;
            int by = entityY + p.offsetY;

            if (px >= bx && px <= bx + p.width
                    && py >= by && py <= by + p.height) {
                return true;
            }
        }

        return false;
    }

    /**
     * Return parts transformed according to the provided facing (rotation around top-left).
     */
    public java.util.List<Part> parts(Facing facing) {
        if (facing == Facing.EAST) return parts;

        List<Part> transformed = new ArrayList<>();

        for (Part p : parts) {
            int ox = p.offsetX;
            int oy = p.offsetY;
            int pw = p.width;
            int ph = p.height;

            switch (facing) {
                case SOUTH -> {
                    // 90 deg clockwise
                    int nx = height - (oy + ph);
                    int ny = ox;
                    transformed.add(new Part(nx, ny, ph, pw));
                }
                case WEST -> {
                    // 180 deg
                    int nx = width - (ox + pw);
                    int ny = height - (oy + ph);
                    transformed.add(new Part(nx, ny, pw, ph));
                }
                case NORTH -> {
                    // 270 deg clockwise (or 90 ccw)
                    int nx = oy;
                    int ny = width - (ox + pw);
                    transformed.add(new Part(nx, ny, ph, pw));
                }
                default -> transformed.add(p);
            }
        }

        return java.util.List.copyOf(transformed);
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public List<Part> parts() {
        return parts;
    }
}