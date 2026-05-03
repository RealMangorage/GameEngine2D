package org.mangorage.game;

import org.mangorage.game.input.GameMouseEvent;
import org.mangorage.game.input.MouseButton;
import org.mangorage.game.render.RenderContext;
import org.mangorage.game.world.pos.BoundingBox;
import org.mangorage.game.world.pos.Camera;
import org.mangorage.game.world.misc.InputHandler;
import org.mangorage.game.world.pos.Location;
import org.mangorage.game.world.World;
import org.mangorage.game.world.entity.Entity;
import org.mangorage.game.world.misc.PlacingMode;
import org.mangorage.game.world.registeries.Entities;

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

    private double lastCheckedInputs = 0;

    private Entity selected = null;

    private int selectedType = 0;
    private PlacingMode placingMode = PlacingMode.OFF;


    public Game() {
        JFrame frame = new JFrame("Simple Game Framework");

        setPreferredSize(new Dimension(width, height));
        setFocusable(true);

        // Keyboard Input
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

        addMouseWheelListener(e -> {
            if (isKeyDown(KeyEvent.VK_SHIFT)) {
                double zoomChange = -e.getPreciseWheelRotation() * 0.1;
                camera.zoom(zoomChange);
            } else {
                selectedType = Math.max(0, Math.min(Entities.ENTITY_TYPES.size() - 1, selectedType + (int) e.getPreciseWheelRotation()));
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

    private void handleInput(double delta) {
        int speed = 6;

        if (isKeyDown(KeyEvent.VK_W))
            camera.move(0, -speed);

        if (isKeyDown(KeyEvent.VK_S))
            camera.move(0, speed);

        if (isKeyDown(KeyEvent.VK_A))
            camera.move(-speed, 0);

        if (isKeyDown(KeyEvent.VK_D))
            camera.move(speed, 0);

        lastCheckedInputs += delta;

        if (lastCheckedInputs >= 4) {
            if (isKeyDown(KeyEvent.VK_Q)) {
                selected = null;
            }

            if (isKeyDown(KeyEvent.VK_F4)) {


                placingMode = switch (placingMode) {
                    case OFF -> PlacingMode.PLACE;
                    case PLACE -> PlacingMode.DELETE;
                    case DELETE -> PlacingMode.OFF;
                };

            }
            lastCheckedInputs = 0;
        }

        double zoom = camera.getZoom();

        while (!mouseEvents.isEmpty()) {
            GameMouseEvent e = mouseEvents.poll();

            // ✅ correct inverse transform
            int worldX = (int)(e.x() / zoom + camera.getX());
            int worldY = (int)(e.y() / zoom + camera.getY());

            var entity = world.handleClick(worldX, worldY);
            var button = e.button() >= MouseButton.values().length ? MouseButton.UNKNOWN : MouseButton.values()[e.button()];

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

            } else if (button == MouseButton.LEFT) {
                if (placingMode == PlacingMode.PLACE) {
                    world.addEntity(
                            Entities.ENTITY_TYPES.get(selectedType).create(world, new Location(worldX, worldY))
                    );
                }
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


        // 3. RENDER SCREEN UI (Top Layer)
        // This uses the original 'graphics' object which is still in screen space
        screenContext.submit(g -> {
            g.setColor(Color.BLUE);
            g.drawString("Selected: " +  (this.selected == null ? "NONE" : (selected.getType().getName() + " " + selected.getBoundingBox().format())), 10, 20);
            g.drawString("Selected Type: " + (selectedType >= 0 && selectedType < Entities.ENTITY_TYPES.size() ? Entities.ENTITY_TYPES.get(selectedType).getName() : "None"), 10, 40);
            g.drawString("Placing Mode: " + (placingMode) + " (Toggle with F4)", 10, 60);
        });

        gameContext.render(graphics);

        // FINALIZE
        graphics.dispose();
        bs.show();
    }
    @Override
    public boolean isKeyDown(int keyEvent) {
        return keys[keyEvent];
    }
}