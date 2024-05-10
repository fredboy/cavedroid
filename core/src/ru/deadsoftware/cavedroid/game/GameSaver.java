package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import ru.deadsoftware.cavedroid.MainConfig;
import ru.deadsoftware.cavedroid.game.mobs.MobsController;
import ru.deadsoftware.cavedroid.game.model.block.Block;
import ru.deadsoftware.cavedroid.game.objects.drop.DropController;
import ru.deadsoftware.cavedroid.game.objects.container.ContainerController;
import ru.deadsoftware.cavedroid.game.world.GameWorld;

import javax.annotation.CheckForNull;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class GameSaver {

    private static final String TAG = "GameSaver";

    public static class Data {
        @CheckForNull
        private MobsController mMobsController;
        @CheckForNull
        private DropController mDropController;
        @CheckForNull
        private ContainerController mContainerController;
        @CheckForNull
        private Block[][] mForeMap, mBackMap;

        public Data(MobsController mobsController,
                    DropController dropController,
                    ContainerController containerController,
                    Block[][] foreMap,
                    Block[][] backMap) {
            mMobsController = mobsController;
            mDropController = dropController;
            mContainerController = containerController;
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

        public ContainerController retrieveFurnaceController() {
            assert mContainerController != null;
            ContainerController containerController = mContainerController;
            mContainerController = null;
            return containerController;
        }

        public Block[][] retrieveForeMap() {
            assert mForeMap != null;
            Block[][] foreMap = mForeMap;
            mForeMap = null;
            return foreMap;
        }

        public Block[][] retrieveBackMap() {
            assert mBackMap != null;
            Block[][] backMap = mBackMap;
            mBackMap = null;
            return backMap;
        }

        public boolean isEmpty() {
            return mMobsController == null &&
                    mDropController == null &&
                    mContainerController == null &&
                    mForeMap == null &&
                    mBackMap == null;
        }
    }

    private static final int SAVE_VERSION = 1;

    private static byte[] intToBytes(int i) {
        return ByteBuffer.allocate(4).putInt(i).array();
    }

    private static Map<String, Integer> buildBlocksDictionary(Block[][] foreMap, Block[][] backMap) {
        final HashMap<String, Integer> dict = new HashMap<>();

        int id = 0;
        for (int i = 0; i < foreMap.length; i++) {
            for (int j = 0; j < foreMap[i].length; j++) {
                for (int k = 0; k < 2; k++) {
                    final Block block = k == 0 ? foreMap[i][j] : backMap[i][j];
                    final String key = block.getParams().getKey();
                    if (!dict.containsKey(key)) {
                        dict.put(key, id++);
                    }
                }
            }
        }

        return dict;
    }

    private static void saveDict(FileHandle file, Map<String, Integer> dict) {
        final String[] arr = new String[dict.size()];

        for (Map.Entry<String, Integer> entry : dict.entrySet()) {
            arr[entry.getValue()] = entry.getKey();
        }

        final StringBuilder builder = new StringBuilder();
        for (String key : arr) {
            builder.append(key);
            builder.append('\n');
        }

        file.writeString(builder.toString(), false);
    }

    private static String[] loadDict(FileHandle file) {
        return file.readString().split("\n");
    }

    private static void saveMap(FileHandle file, Block[][] map, Map<String, Integer> dict) throws IOException {
        int run, block;
        int width = map.length;
        int height = map[0].length;

        BufferedOutputStream out = new BufferedOutputStream(file.write(false));

        out.write(SAVE_VERSION);
        out.write(intToBytes(width));
        out.write(intToBytes(height));

        for (int y = 0; y < height; y++) {
            block = dict.get(map[0][y].getParams().getKey());
            run = 0;
            for (Block[] blocks : map) {
                int newValue = dict.get(blocks[y].getParams().getKey());
                if (run >= 0xFF || newValue != block) {
                    out.write(run);
                    out.write(block);
                    run = 0;
                    block = dict.get(blocks[y].getParams().getKey());
                }
                run++;
            }
            out.write(run);
            out.write(block);
        }

        out.flush();
        out.close();
    }

    private static Block[][] loadMap(GameItemsHolder gameItemsHolder, FileHandle file, String[] dict) throws Exception {
        Block[][] map;
        int version, width, height;
        int run, block;

        DataInputStream in = new DataInputStream(file.read());

        version = in.readByte();

        if (SAVE_VERSION == version) {
            width = in.readInt();
            height = in.readInt();
            map = new Block[width][height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x += run) {
                    run = in.readUnsignedByte();
                    block = in.readUnsignedByte();
                    for (int i = x; i < x + run; i++) {
                        map[i][y] = gameItemsHolder.getBlock(dict[block]);
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
    public static Data load(MainConfig mainConfig, GameItemsHolder gameItemsHolder) {
        String folder = mainConfig.getGameFolder();
        FileHandle file = Gdx.files.absolute(folder + "/saves/game.sav");

        try {
            ObjectInputStream in = new ObjectInputStream(file.read());
            int version = in.readInt();
            DropController dropController;
            MobsController mobsController;
            ContainerController containerController;

            if (SAVE_VERSION == version) {
                dropController = (DropController) in.readObject();
                mobsController = (MobsController) in.readObject();
                containerController = (ContainerController) in.readObject();
            } else {
                throw new Exception("version mismatch");
            }

            in.close();

            final String[] dict = loadDict(Gdx.files.absolute(mainConfig.getGameFolder() + "/saves/dict"));
            Block[][] foreMap = loadMap(gameItemsHolder, Gdx.files.absolute(mainConfig.getGameFolder() + "/saves/foremap.sav"), dict);
            Block[][] backMap = loadMap(gameItemsHolder, Gdx.files.absolute(mainConfig.getGameFolder() + "/saves/backmap.sav"), dict);

            if (dropController == null || mobsController == null) {
                throw new Exception("couldn't load");
            }

            return new Data(mobsController, dropController, containerController, foreMap, backMap);
        } catch (Exception e) {
            Gdx.app.error("GameSaver", e.getMessage());
        }

        return null;
    }

    public static void save(MainConfig mainConfig,
                            DropController dropController,
                            MobsController mobsController,
                            ContainerController containerController,
                            GameWorld gameWorld) {
        String folder = mainConfig.getGameFolder();
        FileHandle file = Gdx.files.absolute(folder + "/saves/");
        file.mkdirs();
        file = Gdx.files.absolute(folder + "/saves/game.sav");

        final Block[][] foreMap, backMap;
        foreMap = gameWorld.getFullForeMap();
        backMap = gameWorld.getFullBackMap();

        final Map<String, Integer> dict = buildBlocksDictionary(foreMap, backMap);

        try {
            ObjectOutputStream out = new ObjectOutputStream(file.write(false));
            out.writeInt(SAVE_VERSION);
            out.writeObject(dropController);
            out.writeObject(mobsController);
            out.writeObject(containerController);
            out.close();

            saveDict(Gdx.files.absolute(folder + "/saves/dict"), dict);
            saveMap(Gdx.files.absolute(folder + "/saves/foremap.sav"), gameWorld.getFullForeMap(), dict);
            saveMap(Gdx.files.absolute(folder + "/saves/backmap.sav"), gameWorld.getFullBackMap(), dict);
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
