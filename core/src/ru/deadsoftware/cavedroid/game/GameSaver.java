package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import ru.deadsoftware.cavedroid.MainConfig;
import ru.deadsoftware.cavedroid.game.mobs.MobsController;
import ru.deadsoftware.cavedroid.game.objects.DropController;

import javax.annotation.CheckForNull;
import java.io.*;
import java.nio.ByteBuffer;

public class GameSaver {

    public static class Data {
        @CheckForNull
        private MobsController mMobsController;
        @CheckForNull
        private DropController mDropController;
        @CheckForNull
        private int[][] mForeMap, mBackMap;

        public Data(MobsController mobsController, DropController dropController, int[][] foreMap, int[][] backMap) {
            mMobsController = mobsController;
            mDropController = dropController;
            mForeMap = foreMap;
            mBackMap = backMap;
        }

        public MobsController retrieveMobsController() {
            assert mMobsController != null;
            MobsController mobsController = mMobsController;
            mMobsController = null;
            return mobsController;
        }

        public DropController retrieveDropController() {
            assert mDropController != null;
            DropController dropController = mDropController;
            mDropController = null;
            return dropController;
        }

        public int[][] retrieveForeMap() {
            assert mForeMap != null;
            int[][] foreMap = mForeMap;
            mForeMap = null;
            return foreMap;
        }

        public int[][] retrieveBackMap() {
            assert mBackMap != null;
            int[][] backMap = mBackMap;
            mBackMap = null;
            return backMap;
        }

        public boolean isEmpty() {
            return mMobsController == null && mDropController == null && mForeMap == null && mBackMap == null;
        }
    }

    private static final int SAVE_VERSION = 1;

    private static byte[] intToBytes(int i) {
        return ByteBuffer.allocate(4).putInt(i).array();
    }

    private static void saveMap(FileHandle file, int[][] map) throws IOException {
        int run, block;
        int width = map.length;
        int height = map[0].length;

        BufferedOutputStream out = new BufferedOutputStream(file.write(false));

        out.write(intToBytes(SAVE_VERSION));
        out.write(intToBytes(width));
        out.write(intToBytes(height));

        for (int y = 0; y < height; y++) {
            block = map[0][y];
            run = 0;
            for (int[] ints : map) {
                if (ints[y] != block) {
                    out.write(intToBytes(run));
                    out.write(intToBytes(block));
                    run = 0;
                    block = ints[y];
                }
                run++;
            }
            out.write(intToBytes(run));
            out.write(intToBytes(block));
        }

        out.flush();
        out.close();
    }

    private static int[][] loadMap(FileHandle file) throws Exception {
        int[][] map;
        int version, width, height;
        int run, block;

        DataInputStream in = new DataInputStream(file.read());

        version = in.readInt();

        if (SAVE_VERSION == version) {
            width = in.readInt();
            height = in.readInt();
            map = new int[width][height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x += run) {
                    run = in.readInt();
                    block = in.readInt();
                    for (int i = x; i < x + run; i++) {
                        map[i][y] = block;
                    }
                }
            }
        } else {
            throw new Exception("version mismatch");
        }

        in.close();
        return map;
    }

    @CheckForNull
    public static Data load(MainConfig mainConfig) {
        String folder = mainConfig.getGameFolder();
        FileHandle file = Gdx.files.absolute(folder + "/saves/game.sav");

        try {
            ObjectInputStream in = new ObjectInputStream(file.read());
            int version = in.readInt();
            DropController dropController;
            MobsController mobsController;

            if (SAVE_VERSION == version) {
                dropController = (DropController) in.readObject();
                mobsController = (MobsController) in.readObject();
            } else {
                throw new Exception("version mismatch");
            }

            in.close();

            int[][] foreMap = loadMap(Gdx.files.absolute(mainConfig.getGameFolder() + "/saves/foremap.sav"));
            int[][] backMap = loadMap(Gdx.files.absolute(mainConfig.getGameFolder() + "/saves/backmap.sav"));

            if (dropController == null || mobsController == null) {
                throw new Exception("couldn't load");
            }

            return new Data(mobsController, dropController, foreMap, backMap);
        } catch (Exception e) {
            Gdx.app.error("GameSaver", e.getMessage());
        }

        return null;
    }

    public static void save(MainConfig mainConfig,
                            DropController dropController,
                            MobsController mobsController,
                            GameWorld gameWorld) {

        String folder = mainConfig.getGameFolder();
        FileHandle file = Gdx.files.absolute(folder + "/saves/");
        file.mkdirs();
        file = Gdx.files.absolute(folder + "/saves/game.sav");

        try {
            ObjectOutputStream out = new ObjectOutputStream(file.write(false));
            out.writeInt(SAVE_VERSION);
            out.writeObject(dropController);
            out.writeObject(mobsController);
            out.close();
            saveMap(Gdx.files.absolute(folder + "/saves/foremap.sav"), gameWorld.getFullForeMap());
            saveMap(Gdx.files.absolute(folder + "/saves/backmap.sav"), gameWorld.getFullBackMap());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean exists(MainConfig mainConfig) {
        String folder = mainConfig.getGameFolder();
        return (Gdx.files.absolute(folder + "/saves/game.sav").exists() &&
                Gdx.files.absolute(folder + "/saves/foremap.sav").exists() &&
                Gdx.files.absolute(folder + "/saves/backmap.sav").exists());
    }
}
