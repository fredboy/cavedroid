package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import ru.deadsoftware.cavedroid.game.objects.Block;
import ru.deadsoftware.cavedroid.game.objects.Item;

import java.util.HashMap;

public class GameItems {

    private static HashMap<String, Integer> blocksIds = new HashMap<>();
    private static HashMap<String, Integer> itemsIds = new HashMap<>();

    private static ArrayMap<String, Block> blocks = new ArrayMap<>();
    private static ArrayMap<String, Item> items = new ArrayMap<>();

    public static boolean isFluid(int id) {
        return getBlock(id).isFluid();
    }

    public static boolean isWater(int id) {
        return getBlock(id).getMeta().equals("water");
    }

    public static boolean isLava(int id) {
        return getBlock(id).getMeta().equals("lava");
    }

    public static boolean isSlab(int id) {
        return getBlock(id).getMeta().equals("slab");
    }

    public static boolean fluidCanFlowThere(int thisId, int thatId) {
        return thatId == 0 || (!getBlock(thatId).hasCollision() && !isFluid(thatId)) ||
                (isWater(thisId) && isWater(thatId) && thatId >= thisId) ||
                (isLava(thisId) && isLava(thatId) && thatId >= thisId);
    }

    public static Block getBlock(int id) {
        return blocks.getValueAt(id);
    }

    public static Item getItem(int id) {
        return items.getValueAt(id);
    }

    public static Block getBlock(String key) {
        return blocks.getValueAt(blocksIds.get(key));
    }

    public static Item getItem(String key) {
        return items.getValueAt(itemsIds.get(key));
    }

    public static int getBlockId(String key) {
        return blocksIds.get(key);
    }

    public static int getItemId(String key) {
        return itemsIds.get(key);
    }

    public static String getBlockKey(int id) {
        return blocks.getKeyAt(id);
    }

    public static String getItemKey(int id) {
        return items.getKeyAt(id);
    }

    public static int getBlockIdByItemId(int id) {
        return getBlockId(items.getKeyAt(id));
    }

    public static int getBlocksSize() {
        return blocks.size;
    }

    public static int getItemsSize() {
        return items.size;
    }

    public static Sprite getBlockTex(int id) {
        return getBlock(id).getTex();
    }

    public static Sprite getItemTex(int id) {
        if (items.getValueAt(id).getType().equals("block")) return getBlockTex(id);
        else return getItem(id).getTex();
    }

    public static void load() {
        JsonValue json = new JsonReader().parse(Gdx.files.internal("game_items.json"));
        JsonValue block = json.child.child;
        JsonValue item = json.child.next.child;
        while (block != null) {
            String key = block.name;
            int left = (block.has("left") ? block.getInt("left") : 0);
            int right = (block.has("right") ? block.getInt("right") : 0);
            int top = (block.has("top") ? block.getInt("top") : 0);
            int bottom = (block.has("bottom") ? block.getInt("bottom") : 0);
            int hp = (block.has("hp") ? block.getInt("hp") : -1);
            String drop = (block.has("drop") ? block.getString("drop") : key);
            boolean collision = (!block.has("collision") || block.getBoolean("collision"));
            boolean background = (block.has("background") && block.getBoolean("background"));
            boolean transparent = !(!block.has("collision") || block.getBoolean("collision"));
            boolean blockRequired = (block.has("block_required") && block.getBoolean("block_required"));
            boolean fluid = (block.has("fluid") && block.getBoolean("fluid"));
            String meta = (block.has("meta") ? block.getString("meta") : "");
            String texture = (block.has("texture") ? block.getString("texture") : key);
            blocksIds.put(key, blocks.size);
            blocks.put(key, new Block(left, top, right, bottom, hp, drop, collision,
                    background, transparent, blockRequired, fluid, meta,
                    key.equals("none") ? null :
                            new Sprite(new Texture(Gdx.files.internal("textures/" + texture + ".png")))));
            block = block.next();
        }
        while (item != null) {
            String key = item.name;
            String name = (item.has("name") ? item.getString("name") : key);
            String type = (item.has("type") ? item.getString("type") : "item");
            String texture = (item.has("texture") ? item.getString("texture") : key);
            itemsIds.put(key, items.size);
            items.put(key, new Item(name, type, type.equals("block") ? null :
                    new Sprite(new Texture(Gdx.files.internal("textures/" + texture + ".png")))));
            item = item.next();
        }
    }

}