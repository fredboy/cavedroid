package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.utils.TimeUtils;

public class WorldGen {

    private static RandomXS128 rand;
    private static long seed;

    private static int[][] foreMap, backMap;
    private static int[] hMap;

    public static long getSeed() {
        return seed;
    }

    static int[] genLandscape(int width, int mid, int min, int max) {
        int[] res = new int[width];
        int t;
        res[0] = mid;
        for (int i=1; i<width; i++) {
            t = rand.nextInt(3)-1;
            res[i] = res[i-1] + t;
            if (res[i]<min) res[i] = min;
            if (res[i]>max) res[i] = max;
        }
        return res;
    }

    static void genWorld(int width, int height) {
        genWorld(width, height, TimeUtils.millis());
    }

    static void genWorld(int width, int height, long s) {
        int dirtH;
        seed = s;
        rand = new RandomXS128(seed);
        foreMap = new int[width][height];
        backMap = new int[width][height];
        hMap = genLandscape(width, height/2, height/4, height/4*3);
        for (int x=0; x<width; x++) {
            dirtH = 4+rand.nextInt(2);
            for (int y = height- hMap[x]; y<height; y++) {
                if (y==height- hMap[x]) {
                    foreMap[x][y] = 2;
                    backMap[x][y] = 2;
                } else if (y<height-hMap[x]+dirtH) {
                    foreMap[x][y] = 3;
                    backMap[x][y] = 3;
                } else if (y<height-1){
                    foreMap[x][y] = 1;
                    backMap[x][y] = 1;
                } else {
                    foreMap[x][y] = 7;
                    backMap[x][y] = 7;
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
