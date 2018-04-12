package ru.deadsoftware.cavecraft;

import com.badlogic.gdx.utils.ArrayMap;
import ru.deadsoftware.cavecraft.game.objects.Block;

public class Items {

    public static ArrayMap<String, Block> BLOCKS = new ArrayMap<String, Block>();

    public static void load() {
        BLOCKS.put("none", null);
        BLOCKS.put("stone", new Block(0,0,16,16,Assets.blockTextures[0]));
        BLOCKS.put("grass", new Block(0,0,16,16,Assets.blockTextures[1]));
        BLOCKS.put("dirt", new Block(0,0,16,16,Assets.blockTextures[2]));
        BLOCKS.put("cobblestone", new Block(0,0,16,16,Assets.blockTextures[3]));
        BLOCKS.put("planks", new Block(0,0,16,16,Assets.blockTextures[4]));
        BLOCKS.put("sapling", new Block(0,0,16,16,Assets.blockTextures[5],false,true));
        BLOCKS.put("bedrock", new Block(0,0,16,16,Assets.blockTextures[6]));
        BLOCKS.put("water", new Block(0,0,16,16,Assets.blockTextures[7],false,true));
        BLOCKS.put("lava", new Block(0,0,16,16,Assets.blockTextures[8],false,true));
    }

}
