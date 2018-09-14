package ru.deadsoftware.cavecraft.game.objects;

public class Item {

    private int texture;
    private int type; // 0 - block, 1 - tool, 2 - other
    private int block;
    private String name;

    public Item(String name, int texture, int type) {
        this(name, texture, type, -1);
    }

    public Item(String name, int texture, int type, int block) {
        this.name = name;
        this.texture = texture;
        this.type = type;
        this.block = block;
    }

    public int getTexture() {
        return texture;
    }
    public int getType() {
        return type;
    }
    public int getBlock() {
        return block;
    }
    public String getName() {
        return name;
    }

}
