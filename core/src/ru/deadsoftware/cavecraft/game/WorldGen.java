package ru.deadsoftware.cavecraft.game;

public class WorldGen {

    private static int[][] foreMap, backMap;

    static void genWorld(int width, int height) {
        foreMap = new int[width][height];
        backMap = new int[width][height];
        for (int x=0; x<width; x++) {
            for (int y=height-8; y<height; y++) {
                if (y==height-8) {
                    foreMap[x][y] = 3;
                    backMap[x][y] = 3;
                } else if (y<height-4) {
                    foreMap[x][y] = 2;
                    backMap[x][y] = 2;
                } else {
                    foreMap[x][y] = 1;
                    backMap[x][y] = 1;
                }
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
