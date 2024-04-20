package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonValue;
import ru.deadsoftware.cavedroid.game.model.block.*;
import ru.deadsoftware.cavedroid.game.model.item.CommonItemParams;
import ru.deadsoftware.cavedroid.game.model.item.Item;
import ru.deadsoftware.cavedroid.misc.Assets;
import ru.deadsoftware.cavedroid.misc.utils.AssetLoader;
import ru.deadsoftware.cavedroid.misc.utils.SpriteOrigin;

import java.io.FileInputStream;
import java.util.*;

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

    @Deprecated
    public static boolean isWater(Block block) {
        return block instanceof Block.Water;
    }

    @Deprecated
    public static boolean isLava(int id) {
        return isLava(getBlock(id));
    }

    @Deprecated
    public static boolean isLava(Block block) {
        return block instanceof Block.Lava;
    }

    @Deprecated
    public static boolean isSlab(int id) {
        return getBlock(id) instanceof Block.Slab;
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

    public static int getItemIdByBlockId(int id) {
        return getItemId(blocks.getKeyAt(id));
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

    public static void load(AssetLoader assetLoader) {
        JsonValue json = Assets.jsonReader.parse(assetLoader.getAssetHandle("json/game_items.json"));

        TreeSet<Block> blocksSet = new TreeSet<>(Comparator.comparingInt(a -> a.getParams().getId()));
        TreeSet<Item> itemsSet = new TreeSet<>(Comparator.comparingInt(a -> a.getParams().getId()));


        int count = 0;
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
                        new Texture(assetLoader.getAssetHandle("textures/blocks/" + tex + ".png"));
                boolean animated = Assets.getBooleanFromJson(block, "animated", false);
                int frames = Assets.getIntFromJson(block, "frames", 0);
                int id = Assets.getIntFromJson(block, "id", count);
                int dropCount = Assets.getIntFromJson(block, "drop_count", 1);
                String fullBlock = Assets.getStringFromJson(block, "full_block", null);
                int state = Assets.getIntFromJson(block, "state", 0);
                blocksIds.put(key, id);

                if (count >= id) {
                    count++;
                }

                BlockMargins collMargins = new BlockMargins(left, top, right, bottom);
                BlockMargins spriteMargins = new BlockMargins(clipX, clipY, clipWidth, clipHeight);
                BlockDropInfo dropInfo = new BlockDropInfo(drop, dropCount);
                BlockAnimationInfo animInfo = null;
                if (animated) {
                    animInfo = new BlockAnimationInfo(frames);
                }

                CommonBlockParams params = new CommonBlockParams(
                        id,
                        key,
                        collMargins,
                        hp,
                        dropInfo,
                        collision,
                        background,
                        transparent,
                        requiresBlock,
                        animInfo,
                        texture,
                        spriteMargins
                );

                Block newBlock = switch (meta) {
                    case "water" -> new Block.Water(params, state);
                    case "lava" -> new Block.Lava(params, state);
                    case "slab" -> new Block.Slab(params, fullBlock);
                    default -> new Block.Normal(params);
                };

                newBlock.initialize();
                blocksSet.add(newBlock);
            } catch (GdxRuntimeException e) {
                Gdx.app.error(TAG, e.getMessage());
            }
        }

        blocksSet.forEach((block -> blocks.put(block.getParams().getKey(), block)));

        count = 0;
        for (JsonValue item = json.get("items").child(); item != null; item = item.next()) {
            try {
                String key = item.name();
                String name = Assets.getStringFromJson(item, "name", key);
                String type = Assets.getStringFromJson(item, "type", "item");
                String texture = Assets.getStringFromJson(item, "texture", key);
                Sprite sprite = type.equals("block") ? null :
                        new Sprite(new Texture(assetLoader.getAssetHandle("textures/items/" + texture + ".png")));

                if (sprite != null) {
                    sprite.flip(false, true);
                }

                float originX = Assets.getFloatFromJson(item, "origin_x", 0f);
                float originY = Assets.getFloatFromJson(item, "origin_y", 1f);
                originX = MathUtils.clamp(originX, 0f, 1f);
                originY = MathUtils.clamp(originY, 0f, 1f);
                SpriteOrigin origin = new SpriteOrigin(originX, originY);

                int id = Assets.getIntFromJson(item, "id", count);

                String actionKey = Assets.getStringFromJson(item, "action_key", null);

                float mobDamage = Assets.getFloatFromJson(item, "mob_damage_multiplier", 1f);
                float blockDamage = Assets.getFloatFromJson(item, "block_damage_multiplier", 1f);

                if (count >= id) {
                    count++;
                }

                CommonItemParams params = new CommonItemParams(id, key, name, origin);

                Item newItem = switch (type) {
                    case "bucket" -> new Item.Bucket(params, sprite, actionKey);
                    case "shovel" -> new Item.Shovel(params, sprite, mobDamage, blockDamage);
                    case "sword" -> new Item.Sword(params, sprite, mobDamage, blockDamage);
                    case "block" -> new Item.Placeable(params, blocks.get(key));
                    default -> throw new RuntimeException("Unknown item type: " + type);
                };

                itemsIds.put(key, id);
                itemsSet.add(newItem);
            } catch (GdxRuntimeException e) {
                Gdx.app.error(TAG, e.getMessage());
            }
        }

        itemsSet.forEach((item -> items.put(item.getParams().getKey(), item)));
    }

}