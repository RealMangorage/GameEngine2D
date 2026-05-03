package org.mangorage.game;

public class Camera {

    private int x, y;
    private double zoom = 1.0;

    public void move(int dx, int dy) {
        x += dx;
        y += dy;
    }

    public void setZoom(double zoom) {
        this.zoom = Math.max(0.1, Math.min(zoom, 5.0)); // clamp
    }

    public void zoom(double amount) {
        setZoom(zoom + amount);
    }

    public double getZoom() {
        return zoom;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}