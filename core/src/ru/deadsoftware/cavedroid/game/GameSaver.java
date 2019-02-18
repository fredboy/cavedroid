package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import ru.deadsoftware.cavedroid.CaveGame;

import java.io.*;
import java.nio.ByteBuffer;

public class GameSaver {

    private static final int VERSION = 0;

    private static byte[] intToBytes(int i) {
        return ByteBuffer.allocate(4).putInt(i).array();
    }

    private static void saveMap(FileHandle file, int[][] map) throws IOException {
        int rl, bl;
        int width = map.length;
        int height = map[0].length;
        BufferedOutputStream out = new BufferedOutputStream(file.write(false));
        out.write(intToBytes(VERSION));
        out.write(intToBytes(width));
        out.write(intToBytes(height));
        for (int y = 0; y < height; y++) {
            bl = map[0][y];
            rl = 0;
            for (int x = 0; x < width; x++) {
                if (map[x][y] != bl) {
                    out.write(intToBytes(rl));
                    out.write(intToBytes(bl));
                    rl = 0;
                    bl = map[x][y];
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
        int[][] map;
        int ver, width, height;
        int rl, bl;
        DataInputStream in = new DataInputStream(file.read());
        ver = in.readInt();
        if (VERSION == ver) {
            width = in.readInt();
            height = in.readInt();
            map = new int[width][height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x += rl) {
                    rl = in.readInt();
                    bl = in.readInt();
                    for (int i = x; i < x + rl; i++) map[i][y] = bl;
                }
            }
            in.close();
        } else throw new Exception("version mismatch");
        return map;
    }

    public static GameProc load() {
        FileHandle file = Gdx.files.absolute(CaveGame.GAME_FOLDER + "/saves/game.sav");
        GameProc gameProc = null;
        try {
            ObjectInputStream in = new ObjectInputStream(file.read());
            int ver = in.readInt();
            if (VERSION == ver) gameProc = (GameProc) in.readObject();
            else throw new Exception("version mismatch");
            in.close();
            gameProc.world = new GameWorld();
            gameProc.world.setMaps(
                    loadMap(Gdx.files.absolute(CaveGame.GAME_FOLDER + "/saves/foremap.sav")),
                    loadMap(Gdx.files.absolute(CaveGame.GAME_FOLDER + "/saves/backmap.sav"))
            );
            gameProc.physics = new GamePhysics();
            gameProc.resetRenderer();
        } catch (Exception e) {
            Gdx.app.error("GameSaver", e.getMessage(), e);
            Gdx.app.exit();
        }
        return gameProc;
    }

    public static void save(GameProc gp) {
        FileHandle file = Gdx.files.absolute(CaveGame.GAME_FOLDER + "/saves/");
        file.mkdirs();
        file = Gdx.files.absolute(CaveGame.GAME_FOLDER + "/saves/game.sav");
        try {
            ObjectOutputStream out = new ObjectOutputStream(file.write(false));
            out.writeInt(VERSION);
            out.writeObject(gp);
            out.close();
            saveMap(Gdx.files.absolute(CaveGame.GAME_FOLDER + "/saves/foremap.sav"), gp.world.getFullForeMap());
            saveMap(Gdx.files.absolute(CaveGame.GAME_FOLDER + "/saves/backmap.sav"), gp.world.getFullBackMap());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean exists() {
        return (Gdx.files.absolute(CaveGame.GAME_FOLDER + "/saves/game.sav").exists() &&
                Gdx.files.absolute(CaveGame.GAME_FOLDER + "/saves/foremap.sav").exists() &&
                Gdx.files.absolute(CaveGame.GAME_FOLDER + "/saves/backmap.sav").exists());
    }

}
