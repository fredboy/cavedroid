package ru.deadsoftware.cavecraft;

import com.badlogic.gdx.utils.ArrayMap;
import ru.deadsoftware.cavecraft.game.objects.Block;

public class BlocksLoader {

    public static ArrayMap<String, Block> BLOCKS = new ArrayMap<String, Block>();

    public static void load() {
        BLOCKS.put("none", null);
        BLOCKS.put("stone", new Block(0,0,16,16,Assets.blockTextures[0]));
        BLOCKS.put("dirt", new Block(0,0,16,16,Assets.blockTextures[1]));
        BLOCKS.put("grass", new Block(0,0,16,16,Assets.blockTextures[2]));
    }

}
