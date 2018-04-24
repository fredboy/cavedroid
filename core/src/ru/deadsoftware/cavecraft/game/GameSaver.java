package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import ru.deadsoftware.cavecraft.CaveGame;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

public class GameSaver {

    private static final int VERSION = 0;

    private static int[][] fMap, bMap;
    private static int readIndex;

    private static int bytesToInt(byte[] bytes) {
        ByteBuffer wrapped = ByteBuffer.wrap(bytes);
        int res = wrapped.getInt(readIndex);
        readIndex+=4;
        return res;
    }

    private static byte[] intToBytes(int i) {
        return ByteBuffer.allocate(4).putInt(i).array();
    }

    private static void saveMap(FileHandle file, int[][] map) throws IOException{
        int rl,bl;
        int width = map.length;
        int height = map[0].length;
        BufferedOutputStream out = new BufferedOutputStream(file.write(false));
        out.write(intToBytes(VERSION));
        out.write(intToBytes(width));
        out.write(intToBytes(height));
        for (int y=0; y<map[0].length; y++) {
            bl = map[0][y];
            rl=0;
            for (int x=0; x<map.length; x++) {
                if (map[x][y]!=bl) {
                    out.write(intToBytes(rl));
                    out.write(intToBytes(bl));
                    rl=0;
                    bl=map[x][y];
                }
                rl++;
            }
            out.write(intToBytes(rl));
            out.write(intToBytes(bl));
        }
        out.flush();
        out.close();
    }

    private static int[][] loadMap(FileHandle file) throws Exception {
        int[][] map = null;
        int ver, width, height;
        int rl,bl;
        byte[] data = file.readBytes();
        readIndex = 0;
        ver = bytesToInt(data);
        if (VERSION == ver) {
            width = bytesToInt(data);
            height = bytesToInt(data);
            map = new int[width][height];
            for (int y=0; y<height; y++) {
                for (int x=0; x<width; x+=rl) {
                    rl = bytesToInt(data);
                    bl = bytesToInt(data);
                    for (int i=x; i<x+rl; i++) map[i][y] = bl;
                }
            }
        } else throw new Exception("version mismatch");
        return map;
    }

    public static int[][] getLoadedForeMap() {
        return fMap;
    }

    public static int[][] getLoadedBackMap() {
        return bMap;
    }

    public static void loadWorld() {
        try {
            fMap = loadMap(Gdx.files.absolute(CaveGame.GAME_FOLDER + "/saves/foremap.sav"));
            bMap = loadMap(Gdx.files.absolute(CaveGame.GAME_FOLDER + "/saves/backmap.sav"));
        } catch (Exception e) {
            Gdx.app.error("GameSaver",e.getMessage(),e);
        }
    }

    public static GameProc load() {
        FileHandle file = Gdx.files.absolute(CaveGame.GAME_FOLDER+"/saves/game.sav");
        GameProc gameProc = null;
        try {
            ObjectInputStream in = new ObjectInputStream(file.read());
            int ver = in.readInt();
            if (VERSION == ver) gameProc = (GameProc)in.readObject();
                else throw new Exception("version mismatch");
            in.close();
        } catch (Exception e) {
            Gdx.app.error("GameSaver",e.getMessage(),e);
        }
        gameProc.world = new GameWorld();
        gameProc.world.load();
        gameProc.physics = new GamePhysics(gameProc);
        gameProc.resetRenderer();
        fMap = null;
        bMap = null;
        return gameProc;
    }

    public static void save(GameProc gameProc) {
        FileHandle file = Gdx.files.absolute(CaveGame.GAME_FOLDER+"/saves/");
        file.mkdirs();
        file = Gdx.files.absolute(CaveGame.GAME_FOLDER+"/saves/game.sav");
        try {
            ObjectOutputStream out = new ObjectOutputStream(file.write(false));
            out.writeInt(VERSION);
            out.writeObject(gameProc);
            out.close();
            saveMap(Gdx.files.absolute(CaveGame.GAME_FOLDER+"/saves/foremap.sav"), gameProc.world.getFullForeMap());
            saveMap(Gdx.files.absolute(CaveGame.GAME_FOLDER+"/saves/backmap.sav"), gameProc.world.getFullBackMap());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean exists() {
        return (Gdx.files.absolute(CaveGame.GAME_FOLDER+"/saves/game.sav").exists() &&
                Gdx.files.absolute(CaveGame.GAME_FOLDER+"/saves/foremap.sav").exists() &&
                Gdx.files.absolute(CaveGame.GAME_FOLDER+"/saves/backmap.sav").exists());
    }

}
