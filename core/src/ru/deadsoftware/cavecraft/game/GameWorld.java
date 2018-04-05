package ru.deadsoftware.cavecraft.game;

public class GameWorld {

    private final int WIDTH, HEIGHT;
    private int[][] foreMap;
    private int[][] backMap;

    public GameWorld(int w, int h) {
        WIDTH = w;
        HEIGHT = h;
        foreMap = new int[WIDTH][HEIGHT];
        backMap = new int[WIDTH][HEIGHT];
    }

    public int getWidth() {
        return WIDTH;
    }

    public int getHeight() {
        return HEIGHT;
    }

    public int getForeMap(int x, int y) {
        return foreMap[x][y];
    }

    public void setForeMap(int x, int y, int value) {
        foreMap[x][y] = value;
    }

    public int getBackMap(int x, int y) {
        return backMap[x][y];
    }

    public void setBackMap(int x, int y, int value) {
        backMap[x][y] = value;
    }

}
