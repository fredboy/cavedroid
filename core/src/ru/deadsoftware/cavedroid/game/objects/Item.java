package ru.deadsoftware.cavedroid.game.objects;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class Item {

    private String name, type;
    private Sprite tex;

    public Item(String name, String type, Sprite tex) {
        this.name = name;
        this.type = type;
        this.tex = tex;
        if (this.tex != null) this.tex.flip(false, true);
    }

    public Sprite getTex() {
        return tex;
    }

    public String getType() {
        return type;
    }

    public boolean isBlock() {
        return type.equals("block");
    }

    public String getName() {
        return name;
    }

}