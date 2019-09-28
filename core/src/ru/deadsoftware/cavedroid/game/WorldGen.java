package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.utils.TimeUtils;

class WorldGen {

    private static RandomXS128 rand;
    private static long seed;

    private static int[][] foreMap, backMap;
    private static int[] hMap;
    private static int[] bMap; //biomes, 0-plains, 1-desert

    public static long getSeed() {
        return seed;
    }

    static int[] genLandscape(int width, int mid, int min, int max) {
        int[] res = new int[width];
        bMap = new int[width];
        int t;
        res[0] = mid;
        for (int i = 1; i < width; i++) {
            t = rand.nextInt(7) - 3;
            if (t > -3 && t < 3) t = 0;
            else t /= Math.abs(t);
            if (i > width - (max - min)) {
                if (res[i - 1] + t < res[0]) t = Math.abs(t);
                else if (res[i - 1] + t > res[0]) t = -Math.abs(t);
            }
            res[i] = res[i - 1] + t;
            if (res[i] < min) res[i] = min;
            if (res[i] > max) res[i] = max;
            bMap[i] = 0;
//            if (i >= width / 2) {
//                bMap[i] = 1;
//                if (res[i] < 60) res[i] = 60;
//            } else {
//                bMap[i] = 0;
//            }
        }
        if (res[0] < res[width - 1]) res[width - 1] = res[0];
        return res;
    }

    private static void genCactus(int x, int y) {
        foreMap[x][y] = 59;
        foreMap[x][y - 1] = 59;
        foreMap[x][y - 2] = 59;
    }

    private static void genOak(int x, int y) {
        backMap[x][y] = 15;
        backMap[x][y - 1] = 15;
        backMap[x][y - 2] = 15;
        backMap[x][y - 3] = 15;
        backMap[x][y - 4] = 16;
        backMap[x][y - 5] = 16;
        backMap[x - 1][y - 3] = 16;
        backMap[x - 1][y - 4] = 16;
        backMap[x + 1][y - 3] = 16;
        backMap[x + 1][y - 4] = 16;
        foreMap[x][y - 3] = 16;
        foreMap[x][y - 4] = 16;
        foreMap[x][y - 5] = 16;
        foreMap[x - 1][y - 3] = 16;
        foreMap[x - 1][y - 4] = 16;
        foreMap[x + 1][y - 3] = 16;
        foreMap[x + 1][y - 4] = 16;
    }

    static void genWorld(int width, int height) {
        genWorld(width, height, TimeUtils.millis());
    }

    static void genWorld(int width, int height, long worldseed) {
        int dirtH;
        seed = worldseed;
        rand = new RandomXS128(seed);
        foreMap = new int[width][height];
        backMap = new int[width][height];
        hMap = genLandscape(width, height / 4, height / 8, height / 2);
        for (int x = 0; x < width; x++) {
            dirtH = 4 + rand.nextInt(2);
            for (int y = height - hMap[x]; y < height; y++) {
                if (y == height - hMap[x]) {
                    switch (bMap[x]) {
                        case 0:
                            foreMap[x][y] = 2;
                            backMap[x][y] = 2;
                            break;
                        case 1:
                            foreMap[x][y] = 10;
                            backMap[x][y] = 10;
                            break;
                    }
                } else if (y < height - hMap[x] + dirtH) {
                    switch (bMap[x]) {
                        case 0:
                            foreMap[x][y] = 3;
                            backMap[x][y] = 3;
                            break;
                        case 1:
                            foreMap[x][y] = 10;
                            backMap[x][y] = 10;
                            break;
                    }
                } else if (bMap[x] == 1 && y < height - hMap[x] + dirtH + 3) {
                    foreMap[x][y] = 21;
                    backMap[x][y] = 21;
                } else if (y < height - 1) {
                    foreMap[x][y] = 1;
                    backMap[x][y] = 1;
                } else {
                    foreMap[x][y] = 7;
                    backMap[x][y] = 7;
                }
            }
            for (int y = height - 60; y < height - 1; y++) {
                if (foreMap[x][y] == 0 && bMap[x] != 1) {
                    foreMap[x][y] = 8;
                    if (bMap[x] == 0) {
                        if (y == height - 60) {
                            backMap[x][y] = 2;
                        } else {
                            backMap[x][y] = 3;
                        }
                    }
                    if (y == height - hMap[x] - 1) {
                        foreMap[x][y + 1] = 3;
                        backMap[x][y + 1] = 3;
                    }
                }
            }
            if (x > 2 && x < width - 2) {
                if (foreMap[x][height - hMap[x] - 1] == 0 && foreMap[x][height - hMap[x]] == 2) {
                    switch (rand.nextInt(50)) {
                        case 0:
                            genOak(x, height - hMap[x] - 1);
                            break;
                        case 1:
                            foreMap[x][height - hMap[x] - 1] = 26;
                            break;
                        case 2:
                            foreMap[x][height - hMap[x] - 1] = 29;
                            break;
                        case 3:
                            foreMap[x][height - hMap[x] - 1] = 30;
                            break;
                        case 4:
                            foreMap[x][height - hMap[x] - 1] = 31;
                            break;
                        case 5:
                            foreMap[x][height - hMap[x] - 1] = 32;
                            break;
                    }
                }
                if (foreMap[x][height - hMap[x] - 1] == 0 && foreMap[x][height - hMap[x]] == 10) {
                    switch (rand.nextInt(20)) {
                        case 0:
                            genCactus(x, height - hMap[x] - 1);
                            break;
                        case 1:
                            foreMap[x][height - hMap[x] - 1] = 27;
                            break;
                    }
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
        hMap = null;
        bMap = null;
    }
}
