package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.utils.TimeUtils;

public class WorldGen {

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
        for (int i=1; i<width; i++) {
            bMap[i] = i/(width/2);
            t = rand.nextInt(7)-3;
            if (t>-3 && t<3) t=0; else t/=Math.abs(t);
            if (i>width-(max-min)) {
                if (res[i-1]+t<res[0]) t=Math.abs(t);
                    else if (res[i-1]+t>res[0]) t=-Math.abs(t);
            }
            res[i] = res[i-1] + t;
            if (res[i]<min) res[i] = min;
            if (res[i]>max) res[i] = max;
        }
        return res;
    }

    private static void genCactus(int x, int y) {
        foreMap[x][y] = 59;
        foreMap[x][y-1] = 59;
        foreMap[x][y-2] = 59;
    }

    private static void genOak(int x, int y) {
        backMap[x][y] = 15;
        backMap[x][y-1] = 15;
        backMap[x][y-2] = 15;
        backMap[x][y-3] = 15;
        backMap[x][y-4] = 16;
        backMap[x][y-5] = 16;
        backMap[x-1][y-3] = 16;
        backMap[x-1][y-4] = 16;
        backMap[x+1][y-3] = 16;
        backMap[x+1][y-4] = 16;
        foreMap[x][y-3] = 16;
        foreMap[x][y-4] = 16;
        foreMap[x][y-5] = 16;
        foreMap[x-1][y-3] = 16;
        foreMap[x-1][y-4] = 16;
        foreMap[x+1][y-3] = 16;
        foreMap[x+1][y-4] = 16;
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
        hMap = genLandscape(width, height/8*3, height/8, height/2);
        for (int x=0; x<width; x++) {
            dirtH = 4+rand.nextInt(2);
            for (int y = height- hMap[x]; y<height; y++) {
                if (y==height- hMap[x]) {
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
                } else if (y<height-hMap[x]+dirtH) {
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
                } else if (bMap[x]==1 && y<height-hMap[x]+dirtH+3) {
                    foreMap[x][y] = 21;
                    backMap[x][y] = 21;
                } else if (y<height-1){
                    foreMap[x][y] = 1;
                    backMap[x][y] = 1;
                } else {
                    if (bMap[x]==0) {
                        foreMap[x][y] = 7;
                        backMap[x][y] = 7;
                    }
                }
            }
            for (int y = height-60; y<height-1; y++) {
                if (foreMap[x][y]==0 && bMap[x]!=1){
                    foreMap[x][y] = 8;
                    backMap[x][y] = 8;
                    if (y==height-hMap[x]-1) {
                        foreMap[x][y+1] = 3;
                    }
                }
            }
            if (x>2 && x<width-2 && rand.nextInt(100)<5){
                if (foreMap[x][height-hMap[x]-1]==0 && foreMap[x][height-hMap[x]]==2) {
                    genOak(x,height-hMap[x]-1);
                }
                if (foreMap[x][height-hMap[x]-1]==0 && foreMap[x][height-hMap[x]]==10) {
                    genCactus(x,height-hMap[x]-1);
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
