package ru.deadsoftware.cavecraft.game;

public class WorldGen {

    private static int[][] foreMap, backMap;

    static void genWorld(int width, int height) {
        foreMap = new int[width][height];
        backMap = new int[width][height];
        for (int x=0; x<width; x++) {
            for (int y=height-6; y<height; y++) {
                foreMap[x][y]=1;
                backMap[x][y]=1;
            }
        }
    }

    static int[][] getForeMap() {
        return foreMap;
    }

    static int[][] getBackMap() {
        return backMap;
    }

    static void clear() {
        foreMap = null;
        backMap = null;
    }
}
