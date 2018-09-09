package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.utils.ArrayMap;
import ru.deadsoftware.cavecraft.game.objects.Block;

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
        BLOCKS.put("bed_l", new Block(22,false,true,true));
        BLOCKS.put("bed_r", new Block(23, false,true, true));
        BLOCKS.put("cobweb", new Block(24,false,false,true));
        BLOCKS.put("tallgrass", new Block(25,false,false,true));
        BLOCKS.put("deadbush", new Block(26,false,false,true));
        BLOCKS.put("brick_block", new Block(27));
        BLOCKS.put("dandelion", new Block(28,false,false,true));
        BLOCKS.put("rose", new Block(29,false,false,true));
        BLOCKS.put("brown_mushroom", new Block(30,false,false,true));
        BLOCKS.put("red_mushroom", new Block(31,false,false,true));
        BLOCKS.put("wool_while", new Block(32,true,false,false));
        BLOCKS.put("wool_orange", new Block(33,true,false,false));
        BLOCKS.put("wool_magenta", new Block(34,true,false,false));
        BLOCKS.put("wool_lightblue", new Block(35,true,false,false));
        BLOCKS.put("wool_yellow", new Block(36,true,false,false));
        BLOCKS.put("wool_lime", new Block(37,true,false,false));
        BLOCKS.put("wool_pink", new Block(38,true,false,false));
        BLOCKS.put("wool_gray", new Block(39,true,false,false));
        BLOCKS.put("wool_lightgray", new Block(40,true,false,false));
        BLOCKS.put("wool_cyan", new Block(41,true,false,false));
        BLOCKS.put("wool_purple", new Block(42,true,false,false));
        BLOCKS.put("wool_blue", new Block(43,true,false,false));
        BLOCKS.put("wool_brown", new Block(44,true,false,false));
        BLOCKS.put("wool_green", new Block(45,true,false,false));
        BLOCKS.put("wool_red", new Block(46,true,false,false));
        BLOCKS.put("wool_black", new Block(47,true,false,false));
        BLOCKS.put("gold_block", new Block(48));
        BLOCKS.put("iron_block", new Block(49));
        BLOCKS.put("stone_slab", new Block(0, 8, 16,8, 50, true, false, true));
        BLOCKS.put("double_stone_slab", new Block(51));
        BLOCKS.put("sandstone_slab", new Block(0, 8, 16,8, 52, true, false, true));
        BLOCKS.put("wooden_slab", new Block(0, 8, 16,8, 53, true, false, true));
        BLOCKS.put("cobblestone_slab", new Block(0, 8, 16,8, 54, true, false, true));
        BLOCKS.put("brick_slab", new Block(0, 8, 16,8, 55, true, false, true));
        BLOCKS.put("stonebrick", new Block(56));
        BLOCKS.put("stone_brick_slab", new Block(0, 8, 16,8, 57, true, false, true));
        BLOCKS.put("cactus", new Block(1, 0, 14, 16, 58, true, false, true));
    }

    public static void load() {
        loadBlocks();
    }

}
