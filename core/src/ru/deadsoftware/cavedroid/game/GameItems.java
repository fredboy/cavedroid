package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonValue;
import ru.deadsoftware.cavedroid.game.objects.Block;
import ru.deadsoftware.cavedroid.game.objects.Item;
import ru.deadsoftware.cavedroid.misc.Assets;

import java.util.HashMap;

public class GameItems {

    private static final String TAG = "GameItems";

    private static final HashMap<String, Integer> blocksIds = new HashMap<>();
    private static final HashMap<String, Integer> itemsIds = new HashMap<>();

    private static final ArrayMap<String, Block> blocks = new ArrayMap<>();
    private static final ArrayMap<String, Item> items = new ArrayMap<>();

    public static boolean isFluid(int id) {
        return getBlock(id).isFluid();
    }

    public static boolean isWater(int id) {
        return isWater(getBlock(id));
    }

    public static boolean isWater(Block block) {
        return block.getMeta().equals("water");
    }

    public static boolean isLava(int id) {
        return isLava(getBlock(id));
    }

    public static boolean isLava(Block block) {
        return block.getMeta().equals("lava");
    }

    public static boolean isSlab(int id) {
        return getBlock(id).getMeta().equals("slab");
    }

    public static boolean fluidCanFlowThere(int thisId, int thatId) {
        return thatId == 0 || (!getBlock(thatId).hasCollision() && !isFluid(thatId)) ||
                (isWater(thisId) && isWater(thatId) && thisId < thatId) ||
                (isLava(thisId) && isLava(thatId) && thisId < thatId);
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
        return getBlock(id).getTexture();
    }

    public static Sprite getItemTex(int id) {
        return items.getValueAt(id).getType().equals("block") ? getBlockTex(id) : getItem(id).getTexture();
    }

    public static void load() {
        JsonValue json = Assets.jsonReader.parse(Gdx.files.internal("json/game_items.json"));
        for (JsonValue block = json.get("blocks").child(); block != null; block = block.next()) {
            try {
                String key = block.name();
                int left = Assets.getIntFromJson(block, "left", 0);
                int right = Assets.getIntFromJson(block, "right", 0);
                int top = Assets.getIntFromJson(block, "top", 0);
                int bottom = Assets.getIntFromJson(block, "bottom", 0);
                int clipX = Assets.getIntFromJson(block, "sprite_left", 0);
                int clipY = Assets.getIntFromJson(block, "sprite_top", 0);
                int clipWidth = Assets.getIntFromJson(block, "sprite_right", 0);
                int clipHeight = Assets.getIntFromJson(block, "sprite_bottom", 0);
                int hp = Assets.getIntFromJson(block, "hp", -1);
                boolean collision = Assets.getBooleanFromJson(block, "collision", true);
                boolean background = Assets.getBooleanFromJson(block, "background", false);
                boolean transparent = Assets.getBooleanFromJson(block, "transparent", false);
                boolean requiresBlock = Assets.getBooleanFromJson(block, "block_required", false);
                boolean fluid = Assets.getBooleanFromJson(block, "fluid", false);
                String drop = Assets.getStringFromJson(block, "drop", key);
                String meta = Assets.getStringFromJson(block, "meta", "");
                String tex = Assets.getStringFromJson(block, "texture", key);
                Texture texture = tex.equals("none") ? null :
                        new Texture(Gdx.files.internal("textures/blocks/" + tex + ".png"));
                boolean animated = Assets.getBooleanFromJson(block, "animated", false);
                int frames = Assets.getIntFromJson(block, "frames", 0);

                Block newBlock = new Block(
                        left,
                        top,
                        right,
                        bottom,
                        hp,
                        drop,
                        collision,
                        background,
                        transparent,
                        requiresBlock,
                        fluid,
                        meta,
                        texture,
                        animated,
                        frames,
                        clipX,
                        clipY,
                        clipWidth,
                        clipHeight
                );

                blocksIds.put(key, blocks.size);
                blocks.put(key, newBlock);
            } catch (GdxRuntimeException e) {
                Gdx.app.error(TAG, e.getMessage());
            }
        }
        for (JsonValue item = json.get("items").child(); item != null; item = item.next()) {
            try {
                String key = item.name();
                String name = Assets.getStringFromJson(item, "name", key);
                String type = Assets.getStringFromJson(item, "type", "item");
                String texture = Assets.getStringFromJson(item, "texture", key);
                Sprite sprite = type.equals("block") ? null :
                        new Sprite(new Texture(Gdx.files.internal("textures/items/" + texture + ".png")));
                itemsIds.put(key, items.size);
                items.put(key, new Item(name, type, sprite));
            } catch (GdxRuntimeException e) {
                Gdx.app.error(TAG, e.getMessage());
            }
        }
    }

}