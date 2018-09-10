package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.utils.ArrayMap;
import ru.deadsoftware.cavecraft.game.objects.Block;
import ru.deadsoftware.cavecraft.game.objects.Item;

import java.util.ArrayList;

public class Items {

    public static ArrayMap<String, Block> BLOCKS = new ArrayMap<String, Block>();
    public static ArrayList<Item> ITEMS = new ArrayList<Item>();

    public static boolean isFluid(int bl) {
        return (bl == 8 || bl == 9 || bl == 60 || bl == 61 || bl == 62 || bl == 63 || bl == 64 || bl == 65 || bl == 66 || bl == 67);
    }

    public static boolean isWater(int bl) {
        return (bl == 8 || bl == 60 || bl == 61 || bl == 62 || bl == 63);
    }

    public static boolean isLava(int bl) {
        return (bl == 9 || bl == 64 || bl == 65 || bl == 66 || bl == 67);
    }

    public static boolean isSlab(int bl) {
        return (bl == 51 || bl == 53 || bl == 54 || bl == 55 || bl == 56 || bl == 58);
    }

    public static void loadItems() {
        //0
        ITEMS.add(new Item("Stone", 0, 0, 1));
        //1
        ITEMS.add(new Item("Grass", 1, 0, 2));
        //2
        ITEMS.add(new Item("Dirt", 2, 0, 3));
        //3
        ITEMS.add(new Item("Cobblestone", 3, 0, 4));
        //4
        ITEMS.add(new Item("Planks", 4, 0, 5));
        //5
        ITEMS.add(new Item("Sapling", 5, 0, 6));
        //6
        ITEMS.add(new Item("Bedrock", 6, 0, 7));
        //7
        ITEMS.add(new Item("Sand", 9, 0, 10));
        //8
        ITEMS.add(new Item("Gravel", 10, 0, 11));
        //9
        ITEMS.add(new Item("Golden Ore", 11, 0, 12));
        //10
        ITEMS.add(new Item("Iron Ore", 12, 0, 13));
        //11
        ITEMS.add(new Item("Coal Ore", 13, 0, 14));
        //12
        ITEMS.add(new Item("Wood", 14, 0, 15));
        //13
        ITEMS.add(new Item("Leaves", 15, 0, 16));
        //14
        ITEMS.add(new Item("Glass", 17, 0, 18));
        //15
        ITEMS.add(new Item("Lapis Ore", 18, 0, 19));
        //16
        ITEMS.add(new Item("Lapis Block", 19, 0, 20));
        //17
        ITEMS.add(new Item("Sandstone", 20, 0, 21));
        //18
        ITEMS.add(new Item("Cobweb", 24, 0, 25));
        //19
        ITEMS.add(new Item("Tall Grass", 25, 0, 26));
        //20
        ITEMS.add(new Item("Dead Bush", 26, 0, 27));
        //21
        ITEMS.add(new Item("Bricks", 27, 0, 28));
        //22
        ITEMS.add(new Item("Dandelion", 28, 0, 29));
        //23
        ITEMS.add(new Item("Rose", 29, 0, 30));
        //24
        ITEMS.add(new Item("Mushroom", 30, 0, 31));
        //25
        ITEMS.add(new Item("Mushroom", 31, 0, 32));
        //26
        ITEMS.add(new Item("White Wool", 32, 0, 33));
        //27
        ITEMS.add(new Item("Orange Wool", 33, 0, 34));
        //28
        ITEMS.add(new Item("Magenta Wool", 34, 0, 35));
        //29
        ITEMS.add(new Item("Light Blue Wool", 35, 0, 36));
        //30
        ITEMS.add(new Item("Yellow Wool", 36, 0, 37));
        //31
        ITEMS.add(new Item("Lime Wool", 37, 0, 38));
        //32
        ITEMS.add(new Item("Pink Wool", 38, 0, 39));
        //33
        ITEMS.add(new Item("Gray Wool", 39, 0, 40));
        //34
        ITEMS.add(new Item("Light Gray Wool", 40, 0, 41));
        //35
        ITEMS.add(new Item("Cyan Wool", 41, 0, 42));
        //36
        ITEMS.add(new Item("Purple Wool", 42, 0, 43));
        //37
        ITEMS.add(new Item("Blue Wool", 43, 0, 44));
        //38
        ITEMS.add(new Item("Brown Wool", 44, 0, 45));
        //39
        ITEMS.add(new Item("Green Wool", 45, 0, 46));
        //40
        ITEMS.add(new Item("Red Wool", 46, 0, 47));
        //41
        ITEMS.add(new Item("Black Wool", 47, 0, 48));
        //42
        ITEMS.add(new Item("Golden Block", 48, 0, 49));
        //43
        ITEMS.add(new Item("Iron Block", 49, 0, 50));
        //44
        ITEMS.add(new Item("Stone Slab", 50, 0, 51));
        //45
        ITEMS.add(new Item("Sandstone Slab", 52, 0, 53));
        //46
        ITEMS.add(new Item("Wooden Slab", 53, 0, 54));
        //47
        ITEMS.add(new Item("Cobblestone Slab", 54, 0, 55));
        //48
        ITEMS.add(new Item("Brick Slab", 55, 0, 56));
        //49
        ITEMS.add(new Item("Stone Brick", 64, 0, 57));
        //50
        ITEMS.add(new Item("Stone Brick Slab", 56, 0, 58));
        //51
        ITEMS.add(new Item("Cactus", 57, 0, 59));
        //52
        ITEMS.add(new Item("Obsidian", 65, 0, 68));

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
        BLOCKS.put("water_16", new Block(7,false,false,true));
        //61
        BLOCKS.put("water_12", new Block(58,false,false,true));
        //62
        BLOCKS.put("water_8", new Block(59,false,false,true));
        //63
        BLOCKS.put("water_4", new Block(60,false,false,true));
        //64
        BLOCKS.put("lava_16", new Block(8,false,false,true));
        //65
        BLOCKS.put("lava_12", new Block(61,false,false,true));
        //66
        BLOCKS.put("lava_8", new Block(62,false,false,true));
        //67
        BLOCKS.put("lava_4", new Block(63,false,false,true));
        //68
        BLOCKS.put("obsidian", new Block(65));
    }

    public static void load() {
        loadBlocks();
        loadItems();
    }

}
