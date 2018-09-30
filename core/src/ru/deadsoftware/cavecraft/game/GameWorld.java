package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.math.Vector2;

public class GameWorld {

    private int WIDTH, HEIGHT;
    private int[][] foreMap;
    private int[][] backMap;

    public int getWidth() {
        return WIDTH;
    }

    public int getHeight() {
        return HEIGHT;
    }

    public int[][] getFullForeMap() {
        return foreMap;
    }

    public int[][] getFullBackMap() {
        return backMap;
    }

    private int transformX(int x) {
        x = x % getWidth();
        if (x < 0) x = getWidth() - Math.abs(x);
        return x;
    }

    public int getForeMap(int x, int y) {
        int map = 0;
        try {
            x = transformX(x);
            map = foreMap[x][y];
        } catch (ArrayIndexOutOfBoundsException e) {
            //Gdx.app.error("GameWorld",e.toString());
        }
        return map;
    }

    public void setForeMap(int x, int y, int value) {
        try {
            x = transformX(x);
            foreMap[x][y] = value;
        } catch (ArrayIndexOutOfBoundsException e) {
            //Gdx.app.error("GameWorld", e.toString());
        }
    }

    public int getBackMap(int x, int y) {
        int map = 0;
        try {
            x = transformX(x);
            map = backMap[x][y];
        } catch (ArrayIndexOutOfBoundsException e) {
            //Gdx.app.error("GameWorld",e.toString());
        }
        return map;
    }

    public void setBackMap(int x, int y, int value) {
        try {
            x = transformX(x);
            backMap[x][y] = value;
        } catch (ArrayIndexOutOfBoundsException e) {
            //Gdx.app.error("GameWorld", e.toString());
        }
    }

    private void placeSlab(int x, int y, int value) {
        switch (value) {
            case 51:
                setForeMap(x, y, 52);
                break;
            case 53:
                setForeMap(x, y, 21);
                break;
            case 54:
                setForeMap(x, y, 5);
                break;
            case 55:
                setForeMap(x, y, 4);
                break;
            case 56:
                setForeMap(x, y, 28);
                break;
            case 58:
                setForeMap(x, y, 57);
                break;
        }
    }

    public void placeToForeground(int x, int y, int value) {
        if (getForeMap(x, y) == 0 || value == 0 || !Items.BLOCKS.getValueAt(getForeMap(x, y)).collision) {
            setForeMap(x, y, value);
        } else if (Items.isSlab(value) && getForeMap(x, y) == value) {
            placeSlab(x, y, value);
        }
        GameProc.UPD_X = x - 8;
        GameProc.UPD_Y = y - 8;
        GameProc.DO_UPD = true;
    }

    public void placeToBackground(int x, int y, int value) {
        if (value == 0 || (getBackMap(x, y) == 0 && Items.BLOCKS.getValueAt(value).collision) &&
                (!Items.BLOCKS.getValueAt(value).transparent || value == 18)) {
            setBackMap(x, y, value);
        }
    }

    public Vector2 getSpawnPoint() {
        int x = 0, y = 0;
        while (true) {
            y++;
            if (getForeMap(x, y) > 0 && Items.BLOCKS.getValueAt(getForeMap(x, y)).collision) break;
        }
        x = x * 16 + 4;
        y = y * 16 - 32;
        return new Vector2(x, y);
    }

    public void generate(int w, int h) {
        WIDTH = w;
        HEIGHT = h;
        WorldGen.genWorld(WIDTH, HEIGHT);
        foreMap = WorldGen.getForeMap();
        backMap = WorldGen.getBackMap();
        WorldGen.clear();
    }

    public void setMaps(int[][] foreMap, int[][] backMap) {
        this.foreMap = foreMap.clone();
        this.backMap = backMap.clone();
        WIDTH = foreMap.length;
        HEIGHT = foreMap[0].length;
    }

}
