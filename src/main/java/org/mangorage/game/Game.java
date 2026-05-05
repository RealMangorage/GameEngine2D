package org.mangorage.game;

import org.mangorage.game.input.GameMouseEvent;
import org.mangorage.game.input.MouseButton;
import org.mangorage.game.render.RenderContext;
import org.mangorage.game.world.pos.Camera;
import org.mangorage.game.world.misc.InputHandler;
import org.mangorage.game.world.World;
import org.mangorage.game.world.entity.Entity;
import org.mangorage.game.world.misc.PlacingMode;
import org.mangorage.game.world.registeries.Entities;
import org.mangorage.game.world.pos.Position;
import org.mangorage.game.world.pos.Facing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Game extends Canvas implements Runnable, InputHandler {

    private boolean running = false;
    private Thread gameThread;

    private final int width = 800;
    private final int height = 600;

    private final RenderContext gameContext = new RenderContext();
    private final RenderContext screenContext = new RenderContext();

    private final World world = new World();
    private final Camera camera = new Camera();

    private final boolean[] keys = new boolean[256];
    private final Queue<GameMouseEvent> mouseEvents = new ConcurrentLinkedDeque<>();

    private int mouseX = 0;
    private int mouseY = 0;

    private double lastCheckedInputs = 0;

    private Entity selected = null;

    private int selectedType = 0;
    private PlacingMode placingMode = PlacingMode.OFF;
    private Facing currentRotation = Facing.EAST;
    private boolean placementSnap = true;
    private boolean placementAutoOrient = false;
    private boolean placementSnapX = true;
    private boolean placementSnapY = true;

    public Game() {
        JFrame frame = new JFrame("Simple Game Framework");

        setPreferredSize(new Dimension(width, height));
        setFocusable(true);

        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() < keys.length) {
                    keys[e.getKeyCode()] = true;
                    world.updateInput(Game.this);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() < keys.length) {
                    keys[e.getKeyCode()] = false;
                    world.updateInput(Game.this);
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseEvents.add(new GameMouseEvent(e.getX(), e.getY(), e.getButton()));
            }
        });

        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });

        addMouseWheelListener(e -> {
            if (isKeyDown(KeyEvent.VK_SHIFT)) {
                camera.zoom(-e.getPreciseWheelRotation() * 0.1);
            } else {
                selectedType = Math.max(
                        0,
                        Math.min(Entities.ENTITY_TYPES.size() - 1,
                                selectedType + (int) e.getPreciseWheelRotation()
                        )
                );
            }
        });

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(this);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public synchronized void start() {
        if (running) return;
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public synchronized void stop() {
        if (!running) return;
        running = false;

        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        requestFocus();

        long lastTime = System.nanoTime();
        double nsPerUpdate = 1_000_000_000.0 / 60.0;
        double delta = 0;

        while (running) {

            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerUpdate;
            lastTime = now;

            while (delta >= 1) {
                handleInput(delta);
                world.update(delta);
                delta--;
            }

            render();
        }

        stop();
    }

    // ----------------------------
    // CLEAN WORLD MOUSE CONVERSION
    // ----------------------------
    private Position screenToWorld(int sx, int sy) {
        double zoom = camera.getZoom();
        int x = (int) (sx / zoom + camera.getX());
        int y = (int) (sy / zoom + camera.getY());
        return new Position(x, y, 0);
    }

    private void handleInput(double delta) {
        int speed = 6;

        if (isKeyDown(KeyEvent.VK_W)) camera.move(0, -speed);
        if (isKeyDown(KeyEvent.VK_S)) camera.move(0, speed);
        if (isKeyDown(KeyEvent.VK_A)) camera.move(-speed, 0);
        if (isKeyDown(KeyEvent.VK_D)) camera.move(speed, 0);

        lastCheckedInputs += delta;

        if (lastCheckedInputs >= 4) {
            if (isKeyDown(KeyEvent.VK_Q)) selected = null;

            if (isKeyDown(KeyEvent.VK_F4)) {
                placingMode = switch (placingMode) {
                    case OFF -> PlacingMode.PLACE;
                    case PLACE -> PlacingMode.DELETE;
                    case DELETE -> PlacingMode.OFF;
                };
            }

            if (isKeyDown(KeyEvent.VK_R)) {
                // rotate selected entity if any, otherwise rotate the currently selected rotation for placement
                if (selected != null) {
                    selected.rotate();
                } else {
                    currentRotation = currentRotation.next();
                }
            }

            if (isKeyDown(KeyEvent.VK_G)) {
                placementSnap = !placementSnap;
            }

            if (isKeyDown(KeyEvent.VK_O)) {
                placementAutoOrient = !placementAutoOrient;
            }
            if (isKeyDown(KeyEvent.VK_X)) {
                placementSnapX = !placementSnapX;
            }
            if (isKeyDown(KeyEvent.VK_Y)) {
                placementSnapY = !placementSnapY;
            }

            lastCheckedInputs = 0;
        }

        while (!mouseEvents.isEmpty()) {
            GameMouseEvent e = mouseEvents.poll();

            Position worldPos = screenToWorld(e.x(), e.y());

            var entity = world.handleClick(worldPos.x(), worldPos.y());
            var button = e.button() >= MouseButton.values().length
                    ? MouseButton.UNKNOWN
                    : MouseButton.values()[e.button()];

            if (entity != null) {

                if (placingMode == PlacingMode.DELETE) {
                    world.removeEntity(entity);
                    continue;
                }

                if (isKeyDown(KeyEvent.VK_SHIFT)) {
                    selected = entity;
                }

                entity.onClick(button);

                if (selected != null) {
                    selected.onClickWithSelected(selected, entity);
                }

            } else if (button == MouseButton.LEFT && placingMode == PlacingMode.PLACE) {

                var newEntity = Entities.ENTITY_TYPES.get(selectedType).create(world, worldPos);
                // apply rotation first so we can center correctly
                newEntity.setFacing(currentRotation);

                var bb = newEntity.getBoundingBox();
                int w = (newEntity.getFacing() == Facing.EAST || newEntity.getFacing() == Facing.WEST) ? bb.width() : bb.height();
                int h = (newEntity.getFacing() == Facing.EAST || newEntity.getFacing() == Facing.WEST) ? bb.height() : bb.width();

                Position centered = new Position(worldPos.x() - w / 2, worldPos.y() - h / 2, 0);
                // apply snapping similarly to the ghost so placement matches the preview
                centered = snapPlacement(newEntity, centered, placementSnap);
                newEntity.setPosition(centered);

                world.addEntity(newEntity);
            }
        }
    }

    private void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }

        Graphics2D graphics = (Graphics2D) bs.getDrawGraphics();

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);

        screenContext.render(graphics);

        graphics.scale(camera.getZoom(), camera.getZoom());
        graphics.translate(-camera.getX(), -camera.getY());

        world.render(gameContext);

        Position mouseWorld = screenToWorld(mouseX, mouseY);

        Entity ghostEntity = null;
        RenderContext ghostContext = null;

        if (placingMode == PlacingMode.PLACE && selectedType >= 0 && selectedType < Entities.ENTITY_TYPES.size()) {

            ghostEntity = Entities.ENTITY_TYPES.get(selectedType).create(world, mouseWorld);

            // apply rotation first so bounding box extents can be computed correctly
            ghostEntity.setFacing(currentRotation);

            var gb = ghostEntity.getBoundingBox();
            int w = (ghostEntity.getFacing() == Facing.EAST || ghostEntity.getFacing() == Facing.WEST) ? gb.width() : gb.height();
            int h = (ghostEntity.getFacing() == Facing.EAST || ghostEntity.getFacing() == Facing.WEST) ? gb.height() : gb.width();

            Position centered = new Position(
                    mouseWorld.x() - w / 2,
                    mouseWorld.y() - h / 2,
                    0
            );

            // apply snapping to nearby entities or to grid depending on toggle
            centered = snapPlacement(ghostEntity, centered, placementSnap);

            ghostEntity.setPosition(centered);

            ghostContext = new RenderContext();
            ghostEntity.render(ghostContext);
        }

        screenContext.submit(g -> {
            g.setColor(Color.BLUE);
            g.drawString(
                    "Selected: " + (selected == null ? "NONE"
                            : selected.getType().getName()),
                    10, 20
            );
            g.drawString("Selected Type: " +
                            Entities.ENTITY_TYPES.get(selectedType).getName(),
                    10, 40
            );
            g.drawString("Placing Mode: " + placingMode + " (F4)", 10, 60);
            g.drawString("Rotation: " + (selected != null ? selected.getFacing() : currentRotation), 10, 80);
            g.drawString("Snap: " + (placementSnap ? "ON (G)" : "OFF (G)"), 10, 100);
            g.drawString("SnapX: " + (placementSnapX ? "ON (X)" : "OFF (X)"), 10, 120);
            g.drawString("SnapY: " + (placementSnapY ? "ON (Y)" : "OFF (Y)"), 10, 140);
            g.drawString("Auto-Orient: " + (placementAutoOrient ? "ON (O)" : "OFF (O)"), 10, 160);
        });

        gameContext.render(graphics);

        if (ghostContext != null && ghostEntity != null) {
            Graphics2D gGhost = (Graphics2D) graphics.create();

            gGhost.setComposite(
                    AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f)
            );

            ghostContext.render(gGhost);

            if (world.isRenderBoundingBoxes()) {
                gGhost.setComposite(
                        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f)
                );
                gGhost.setColor(Color.WHITE);

                var pos = ghostEntity.getPosition();

                for (var p : ghostEntity.getBoundingBox().parts(ghostEntity.getFacing())) {
                    gGhost.drawRect(
                            pos.x() + p.offsetX(),
                            pos.y() + p.offsetY(),
                            p.width(),
                            p.height()
                    );
                }
            }

            gGhost.dispose();
        }

        graphics.dispose();
        bs.show();
    }

    /**
     * Snap the placement of an entity to nearby entities or a grid.
     * If snap==false, returns the provided centered position unchanged.
     */
    private Position snapPlacement(Entity ghost, Position centered, boolean snap) {
        if (!snap) return centered;

        int snapThreshold = 16; // pixels within which to snap to other entity edges

        // ghost bounding box and world rect
        var gb = ghost.getBoundingBox();
        int gw = (ghost.getFacing() == Facing.EAST || ghost.getFacing() == Facing.WEST) ? gb.width() : gb.height();
        int gh = (ghost.getFacing() == Facing.EAST || ghost.getFacing() == Facing.WEST) ? gb.height() : gb.width();

        int gx = centered.x();
        int gy = centered.y();

        // track best snap (smallest distance)
        double bestDist = Double.POSITIVE_INFINITY;
        Rectangle bestRect = null;
        Entity bestOther = null;
        String bestSide = null; // "LEFT"/"RIGHT"/"TOP"/"BOTTOM"

        for (Entity other : world.getEntities()) {
            if (other == ghost) continue;
            Position op = other.getPosition();
            var ob = other.getBoundingBox();
            java.util.List<org.mangorage.game.world.pos.BoundingBox.Part> parts = ob.parts(other.getFacing());

            if (parts.isEmpty()) {
                int ow = (other.getFacing() == Facing.EAST || other.getFacing() == Facing.WEST) ? ob.width() : ob.height();
                int oh = (other.getFacing() == Facing.EAST || other.getFacing() == Facing.WEST) ? ob.height() : ob.width();

                Rectangle r = new Rectangle(op.x(), op.y(), ow, oh);
                // candidate snaps: left/right/top/bottom
                // left/right candidates only if snapping on X axis is enabled
                if (placementSnapX) {
                    // left: align ghost right edge to other left
                    int candX = r.x - gw;
                    int candY = gy;
                    double dist = Math.hypot(candX - gx, candY - gy);
                    if (Math.abs(candX - gx) <= snapThreshold && dist < bestDist) {
                        bestDist = dist; bestRect = r; bestOther = other; bestSide = "LEFT";
                    }

                    // right: align ghost left edge to other right
                    candX = r.x + r.width;
                    candY = gy;
                    dist = Math.hypot(candX - gx, candY - gy);
                    if (Math.abs(candX - gx) <= snapThreshold && dist < bestDist) {
                        bestDist = dist; bestRect = r; bestOther = other; bestSide = "RIGHT";
                    }
                }

                // top/bottom candidates only if snapping on Y axis is enabled
                if (placementSnapY) {
                    // top: align ghost bottom to other top
                    int candX = gx;
                    int candY = r.y - gh;
                    double dist = Math.hypot(candX - gx, candY - gy);
                    if (Math.abs(candY - gy) <= snapThreshold && dist < bestDist) {
                        bestDist = dist; bestRect = r; bestOther = other; bestSide = "TOP";
                    }

                    // bottom: align ghost top to other bottom
                    candX = gx;
                    candY = r.y + r.height;
                    dist = Math.hypot(candX - gx, candY - gy);
                    if (Math.abs(candY - gy) <= snapThreshold && dist < bestDist) {
                        bestDist = dist; bestRect = r; bestOther = other; bestSide = "BOTTOM";
                    }
                }
            } else {
                for (var p : parts) {
                    int ox = op.x() + p.offsetX();
                    int oy = op.y() + p.offsetY();
                    int ow = p.width();
                    int oh = p.height();
                    Rectangle r = new Rectangle(ox, oy, ow, oh);

                    if (placementSnapX) {
                        int candX = r.x - gw; int candY = gy;
                        double dist = Math.hypot(candX - gx, candY - gy);
                        if (Math.abs(candX - gx) <= snapThreshold && dist < bestDist) { bestDist = dist; bestRect = r; bestOther = other; bestSide = "LEFT"; }

                        candX = r.x + r.width; candY = gy;
                        dist = Math.hypot(candX - gx, candY - gy);
                        if (Math.abs(candX - gx) <= snapThreshold && dist < bestDist) { bestDist = dist; bestRect = r; bestOther = other; bestSide = "RIGHT"; }
                    }

                    if (placementSnapY) {
                        int candX = gx; int candY = r.y - gh;
                        double dist = Math.hypot(candX - gx, candY - gy);
                        if (Math.abs(candY - gy) <= snapThreshold && dist < bestDist) { bestDist = dist; bestRect = r; bestOther = other; bestSide = "TOP"; }

                        candX = gx; candY = r.y + r.height;
                        dist = Math.hypot(candX - gx, candY - gy);
                        if (Math.abs(candY - gy) <= snapThreshold && dist < bestDist) { bestDist = dist; bestRect = r; bestOther = other; bestSide = "BOTTOM"; }
                    }
                }
            }
        }

        if (bestDist < Double.POSITIVE_INFINITY && bestRect != null && bestOther != null && bestSide != null) {
            // If same entity type, align facing to the existing entity and snap exactly on the same X or Y
            boolean sameType = ghost.getType() == bestOther.getType();
            Facing finalFacing = ghost.getFacing();
            // only auto-orient to the other entity's facing if the user enabled auto-orient (O)
            if (sameType && placementAutoOrient) {
                finalFacing = bestOther.getFacing();
                // do not forcibly set ghost facing unless auto-orient is enabled
                ghost.setFacing(finalFacing);
            }

            int finalGw = (finalFacing == Facing.EAST || finalFacing == Facing.WEST) ? gb.width() : gb.height();
            int finalGh = (finalFacing == Facing.EAST || finalFacing == Facing.WEST) ? gb.height() : gb.width();

            int fx = gx;
            int fy = gy;

            switch (bestSide) {
                case "LEFT" -> {
                    if (placementSnapX) fx = bestRect.x - finalGw;
                    if (sameType && placementSnapY) fy = bestRect.y;
                }
                case "RIGHT" -> {
                    if (placementSnapX) fx = bestRect.x + bestRect.width;
                    if (sameType && placementSnapY) fy = bestRect.y;
                }
                case "TOP" -> {
                    if (placementSnapY) fy = bestRect.y - finalGh;
                    if (sameType && placementSnapX) fx = bestRect.x;
                }
                case "BOTTOM" -> {
                    if (placementSnapY) fy = bestRect.y + bestRect.height;
                    if (sameType && placementSnapX) fx = bestRect.x;
                }
            }

            return new Position(fx, fy, centered.z());
        }

        // fallback: snap to grid (16 px)
        int grid = 16;
        int sx = Math.round((float) gx / grid) * grid;
        int sy = Math.round((float) gy / grid) * grid;
        return new Position(sx, sy, centered.z());
    }

    @Override
    public boolean isKeyDown(int keyEvent) {
        return keys[keyEvent];
    }
}