package org.mangorage.game.client.layer;

import org.mangorage.game.Game;
import org.mangorage.game.input.GameMouseEvent;
import org.mangorage.game.input.MouseButton;
import org.mangorage.game.client.render.core.RenderContext;
import org.mangorage.game.world.World;
import org.mangorage.game.world.entity.Entity;
import org.mangorage.game.world.misc.PlacingMode;
import org.mangorage.game.world.pos.BoundingBox.Part;
import org.mangorage.game.world.pos.Camera;
import org.mangorage.game.world.pos.Facing;
import org.mangorage.game.world.pos.Position;
import org.mangorage.game.world.registeries.Entities;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Queue;

public final class WorldLayer extends Layer {

    // Configuration Constants
    private static final int CAMERA_SPEED = 6;
    private static final double INPUT_DELAY_THRESHOLD = 4.0;
    private static final int SNAP_THRESHOLD = 16;
    private static final int GRID_SIZE = 16;
    private static final float GHOST_OPACITY = 0.35f;
    private static final float BOUNDING_BOX_OPACITY = 0.85f;

    private final Game game = Game.getInstance();
    private final World world = new World();
    private final Camera camera = new Camera();

    private Entity selected = null;
    private int selectedType = 0;
    private PlacingMode placingMode = PlacingMode.OFF;
    private Facing currentRotation = Facing.EAST;

    private boolean placementSnap = true;
    private boolean placementAutoOrient = false;
    private boolean placementSnapX = true;
    private boolean placementSnapY = true;

    private double lastCheckedInputs = 0;

    public WorldLayer() {}

    public void zoomCamera(double amount) {
        camera.zoom(amount);
    }

    public void scrollSelectedType(int amount) {
        selectedType = Math.max(0, Math.min(Entities.ENTITY_TYPES.size() - 1, selectedType + amount));
    }

    @Override
    public void update(double delta) {
        world.update(delta);
    }

    @Override
    public void handleInput(double delta, Queue<GameMouseEvent> mouseEvents) {
        handleCameraMovement();

        lastCheckedInputs += delta;
        if (lastCheckedInputs >= INPUT_DELAY_THRESHOLD) {
            handleToggles();
            lastCheckedInputs = 0;
        }

        processMouse(mouseEvents);
    }

    private void handleCameraMovement() {
        if (game.isKeyDown(KeyEvent.VK_W)) camera.move(0, -CAMERA_SPEED);
        if (game.isKeyDown(KeyEvent.VK_S)) camera.move(0, CAMERA_SPEED);
        if (game.isKeyDown(KeyEvent.VK_A)) camera.move(-CAMERA_SPEED, 0);
        if (game.isKeyDown(KeyEvent.VK_D)) camera.move(CAMERA_SPEED, 0);
    }

    private void handleToggles() {
        if (game.isKeyDown(KeyEvent.VK_Q)) {
            selected = null;
        }

        if (game.isKeyDown(KeyEvent.VK_F4)) {
            placingMode = switch (placingMode) {
                case OFF -> PlacingMode.PLACE;
                case PLACE -> PlacingMode.DELETE;
                case DELETE -> PlacingMode.OFF;
            };
        }

        if (game.isKeyDown(KeyEvent.VK_R)) {
            if (selected != null) {
                selected.rotate();
            } else {
                currentRotation = currentRotation.next();
            }
        }

        if (game.isKeyDown(KeyEvent.VK_G)) placementSnap = !placementSnap;
        if (game.isKeyDown(KeyEvent.VK_O)) placementAutoOrient = !placementAutoOrient;
        if (game.isKeyDown(KeyEvent.VK_X)) placementSnapX = !placementSnapX;
        if (game.isKeyDown(KeyEvent.VK_Y)) placementSnapY = !placementSnapY;
    }

    private void processMouse(Queue<GameMouseEvent> mouseEvents) {
        while (!mouseEvents.isEmpty()) {
            GameMouseEvent e = mouseEvents.poll();
            Position worldPos = screenToWorld(e.x(), e.y());

            Entity entity = world.handleClick(worldPos.x(), worldPos.y());
            MouseButton button = e.button() >= MouseButton.values().length
                    ? MouseButton.UNKNOWN
                    : MouseButton.values()[e.button()];

            if (entity != null) {
                handleEntityClick(entity, button);
            } else if (button == MouseButton.LEFT && placingMode == PlacingMode.PLACE) {
                placeNewEntity(worldPos);
            }
        }
    }

