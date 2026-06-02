package org.mangorage.game;

import org.mangorage.game.client.layer.LayerId;
import org.mangorage.game.client.layer.LayerSystem;
import org.mangorage.game.client.layer.Layers;
import org.mangorage.game.input.GameMouseEvent;
import org.mangorage.game.client.layer.Layer;
import org.mangorage.game.client.layer.UILayer;
import org.mangorage.game.client.layer.WorldLayer;
import org.mangorage.game.client.render.core.RenderContext;
import org.mangorage.game.world.misc.InputHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Game extends Canvas implements Runnable, InputHandler {
    private static  Game INSTANCE;

    public synchronized static Game getInstance() {
        synchronized (Game.class) {
            if (INSTANCE == null) {
                INSTANCE = new Game();
            }
            return INSTANCE;
        }
    }

    private boolean running = false;
    private Thread gameThread;

    private final int width = 800;
    private final int height = 600;

    private final boolean[] keys = new boolean[256];
    private final Queue<GameMouseEvent> mouseEvents = new ConcurrentLinkedDeque<>();

    private int mouseX, mouseY = 0;

    private final LayerSystem layerSystem = new LayerSystem();

    private Game() {
        JFrame frame = new JFrame("Simple Game Framework");

        setPreferredSize(new Dimension(width, height));
        setFocusable(true);

        final WorldLayer worldLayer = new WorldLayer();
        final UILayer uiLayer = new UILayer(worldLayer);



        layerSystem.add(Layers.UI, uiLayer);
        layerSystem.add(Layers.WORLD, worldLayer);

        for (Layer layer : layerSystem.asList()) {
            System.out.println(layer);
        }

        // Input Listeners
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() < keys.length)
                    keys[e.getKeyCode()] = true;
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() < keys.length)
                    keys[e.getKeyCode()] = false;
            }

            @Override
            public void keyTyped(KeyEvent e) {
                for (Layer layer : layerSystem.asList()) {
                    layer.handleKeyEvent();
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
                worldLayer.zoomCamera(-e.getPreciseWheelRotation() * 0.1);
            } else {
                worldLayer.scrollSelectedType((int) e.getPreciseWheelRotation());
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {;
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });

        frame.setFocusable(true);

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

    public List<Layer> getLayers() {
        return List.copyOf(layerSystem.asList());
    }

    public void addLayer(LayerId layerId, Layer layer) {
        layerSystem.add(layerId, layer);
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
                // Pass a snapshot of inputs to the layers
                Queue<GameMouseEvent> eventsSnapshot = new ConcurrentLinkedDeque<>(mouseEvents);
                mouseEvents.clear(); // Clear so they aren't processed forever

                var inputHandled = false;

                for (Layer layer : layerSystem.asList()) {
                    // If input has been handled, it means something has already processed it, and that must mean no more layers are needing to handle input.
                    if (!inputHandled) {
                        inputHandled = layer.handleInput(delta, eventsSnapshot, mouseX, mouseY);
                    }

                    layer.update(delta);
                }
                delta--;
            }

            render();
        }

        stop();
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

        for (Layer layer : layerSystem.asList()) {
            layer.render(graphics);
        }

        graphics.dispose();
        bs.show();
    }

    @Override
    public boolean isKeyDown(int keyEvent) {
        return keyEvent >= 0 && keyEvent < keys.length && keys[keyEvent];
    }
}