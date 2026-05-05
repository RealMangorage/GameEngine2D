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

        if (parts.isEmpty()) {
            return px >= entityX && px <= entityX + width
                    && py >= entityY && py <= entityY + height;
        }

        for (Part p : parts) {
            int bx = entityX + p.offsetX;
            int by = entityY + p.offsetY;

            if (px >= bx && px <= bx + p.width
                    && py >= by && py <= by + p.height) {
                return true;
            }
        }

        return false;
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