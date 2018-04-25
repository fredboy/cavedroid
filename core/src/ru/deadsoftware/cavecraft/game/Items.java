package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.utils.ArrayMap;
import ru.deadsoftware.cavecraft.game.objects.Block;
import ru.deadsoftware.cavecraft.misc.Assets;

public class Items {

    public static ArrayMap<String, Block> BLOCKS = new ArrayMap<String, Block>();

    public static void loadBlocks() {
        BLOCKS.put("none", null);
        BLOCKS.put("stone", new Block(0));
        BLOCKS.put("grass", new Block(1));
        BLOCKS.put("dirt", new Block(2));
        BLOCKS.put("cobblestone", new Block(3));
        BLOCKS.put("planks", new Block(4));
        BLOCKS.put("sapling", new Block(5,false,false,true));
        BLOCKS.put("bedrock", new Block(6));
        BLOCKS.put("water", new Block(7,false,false,true));
        BLOCKS.put("lava", new Block(8,false,false,false));
        BLOCKS.put("sand", new Block(9));
        BLOCKS.put("gravel", new Block(10));
        BLOCKS.put("gold_ore", new Block(11));
        BLOCKS.put("iron_ore", new Block(12));
        BLOCKS.put("coal_ore", new Block(13));
        BLOCKS.put("log", new Block(14));
        BLOCKS.put("leaves", new Block(15));
        BLOCKS.put("sponge", new Block(16));
        BLOCKS.put("glass", new Block(17,true,false,true));
        BLOCKS.put("lapis_ore", new Block(18));
        BLOCKS.put("lapis_block", new Block(19));
        BLOCKS.put("sandstone", new Block(20));
        BLOCKS.put("noteblock", new Block(21));
        BLOCKS.put("bed", new Block(0,8,16,8,22,false,true,true));
        BLOCKS.put("cobweb", new Block(24,false,false,true));
        BLOCKS.put("tallgrass", new Block(25,false,false,true));
        BLOCKS.put("deadbush", new Block(26,false,false,true));
        BLOCKS.put("brick_block", new Block(27));
        BLOCKS.put("dandelion", new Block(28,false,false,true));
        BLOCKS.put("rose", new Block(29,false,false,true));
        BLOCKS.put("brown_mushroom", new Block(30,false,false,true));
        BLOCKS.put("red_mushroom", new Block(31,false,false,true));
        BLOCKS.put("wool", new Block(32,false,true,false));
    }

    public static void load() {
        loadBlocks();
    }

}
