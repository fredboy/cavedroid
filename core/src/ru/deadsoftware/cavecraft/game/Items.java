package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.utils.ArrayMap;
import ru.deadsoftware.cavecraft.game.objects.Block;

public class Items {

    public static ArrayMap<String, Block> BLOCKS = new ArrayMap<String, Block>();

    public static boolean isFluid(int bl) {
        return (bl == 8 || bl == 9 || bl == 60 || bl == 61 || bl == 62 || bl == 63 || bl == 64 || bl == 65);
    }

    public static boolean isWater(int bl) {
        return (bl == 8 || bl == 60 || bl == 61 || bl == 62);
    }

    public static boolean isLava(int bl) {
        return (bl == 9 || bl == 63 || bl == 64 || bl == 65);
    }

    public static void loadBlocks() {
        //0
        BLOCKS.put("none", null);
        //1
        BLOCKS.put("stone", new Block(0));
        //2
        BLOCKS.put("grass", new Block(1));
        //3
        BLOCKS.put("dirt", new Block(2));
        //4
        BLOCKS.put("cobblestone", new Block(3));
        //5
        BLOCKS.put("planks", new Block(4));
        //6
        BLOCKS.put("sapling", new Block(5,false,false,true));
        //7
        BLOCKS.put("bedrock", new Block(6));
        //8
        BLOCKS.put("water", new Block(7,false,false,true));
        //9
        BLOCKS.put("lava", new Block(8,false,false,false));
        //10
        BLOCKS.put("sand", new Block(9));
        //11
        BLOCKS.put("gravel", new Block(10));
        //12
        BLOCKS.put("gold_ore", new Block(11));
        //13
        BLOCKS.put("iron_ore", new Block(12));
        //14
        BLOCKS.put("coal_ore", new Block(13));
        //15
        BLOCKS.put("log", new Block(14));
        //16
        BLOCKS.put("leaves", new Block(15));
        //17
        BLOCKS.put("sponge", new Block(16));
        //18
        BLOCKS.put("glass", new Block(17,true,false,true));
        //19
        BLOCKS.put("lapis_ore", new Block(18));
        //20
        BLOCKS.put("lapis_block", new Block(19));
        //21
        BLOCKS.put("sandstone", new Block(20));
        //22
        BLOCKS.put("noteblock", new Block(21));
        //23
        BLOCKS.put("bed_l", new Block(22,false,true,true));
        //24
        BLOCKS.put("bed_r", new Block(23, false,true, true));
        //25
        BLOCKS.put("cobweb", new Block(24,false,false,true));
        //26
        BLOCKS.put("tallgrass", new Block(25,false,false,true));
        //27
        BLOCKS.put("deadbush", new Block(26,false,false,true));
        //28
        BLOCKS.put("brick_block", new Block(27));
        //29
        BLOCKS.put("dandelion", new Block(28,false,false,true));
        //30
        BLOCKS.put("rose", new Block(29,false,false,true));
        //31
        BLOCKS.put("brown_mushroom", new Block(30,false,false,true));
        //32
        BLOCKS.put("red_mushroom", new Block(31,false,false,true));
        //33
        BLOCKS.put("wool_while", new Block(32,true,false,false));
        //34
        BLOCKS.put("wool_orange", new Block(33,true,false,false));
        //35
        BLOCKS.put("wool_magenta", new Block(34,true,false,false));
        //36
        BLOCKS.put("wool_lightblue", new Block(35,true,false,false));
        //37
        BLOCKS.put("wool_yellow", new Block(36,true,false,false));
        //38
        BLOCKS.put("wool_lime", new Block(37,true,false,false));
        //39
        BLOCKS.put("wool_pink", new Block(38,true,false,false));
        //40
        BLOCKS.put("wool_gray", new Block(39,true,false,false));
        //41
        BLOCKS.put("wool_lightgray", new Block(40,true,false,false));
        //42
        BLOCKS.put("wool_cyan", new Block(41,true,false,false));
        //43
        BLOCKS.put("wool_purple", new Block(42,true,false,false));
        //44
        BLOCKS.put("wool_blue", new Block(43,true,false,false));
        //45
        BLOCKS.put("wool_brown", new Block(44,true,false,false));
        //46
        BLOCKS.put("wool_green", new Block(45,true,false,false));
        //47
        BLOCKS.put("wool_red", new Block(46,true,false,false));
        //48
        BLOCKS.put("wool_black", new Block(47,true,false,false));
        //49
        BLOCKS.put("gold_block", new Block(48));
        //50
        BLOCKS.put("iron_block", new Block(49));
        //51
        BLOCKS.put("stone_slab", new Block(0, 8, 16,8, 50, true, false, true));
        //52
        BLOCKS.put("double_stone_slab", new Block(51));
        //53
        BLOCKS.put("sandstone_slab", new Block(0, 8, 16,8, 52, true, false, true));
        //54
        BLOCKS.put("wooden_slab", new Block(0, 8, 16,8, 53, true, false, true));
        //55
        BLOCKS.put("cobblestone_slab", new Block(0, 8, 16,8, 54, true, false, true));
        //56
        BLOCKS.put("brick_slab", new Block(0, 8, 16,8, 55, true, false, true));
        //57
        BLOCKS.put("stonebrick", new Block(64));
        //58
        BLOCKS.put("stone_brick_slab", new Block(0, 8, 16,8, 56, true, false, true));
        //59
        BLOCKS.put("cactus", new Block(1, 0, 14, 16, 57, true, false, true));
        //60
        BLOCKS.put("water_12", new Block(58,false,false,true));
        //61
        BLOCKS.put("water_8", new Block(59,false,false,true));
        //62
        BLOCKS.put("water_4", new Block(60,false,false,true));
        //63
        BLOCKS.put("lava_12", new Block(61,false,false,true));
        //64
        BLOCKS.put("lava_8", new Block(62,false,false,true));
        //65
        BLOCKS.put("lava_4", new Block(63,false,false,true));
        //66
        BLOCKS.put("obsidian", new Block(65));
    }

    public static void load() {
        loadBlocks();
    }

}
