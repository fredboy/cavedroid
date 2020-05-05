package ru.deadsoftware.cavedroid.game.objects;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;

import javax.annotation.CheckForNull;

public class Block {

    private final int x;
    private final int y;
    private final int w;
    private final int h;
    private final int hp;
    private final String drop;
    private final String meta;
    @CheckForNull
    private final Sprite tex;

    private final boolean coll;
    private final boolean bg;
    private final boolean tp;
    private final boolean rb;
    private final boolean fluid;

    /**
     * @param left   margin from left edge
     * @param top    margin from top edge
     * @param right  margin from right edge
     * @param bottom margin from bottom edge
     * @param hp     hit points
     * @param drop   id of an item the block will drop when destroyed
     * @param coll   true if block has collision
     * @param bg     true if block should be drawn behind player
     * @param tp     true if block is transparent and renderer should draw a block behind it
     * @param rb     true if block should break when there is no block with collision under it
     * @param fluid  true if fluid
     * @param meta   extra info for storing
     * @param tex    block's texture
     */
    public Block(int left, int top, int right, int bottom, int hp, String drop, boolean coll, boolean bg, boolean tp,
                 boolean rb, boolean fluid, String meta, @CheckForNull Sprite tex) {
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
        if (this.tex != null) {
            this.tex.flip(false, true);
        }
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

    public Sprite getTexture() {
        assert tex != null;
        return tex;
    }

    public Rectangle getRectangle(int x, int y) {
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