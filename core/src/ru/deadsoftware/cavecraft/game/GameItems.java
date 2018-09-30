package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.utils.ArrayMap;
import ru.deadsoftware.cavecraft.game.objects.Block;
import ru.deadsoftware.cavecraft.game.objects.Item;

import java.util.ArrayList;

public class GameItems {

    private static ArrayMap<String, Block> blocks = new ArrayMap<String, Block>();
    private static ArrayList<Item> items = new ArrayList<Item>();

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

    public static String getBlockKey(int id) {
        return blocks.getKeyAt(id);
    }

    public static Block getBlock(int id) {
        return blocks.getValueAt(id);
    }

    public static Item getItem(int id) {
        return items.get(id);
    }

    public static int getBlocksSize() {
        return blocks.size;
    }

    public static int getItemsSize() {
        return items.size();
    }

    public static void loadItems() {
        //0
        items.add(null);
        //1
        items.add(new Item("Stone", 0, 0, 1));
        //2
        items.add(new Item("Grass", 1, 0, 2));
        //3
        items.add(new Item("Dirt", 2, 0, 3));
        //4
        items.add(new Item("Cobblestone", 3, 0, 4));
        //5
        items.add(new Item("Planks", 4, 0, 5));
        //6
        items.add(new Item("Sapling", 5, 0, 6));
        //7
        items.add(new Item("Bedrock", 6, 0, 7));
        //8
        items.add(new Item("Sand", 9, 0, 10));
        //9
        items.add(new Item("Gravel", 10, 0, 11));
        //10
        items.add(new Item("Golden Ore", 11, 0, 12));
        //11
        items.add(new Item("Iron Ore", 12, 0, 13));
        //12
        items.add(new Item("Coal Ore", 13, 0, 14));
        //13
        items.add(new Item("Wood", 14, 0, 15));
        //14
        items.add(new Item("Leaves", 15, 0, 16));
        //15
        items.add(new Item("Glass", 17, 0, 18));
        //16
        items.add(new Item("Lapis Ore", 18, 0, 19));
        //17
        items.add(new Item("Lapis Block", 19, 0, 20));
        //18
        items.add(new Item("Sandstone", 20, 0, 21));
        //19
        items.add(new Item("Cobweb", 24, 0, 25));
        //20
        items.add(new Item("Tall Grass", 25, 0, 26));
        //21
        items.add(new Item("Dead Bush", 26, 0, 27));
        //22
        items.add(new Item("Bricks", 27, 0, 28));
        //23
        items.add(new Item("Dandelion", 28, 0, 29));
        //24
        items.add(new Item("Rose", 29, 0, 30));
        //25
        items.add(new Item("Mushroom", 30, 0, 31));
        //26
        items.add(new Item("Mushroom", 31, 0, 32));
        //27
        items.add(new Item("White Wool", 32, 0, 33));
        //28
        items.add(new Item("Orange Wool", 33, 0, 34));
        //29
        items.add(new Item("Magenta Wool", 34, 0, 35));
        //30
        items.add(new Item("Light Blue Wool", 35, 0, 36));
        //31
        items.add(new Item("Yellow Wool", 36, 0, 37));
        //32
        items.add(new Item("Lime Wool", 37, 0, 38));
        //33
        items.add(new Item("Pink Wool", 38, 0, 39));
        //34
        items.add(new Item("Gray Wool", 39, 0, 40));
        //35
        items.add(new Item("Light Gray Wool", 40, 0, 41));
        //36
        items.add(new Item("Cyan Wool", 41, 0, 42));
        //37
        items.add(new Item("Purple Wool", 42, 0, 43));
        //38
        items.add(new Item("Blue Wool", 43, 0, 44));
        //39
        items.add(new Item("Brown Wool", 44, 0, 45));
        //40
        items.add(new Item("Green Wool", 45, 0, 46));
        //41
        items.add(new Item("Red Wool", 46, 0, 47));
        //42
        items.add(new Item("Black Wool", 47, 0, 48));
        //43
        items.add(new Item("Golden Block", 48, 0, 49));
        //44
        items.add(new Item("Iron Block", 49, 0, 50));
        //45
        items.add(new Item("Stone Slab", 50, 0, 51));
        //46
        items.add(new Item("Sandstone Slab", 52, 0, 53));
        //47
        items.add(new Item("Wooden Slab", 53, 0, 54));
        //48
        items.add(new Item("Cobblestone Slab", 54, 0, 55));
        //49
        items.add(new Item("Brick Slab", 55, 0, 56));
        //50
        items.add(new Item("Stone Brick", 64, 0, 57));
        //51
        items.add(new Item("Stone Brick Slab", 56, 0, 58));
        //52
        items.add(new Item("Cactus", 57, 0, 59));
        //53
        items.add(new Item("Obsidian", 65, 0, 68));
        //54
        items.add(new Item("Wooden Sword", 0, 1));
        //55
        items.add(new Item("Stone Sword", 1, 1));
        //56
        items.add(new Item("Iron Sword", 2, 1));
        //57
        items.add(new Item("Diamond Sword", 3, 1));
        //58
        items.add(new Item("Golden Sword", 4, 1));
        //59
        items.add(new Item("Wooden Shovel", 5, 1));
        //60
        items.add(new Item("Stone Shovel", 6, 1));
        //61
        items.add(new Item("Iron Shovel", 7, 1));
        //62
        items.add(new Item("Diamond Shovel", 8, 1));
        //63
        items.add(new Item("Golden Shovel", 9, 1));

    }

