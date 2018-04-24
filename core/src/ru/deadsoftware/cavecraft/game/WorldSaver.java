package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import ru.deadsoftware.cavecraft.CaveGame;

import java.nio.ByteBuffer;

public class WorldSaver {

    private static final int VERSION = 0;

    private static int[][] fMap, bMap;
    private static int readIndex;

    private static int bytesInt(byte[] bytes) {
        ByteBuffer wrapped = ByteBuffer.wrap(bytes);
        int res = wrapped.getInt(readIndex);
        readIndex+=4;
        return res;
    }

    private static void writeInt(FileHandle file, int i, boolean append) {
        byte[] bytes = ByteBuffer.allocate(4).putInt(i).array();
        file.writeBytes(bytes, append);
    }
    
    private static void saveMap(FileHandle file, int[][] map) {
        int width = map.length;
        int height = map[0].length;
        writeInt(file, VERSION, false);
        writeInt(file, width, true);
        writeInt(file, height, true);
        for (int y=0; y<map[0].length; y++) {
            int bl = map[0][y];
            int rl = 1;
            for (int x=0; x<map.length; x++) {
                if (map[x][y]!=bl || x==map.length-1) {
                    writeInt(file, rl, true);
                    writeInt(file, bl, true);
                    rl=1;
                    bl=map[x][y];
                } else {
                    rl++;
                }
            }
        }
    }

    private static int[][] loadMap(FileHandle file) {
        int[][] map = null;
        int ver, width, height;
        int rl,bl;
        byte[] data = file.readBytes();
        readIndex = 0;
        ver = bytesInt(data);
        if (VERSION <= ver) {
            width = bytesInt(data);
            height = bytesInt(data);
            map = new int[width][height];
            for (int y=0; y<height; y++) {
                for (int x=0; x<width; x+=rl) {
                    rl = bytesInt(data);
                    bl = bytesInt(data);
                    for (int i=x; i<x+rl; i++) map[i][y] = bl;
                }
            }
        }
        return map;
    }

    public static int[][] getLoadedForeMap() {
        return fMap;
    }

    public static int[][] getLoadedBackMap() {
        return bMap;
    }

    public static void load() {
        fMap = loadMap(Gdx.files.absolute(CaveGame.GAME_FOLDER+"/saves/foremap.sav"));
        bMap = loadMap(Gdx.files.absolute(CaveGame.GAME_FOLDER+"/saves/backmap.sav"));
    }

    public static void save(int[][] foreMap, int[][] backMap) {
        Gdx.files.absolute(CaveGame.GAME_FOLDER+"/saves/").mkdirs();
        saveMap(Gdx.files.absolute(CaveGame.GAME_FOLDER+"/saves/foremap.sav"), foreMap);
        saveMap(Gdx.files.absolute(CaveGame.GAME_FOLDER+"/saves/backmap.sav"), backMap);
    }

    public static boolean exists() {
        return (Gdx.files.absolute(CaveGame.GAME_FOLDER+"/saves/foremap.sav").exists() &&
                Gdx.files.absolute(CaveGame.GAME_FOLDER+"/saves/backmap.sav").exists());
    }

}
