package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ArrayMap;
import ru.deadsoftware.cavecraft.Items;

public class GameWorld {

    private final int WIDTH, HEIGHT;

    private int[][] foreMap;
    private int[][] backMap;

    public GameWorld(int w, int h) {
        WIDTH = w;
        HEIGHT = h;
        WorldGen.genWorld(WIDTH,HEIGHT);
        foreMap = WorldGen.getForeMap();
        backMap = WorldGen.getBackMap();
        WorldGen.clear();
    }

    public int getWidth() {
        return WIDTH;
    }

    public int getHeight() {
        return HEIGHT;
    }

    public int getForeMap(int x, int y) {
        int ret = 0;
        try {
            ret = foreMap[x][y];
        } catch (ArrayIndexOutOfBoundsException e) {
            Gdx.app.error("GameWorld",e.toString());
        }
        return ret;
    }

    public void setForeMap(int x, int y, int value) {
        try {
            foreMap[x][y] = value;
        } catch (ArrayIndexOutOfBoundsException e) {
            Gdx.app.error("GameWorld", e.toString());
        }
    }

    public int getBackMap(int x, int y) {
        int ret = 0;
        try {
            ret = backMap[x][y];
        } catch (ArrayIndexOutOfBoundsException e) {
            Gdx.app.error("GameWorld",e.toString());
        }
        return ret;
    }

    public void setBackMap(int x, int y, int value) {
        try {
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

    public Vector2 getSpawnPoint() {
        float x=0, y=0;
        boolean found = false;
        x = getWidth()/2;
        while (!found) {
            for (int i = 0; i < getHeight(); i++) {
                if (getForeMap((int)x, i)>0 &&
                        Items.BLOCKS.getValueAt(getForeMap((int)x, i)).collision) {
                    y = i-3;
                    found = true;
                    break;
                }
            }
            if (!found) x--;
        }
        x = x*16 + 4;
        y *= 16;
        return new Vector2(x,y);
    }

}