    private void handleEntityClick(Entity entity, MouseButton button) {
        if (placingMode == PlacingMode.DELETE) {
            world.removeEntity(entity);
            return;
        }

        if (game.isKeyDown(KeyEvent.VK_SHIFT)) {
            selected = entity;
        }

        entity.onClick(button);

        if (selected != null) {
            selected.onClickWithSelected(selected, entity);
        }
    }

    private void placeNewEntity(Position worldPos) {
        Entity newEntity = Entities.ENTITY_TYPES.get(selectedType).create(world, worldPos);
        newEntity.setFacing(currentRotation);

        int w = getOrientedWidth(newEntity);
        int h = getOrientedHeight(newEntity);

        Position centered = new Position(worldPos.x() - w / 2, worldPos.y() - h / 2, 0);
        newEntity.setPosition(snapPlacement(newEntity, centered, placementSnap));

        world.addEntity(newEntity);
    }

    @Override
    public void render(RenderContext context) {
        context.submit(g -> {
            g.scale(camera.getZoom(), camera.getZoom());
            g.translate(-camera.getX(), -camera.getY());

            Position mouseWorld = screenToWorld(game.getMouseX(), game.getMouseY());

            if (placingMode == PlacingMode.PLACE && selectedType >= 0 && selectedType < Entities.ENTITY_TYPES.size()) {
                renderPlacementGhost(g, mouseWorld);
            }

        });

        context.push();
        world.render(context);
        context.pop();
    }

