package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import ru.deadsoftware.cavecraft.Items;

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

    public int getForeMap(int x, int y) {
        int map = 0;
        try {
            if (x<0) {
                x = x % (getWidth());
                x = getWidth()- Math.abs(x);
            } else if (x>0) {
                x = x % (getWidth());
            }
            map = foreMap[x][y];
        } catch (ArrayIndexOutOfBoundsException e) {
            Gdx.app.error("GameWorld",e.toString());
        }
        return map;
    }

    public void setForeMap(int x, int y, int value) {
        try {
            if (x<0) {
                x = x % (getWidth());
                x = getWidth()- Math.abs(x);
            } else if (x>0) {
                x = x % (getWidth());
            }
            foreMap[x][y] = value;
        } catch (ArrayIndexOutOfBoundsException e) {
            Gdx.app.error("GameWorld", e.toString());
        }
    }

    public int getBackMap(int x, int y) {
        int map = 0;
        try {
            if (x<0) {
                x = x % (getWidth());
                x = getWidth()- Math.abs(x);
            } else if (x>0) {
                x = x % (getWidth());
            }
            map = backMap[x][y];
        } catch (ArrayIndexOutOfBoundsException e) {
            Gdx.app.error("GameWorld",e.toString());
        }
        return map;
    }

    public void setBackMap(int x, int y, int value) {
        try {
            if (x<0) {
                x = x % (getWidth());
                x = getWidth()- Math.abs(x);
            } else if (x>0) {
                x = x % (getWidth());
            }
            backMap[x][y] = value;
        } catch (ArrayIndexOutOfBoundsException e) {
            Gdx.app.error("GameWorld", e.toString());
        }
    }

    public void placeToForeground(int x, int y, int value) {
        if (getForeMap(x,y) == 0 || value == 0) {
            setForeMap(x,y,value);
        }
    }

    public void placeToBackground(int x, int y, int value) {
        if (value==0 || (getBackMap(x,y) == 0 && !Items.BLOCKS.getValueAt(value).foreground)) {
            setBackMap(x,y,value);
        }
    }

    public Vector2 getSpawnPoint(int x) {
        int y=0;
        while (true) {
            y++;
            if (getForeMap(x,y)>0 && Items.BLOCKS.getValueAt(getForeMap(x,y)).collision) break;
        }
        x = x*16 + 4;
        y = y*16 - 32;
        return new Vector2(x,y);
    }

    public void generate(int w, int h) {
        WIDTH = w;
        HEIGHT = h;
        WorldGen.genWorld(WIDTH,HEIGHT);
        foreMap = WorldGen.getForeMap();
        backMap = WorldGen.getBackMap();
        WorldGen.clear();
        save();
    }

    public void save() {
        WorldSaver.save(foreMap, backMap);
    }

    public void load() {
        WorldSaver.load();
        foreMap = WorldSaver.getLoadedForeMap();
        backMap = WorldSaver.getLoadedBackMap();
        WIDTH = foreMap.length;
        HEIGHT = foreMap[0].length;
    }

}