    public static void loadBlocks() {
        //0
        blocks.put("none", null);
        //1
        blocks.put("stone", new Block(0, 450, 4));
        //2
        blocks.put("grass", new Block(1, 54, 3));
        //3
        blocks.put("dirt", new Block(2, 45, 3));
        //4
        blocks.put("cobblestone", new Block(3, 600, 4));
        //5
        blocks.put("planks", new Block(4, 180, 5));
        //6
        blocks.put("sapling", new Block(5, 0, 6, false, false, true));
        //7
        blocks.put("bedrock", new Block(6, -1, 7));
        //8
        blocks.put("water", new Block(7, -1, 0, false, false, true));
        //9
        blocks.put("lava", new Block(8, -1, 0, false, false, false));
        //10
        blocks.put("sand", new Block(9, 45, 8));
        //11
        blocks.put("gravel", new Block(10, 54, 9));
        //12
        blocks.put("gold_ore", new Block(11, 900, 10));
        //13
        blocks.put("iron_ore", new Block(12, 900, 11));
        //14
        blocks.put("coal_ore", new Block(13, 900, 0));
        //15
        blocks.put("log", new Block(14, 180, 13));
        //16
        blocks.put("leaves", new Block(15, 21, 0));
        //17
        blocks.put("sponge", new Block(16, 54, 0));
        //18
        blocks.put("glass", new Block(17, 27, 0, true, false, true));
        //19
        blocks.put("lapis_ore", new Block(18, 900, 0));
        //20
        blocks.put("lapis_block", new Block(19, 900, 17));
        //21
        blocks.put("sandstone", new Block(20, 240, 18));
        //22
        blocks.put("noteblock", new Block(21, 75, 0));
        //23
        blocks.put("bed_l", new Block(22, 21, 0, false, true, true));
        //24
        blocks.put("bed_r", new Block(23, 21, 0, false, true, true));
        //25
        blocks.put("cobweb", new Block(24, 1200, 0, false, false, true));
        //26
        blocks.put("tallgrass", new Block(25, 0, 0, false, false, true));
        //27
        blocks.put("deadbush", new Block(26, 0, 0, false, false, true));
        //28
        blocks.put("brick_block", new Block(27, 600, 22));
        //29
        blocks.put("dandelion", new Block(28, 0, 23, false, false, true));
        //30
        blocks.put("rose", new Block(29, 0, 24, false, false, true));
        //31
        blocks.put("brown_mushroom", new Block(30, 0, 25, false, false, true));
        //32
        blocks.put("red_mushroom", new Block(31, 0, 26, false, false, true));
        //33
        blocks.put("wool_while", new Block(32, 75, 27, true, false, false));
        //34
        blocks.put("wool_orange", new Block(33, 75, 28, true, false, false));
        //35
        blocks.put("wool_magenta", new Block(34, 75, 29, true, false, false));
        //36
        blocks.put("wool_lightblue", new Block(35, 75, 30, true, false, false));
        //37
        blocks.put("wool_yellow", new Block(36, 75, 31, true, false, false));
        //38
        blocks.put("wool_lime", new Block(37, 75, 32, true, false, false));
        //39
        blocks.put("wool_pink", new Block(38, 75, 33, true, false, false));
        //40
        blocks.put("wool_gray", new Block(39, 75, 34, true, false, false));
        //41
        blocks.put("wool_lightgray", new Block(40, 75, 35, true, false, false));
        //42
        blocks.put("wool_cyan", new Block(41, 75, 36, true, false, false));
        //43
        blocks.put("wool_purple", new Block(42, 75, 37, true, false, false));
        //44
        blocks.put("wool_blue", new Block(43, 75, 38, true, false, false));
        //45
        blocks.put("wool_brown", new Block(44, 75, 39, true, false, false));
        //46
        blocks.put("wool_green", new Block(45, 75, 40, true, false, false));
        //47
        blocks.put("wool_red", new Block(46, 75, 41, true, false, false));
        //48
        blocks.put("wool_black", new Block(47, 75, 42, true, false, false));
        //49
        blocks.put("gold_block", new Block(48, 900, 43));
        //50
        blocks.put("iron_block", new Block(49, 1500, 44));
        //51
        blocks.put("stone_slab", new Block(0, 8, 16, 8, 50, 600, 45, true, false, true));
        //52
        blocks.put("double_stone_slab", new Block(51, 600, 45));
        //53
        blocks.put("sandstone_slab", new Block(0, 8, 16, 8, 52, 600, 46, true, false, true));
        //54
        blocks.put("wooden_slab", new Block(0, 8, 16, 8, 53, 180, 47, true, false, true));
        //55
        blocks.put("cobblestone_slab", new Block(0, 8, 16, 8, 54, 600, 48, true, false, true));
        //56
        blocks.put("brick_slab", new Block(0, 8, 16, 8, 55, 600, 49, true, false, true));
        //57
        blocks.put("stonebrick", new Block(64, 450, 50));
        //58
        blocks.put("stone_brick_slab", new Block(0, 8, 16, 8, 56, 450, 51, true, false, true));
        //59
        blocks.put("cactus", new Block(1, 0, 14, 16, 57, 39, 52, true, false, true));
        //60
        blocks.put("water_16", new Block(7, -1, 0, false, false, true));
        //61
        blocks.put("water_12", new Block(58, -1, 0, false, false, true));
        //62
        blocks.put("water_8", new Block(59, -1, 0, false, false, true));
        //63
        blocks.put("water_4", new Block(60, -1, 0, false, false, true));
        //64
        blocks.put("lava_16", new Block(8, -1, 0, false, false, true));
        //65
        blocks.put("lava_12", new Block(61, -1, 0, false, false, true));
        //66
        blocks.put("lava_8", new Block(62, -1, 0, false, false, true));
        //67
        blocks.put("lava_4", new Block(63, -1, 0, false, false, true));
        //68
        blocks.put("obsidian", new Block(65, 1500, 53));
    }

    public static void load() {
        loadBlocks();
        loadItems();
    }

}
