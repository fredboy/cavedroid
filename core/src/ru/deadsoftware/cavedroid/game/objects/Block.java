package ru.deadsoftware.cavedroid.game.objects;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;

public class Block {

    private int x, y, w, h;
    private int hp;
    private String drop, meta;
    private Sprite tex;

    //coll - collision, bg - background, tp - transparent, rb - requires block under it
    private boolean coll, bg, tp, rb, fluid;

    public Block(int left, int top, int right, int bottom, int hp,
                 String drop, boolean coll, boolean bg, boolean tp, boolean rb, boolean fluid, String meta, Sprite tex) {
        this.x = left;
        this.y = top;
        this.w = 16 - right - left;
        this.h = 16 - top - bottom;
        this.hp = hp;
        this.drop = drop;
        this.coll = coll;
        this.bg = bg;
        this.tp = tp;
        this.rb = rb;
        this.fluid = fluid;
        this.meta = meta;
        this.tex = tex;
        if (this.tex != null) this.tex.flip(false, true);
    }

    public boolean hasCollision() {
        return coll;
    }

    public boolean isBackground() {
        return bg;
    }

    public boolean isTransparent() {
        return tp;
    }

    public boolean requiresBlock() {
        return rb;
    }

    public int getHp() {
        return hp;
    }

    public String getDrop() {
        return drop;
    }

    public boolean hasDrop() {
        return !drop.equals("none");
    }

    public Sprite getTex() {
        return tex;
    }

    public Rectangle getRect(int x, int y) {
        x *= 16;
        y *= 16;
        return new Rectangle(x + this.x, y + this.y, w, h);
    }

    public boolean isFluid() {
        return fluid;
    }

    public String getMeta() {
        return meta;
    }

    public boolean toJump() {
        return (y < 8 && coll);
    }

}