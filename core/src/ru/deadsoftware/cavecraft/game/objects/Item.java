package ru.deadsoftware.cavecraft.game.objects;

public class Item {

    private int tex;
    private int type; // 0 - block, 1 - tool, 2 - other
    private int block;
    private String name;

    public Item(String name, int tex, int type) {
        this(name, tex, type, -1);
    }

    public Item(String name, int tex, int type, int block) {
        this.name = name;
        this.tex = tex;
        this.type = type;
        this.block = block;
    }

    public int getTex() {
        return tex;
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
