package org.mangorage.game;

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
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Game extends Canvas implements Runnable, InputHandler {
    private static final Game INSTANCE = new Game();

    public static Game getInstance() {
        return INSTANCE;
    }

    private boolean running = false;
    private Thread gameThread;

    private final int width = 800;
    private final int height = 600;

    private final boolean[] keys = new boolean[256];
    private final Queue<GameMouseEvent> mouseEvents = new ConcurrentLinkedDeque<>();

    private int mouseX, mouseY = 0;

    private final List<Layer> layers = new ArrayList<>();

    private final WorldLayer worldLayer = new WorldLayer();
    private final UILayer uiLayer = new UILayer(worldLayer);

    private Game() {
        JFrame frame = new JFrame("Simple Game Framework");

        setPreferredSize(new Dimension(width, height));
        setFocusable(true);

        layers.add(worldLayer);
        layers.add(uiLayer);

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
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseEvents.add(new GameMouseEvent(e.getX(), e.getY(), e.getButton()));
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
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
                worldLayer.zoomCamera(-e.getPreciseWheelRotation() * 0.1);
            } else {
                worldLayer.scrollSelectedType((int) e.getPreciseWheelRotation());
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

    public List<Layer> getLayers() {
        return List.copyOf(layers);
    }

    public void addLayer(Layer layer) {
        layers.add(layer);
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

                for (Layer layer : layers) {
                    layer.handleInput(delta, eventsSnapshot);
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

        for (Layer layer : layers) {
            layer.render(graphics);
        }

        graphics.dispose();
        bs.show();
    }

    @Override
    public boolean isKeyDown(int keyEvent) {
        return keyEvent >= 0 && keyEvent < keys.length && keys[keyEvent];
    }

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }
}