package ru.deadsoftware.cavecraft.game.objects;

public class Item {

    private int texture, type;
    private int block;
    private String name;

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