    private void renderPlacementGhost(Graphics2D g, Position mouseWorld) {
        Entity ghost = Entities.ENTITY_TYPES.get(selectedType).create(world, mouseWorld);
        ghost.setFacing(currentRotation);

        int w = getOrientedWidth(ghost);
        int h = getOrientedHeight(ghost);

        Position centered = new Position(mouseWorld.x() - w / 2, mouseWorld.y() - h / 2, 0);
        ghost.setPosition(snapPlacement(ghost, centered, placementSnap));

        RenderContext ghostContext = new RenderContext();
        ghost.render(ghostContext);

        Graphics2D gGhost = (Graphics2D) g.create();
        gGhost.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, GHOST_OPACITY));
        ghostContext.render(gGhost);

        if (world.isRenderBoundingBoxes()) {
            gGhost.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, BOUNDING_BOX_OPACITY));
            gGhost.setColor(Color.WHITE);

            Position pos = ghost.getPosition();
            for (Part p : ghost.getBoundingBox().parts(ghost.getFacing())) {
                gGhost.drawRect(pos.x() + p.offsetX(), pos.y() + p.offsetY(), p.width(), p.height());
            }
        }
        gGhost.dispose();
    }

    private Position screenToWorld(int sx, int sy) {
        double zoom = camera.getZoom();
        int x = (int) (sx / zoom + camera.getX());
        int y = (int) (sy / zoom + camera.getY());
        return new Position(x, y, 0);
    }

    // --- Snapping Logic ---

    private Position snapPlacement(Entity ghost, Position centered, boolean snap) {
        if (!snap) return centered;

        int gw = getOrientedWidth(ghost);
        int gh = getOrientedHeight(ghost);
        int gx = centered.x();
        int gy = centered.y();

        SnapState state = new SnapState();

        for (Entity other : world.getEntities()) {
            if (other == ghost) continue;

            Position op = other.getPosition();
            List<Part> parts = other.getBoundingBox().parts(other.getFacing());

            if (parts.isEmpty()) {
                Rectangle r = new Rectangle(op.x(), op.y(), getOrientedWidth(other), getOrientedHeight(other));
                evaluateSnap(state, r, gw, gh, gx, gy, other);
            } else {
                for (Part p : parts) {
                    Rectangle r = new Rectangle(op.x() + p.offsetX(), op.y() + p.offsetY(), p.width(), p.height());
                    evaluateSnap(state, r, gw, gh, gx, gy, other);
                }
            }
        }

        if (state.bestDist < Double.POSITIVE_INFINITY) {
            return calculateFinalSnapPosition(ghost, state, gw, gh, gx, gy, centered.z());
        }

        int sx = Math.round((float) gx / GRID_SIZE) * GRID_SIZE;
        int sy = Math.round((float) gy / GRID_SIZE) * GRID_SIZE;
        return new Position(sx, sy, centered.z());
    }

    private void evaluateSnap(SnapState state, Rectangle r, int gw, int gh, int gx, int gy, Entity other) {
        if (placementSnapX) {
            state.check(r.x - gw, gy, gx, gy, r, other, SnapSide.LEFT, true);
            state.check(r.x + r.width, gy, gx, gy, r, other, SnapSide.RIGHT, true);
        }
        if (placementSnapY) {
            state.check(gx, r.y - gh, gx, gy, r, other, SnapSide.TOP, false);
            state.check(gx, r.y + r.height, gx, gy, r, other, SnapSide.BOTTOM, false);
        }
    }

    private Position calculateFinalSnapPosition(Entity ghost, SnapState state, int gw, int gh, int gx, int gy, int gz) {
        boolean sameType = ghost.getType() == state.bestOther.getType();

        if (sameType && placementAutoOrient) {
            ghost.setFacing(state.bestOther.getFacing());
        }

        // Re-calculate oriented bounds based on potentially new facing
        int finalGw = getOrientedWidth(ghost);
        int finalGh = getOrientedHeight(ghost);

        int fx = gx;
        int fy = gy;

        switch (state.bestSide) {
            case LEFT -> {
                if (placementSnapX) fx = state.bestRect.x - finalGw;
                if (sameType && placementSnapY) fy = state.bestRect.y;
            }
            case RIGHT -> {
                if (placementSnapX) fx = state.bestRect.x + state.bestRect.width;
                if (sameType && placementSnapY) fy = state.bestRect.y;
            }
            case TOP -> {
                if (placementSnapY) fy = state.bestRect.y - finalGh;
                if (sameType && placementSnapX) fx = state.bestRect.x;
            }
            case BOTTOM -> {
                if (placementSnapY) fy = state.bestRect.y + state.bestRect.height;
                if (sameType && placementSnapX) fx = state.bestRect.x;
            }
        }
        return new Position(fx, fy, gz);
    }

    private int getOrientedWidth(Entity entity) {
        Facing f = entity.getFacing();
        return (f == Facing.EAST || f == Facing.WEST) ? entity.getBoundingBox().width() : entity.getBoundingBox().height();
    }

    private int getOrientedHeight(Entity entity) {
        Facing f = entity.getFacing();
        return (f == Facing.EAST || f == Facing.WEST) ? entity.getBoundingBox().height() : entity.getBoundingBox().width();
    }

    // --- Getters ---

    public Entity getSelected() {
        return selected;
    }

    public int getSelectedType() {
        return selectedType;
    }

    public PlacingMode getPlacingMode() {
        return placingMode;
    }

    public Facing getCurrentRotation() {
        return currentRotation;
    }

    public boolean isPlacementSnap() {
        return placementSnap;
    }

    public boolean isPlacementSnapX() {
        return placementSnapX;
    }

    public boolean isPlacementSnapY() {
        return placementSnapY;
    }

    public boolean isPlacementAutoOrient() {
        return placementAutoOrient;
    }

    // --- Internal Snapping Helpers ---

    private enum SnapSide {
        LEFT, RIGHT, TOP, BOTTOM
    }

    private final static class SnapState {
        double bestDist = Double.POSITIVE_INFINITY;
        Rectangle bestRect = null;
        Entity bestOther = null;
        SnapSide bestSide = null;

        void check(int candX, int candY, int gx, int gy, Rectangle r, Entity other, SnapSide side, boolean isX) {
            double dist = Math.hypot(candX - gx, candY - gy);
            boolean inRange = isX ? Math.abs(candX - gx) <= SNAP_THRESHOLD : Math.abs(candY - gy) <= SNAP_THRESHOLD;

            if (inRange && dist < bestDist) {
                bestDist = dist;
                bestRect = r;
                bestOther = other;
                bestSide = side;
            }
        }
    }
}