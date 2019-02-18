package ru.deadsoftware.cavedroid.game.objects;

import com.badlogic.gdx.math.Rectangle;

public class Block {

    private int x, y, w, h;
    private int tex;
    private int hp, drop;

    //coll - collision, bg - background, tp - transparent, rb - requires block under it
    private boolean coll, bg, tp, rb;

    public Block(int tex, int hp, int drop) {
        this(0, 0, 16, 16, tex, hp, drop, true, false, false, false);
    }

    public Block(int tex, int hp, int drop, boolean coll, boolean bg, boolean tp) {
        this(0, 0, 16, 16, tex, hp, drop, coll, bg, tp, false);
    }

    public Block(int tex, int hp, int drop, boolean coll, boolean bg, boolean tp, boolean rb) {
        this(0, 0, 16, 16, tex, hp, drop, coll, bg, tp, rb);
    }

    public Block(int x, int y, int w, int h, int tex, int hp, int drop, boolean coll, boolean bg, boolean tp) {
        this(x, y, w, h, tex, hp, drop, coll, bg, tp, false);
    }

    public Block(int x, int y, int w, int h, int tex, int hp, int drop, boolean coll, boolean bg, boolean tp, boolean rb) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.tex = tex;
        this.hp = hp;
        this.drop = drop;
        this.coll = coll;
        this.bg = bg;
        this.tp = tp;
        this.rb = rb;
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

    public int getTex() {
        return tex;
    }

    public int getHp() {
        return hp;
    }

    public int getDrop() {
        return drop;
    }

    public Rectangle getRect(int x, int y) {
        x *= 16;
        y *= 16;
        return new Rectangle(x + this.x, y + this.y, w, h);
    }

    public boolean toJump() {
        return (y < 8 && coll);
    }

}
