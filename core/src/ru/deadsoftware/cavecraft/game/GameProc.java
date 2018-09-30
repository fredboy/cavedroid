package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.TimeUtils;
import ru.deadsoftware.cavecraft.CaveGame;
import ru.deadsoftware.cavecraft.GameScreen;
import ru.deadsoftware.cavecraft.game.mobs.FallingGravel;
import ru.deadsoftware.cavecraft.game.mobs.FallingSand;
import ru.deadsoftware.cavecraft.game.mobs.Mob;
import ru.deadsoftware.cavecraft.game.mobs.Pig;
import ru.deadsoftware.cavecraft.game.objects.Drop;
import ru.deadsoftware.cavecraft.game.objects.Player;
import ru.deadsoftware.cavecraft.misc.AppState;
import ru.deadsoftware.cavecraft.misc.Assets;

import java.io.Serializable;
import java.util.ArrayList;

public class GameProc implements Serializable {

    public static double RUN_TIME = 0;

    public static boolean DO_UPD = false;
    public static int UPD_X = -1, UPD_Y = -1;

    public Player player;

    public ArrayList<Mob> mobs;
    public ArrayList<Drop> drops;

    public transient GameWorld world;
    public transient GameRenderer renderer;
    public transient GamePhysics physics;

    public int curX, curY;
    public int invSlot;
    public int ctrlMode;
    public int creativeScroll, maxCreativeScroll;
    public int blockDmg = 0;

    public boolean isTouchDown, isKeyDown, swim;
    public int touchDownX, touchDownY, keyDownCode;
    public int touchDownBtn;
    public long touchDownTime;

    public GameProc() {
        world = new GameWorld();
        world.generate(1024, 256);
        player = new Player(world.getSpawnPoint());
        drops = new ArrayList<Drop>();
        mobs = new ArrayList<Mob>();
        for (int i = 0; i < 16; i++) {
            mobs.add(new Pig(i * 256, 196 * 16));
        }
        physics = new GamePhysics(this);
        if (CaveGame.TOUCH) {
            renderer = new GameRenderer(this, 320,
                    320 * ((float) GameScreen.getHeight() / GameScreen.getWidth()));
        } else {
            ctrlMode = 1;
            renderer = new GameRenderer(this, 480,
                    480 * ((float) GameScreen.getHeight() / GameScreen.getWidth()));
        }
        maxCreativeScroll = Items.items.size() / 8;
        GameSaver.save(this);
    }

    public void resetRenderer() {
        if (CaveGame.TOUCH) {
            renderer = new GameRenderer(this, 320,
                    320 * ((float) GameScreen.getHeight() / GameScreen.getWidth()));
        } else {
            renderer = new GameRenderer(this, 480,
                    480 * ((float) GameScreen.getHeight() / GameScreen.getWidth()));
        }
    }

    private boolean isAutoselectable(int x, int y) {
        return (world.getForeMap(x, y) > 0 &&
                Items.blocks.getValueAt(world.getForeMap(x, y)).coll);
    }

    private void moveCursor() {
        int pastX = curX, pastY = curY;
        if (ctrlMode == 0 && CaveGame.TOUCH) {
            curX = (int) (player.position.x + player.texWidth / 2) / 16;
            if (player.dir == 0) curX--;
            else curX++;
            curY = (int) (player.position.y + player.texWidth) / 16;
            if (!isAutoselectable(curX, curY)) {
                curY++;
            }
            if (!isAutoselectable(curX, curY)) {
                curY++;
            }
            if (!isAutoselectable(curX, curY)) {
                if (player.dir == 0) curX++;
                else curX--;
            }
        } else if (!CaveGame.TOUCH) {
            curX = (int) (Gdx.input.getX() *
                    (renderer.getWidth() / GameScreen.getWidth()) + renderer.getCamX()) / 16;
            curY = (int) (Gdx.input.getY() *
                    (renderer.getHeight() / GameScreen.getHeight()) + renderer.getCamY()) / 16;
            if ((Gdx.input.getX() *
                    (renderer.getWidth() / GameScreen.getWidth()) + renderer.getCamX()) < 0)
                curX--;
        }
        if (pastX != curX || pastY != curY) blockDmg = 0;
    }

    private void checkCursorBounds() {
        if (curY < 0) curY = 0;
        if (curY >= world.getHeight()) curY = world.getHeight() - 1;
        if (ctrlMode == 1) {
            if (curX * 16 + 8 < player.position.x + player.texWidth / 2)
                player.dir = 0;
            if (curX * 16 + 8 > player.position.x + player.texWidth / 2)
                player.dir = 1;
        }
    }

    private void updateFluids(int x, int y) {
        if (Items.isWater(world.getForeMap(x, y)) && world.getForeMap(x, y) != 8) {
            if (world.getForeMap(x, y) == 60) {
                if (!Items.isWater(world.getForeMap(x, y - 1)))
                    world.setForeMap(x, y, world.getForeMap(x, y) + 1);
            } else if ((!Items.isWater(world.getForeMap(x - 1, y)) ||
                    (Items.isWater(world.getForeMap(x, y)) && world.getForeMap(x - 1, y) >= world.getForeMap(x, y))) &&
                    (!Items.isWater(world.getForeMap(x + 1, y)) ||
                            (Items.isWater(world.getForeMap(x, y)) && world.getForeMap(x + 1, y) >= world.getForeMap(x, y)))) {
                world.setForeMap(x, y, world.getForeMap(x, y) + 1);
            }
            if (world.getForeMap(x, y) > 63) world.setForeMap(x, y, 0);
        }

        if (world.getForeMap(x, y) == 8 || world.getForeMap(x, y) == 60) {
            if (world.getForeMap(x, y + 1) == 0 || (world.getForeMap(x, y + 1) >= 61 && world.getForeMap(x, y + 1) <= 63) ||
                    (!Items.blocks.getValueAt(world.getForeMap(x, y + 1)).coll && !Items.isFluid(world.getForeMap(x, y + 1)))) {
                world.setForeMap(x, y + 1, 60);
                updateBlock(x, y + 2);
            } else if (Items.isLava(world.getForeMap(x, y + 1))) {
                if (world.getForeMap(x, y + 1) > 9) world.setForeMap(x, y + 1, 4);
                else world.setForeMap(x, y + 1, 68);
            } else if (Items.blocks.getValueAt(world.getForeMap(x, y + 1)).coll) {
                if (world.getForeMap(x + 1, y) == 0 ||
                        (!Items.blocks.getValueAt(world.getForeMap(x + 1, y)).coll && !Items.isFluid(world.getForeMap(x + 1, y))) ||
                        (Items.isWater(world.getForeMap(x + 1, y)) && world.getForeMap(x + 1, y) > 61)) {
                    world.setForeMap(x + 1, y, 61);
                    updateBlock(x + 1, y + 1);
                } else if (Items.isLava(world.getForeMap(x + 1, y))) {
                    if (world.getForeMap(x + 1, y) > 9) world.setForeMap(x + 1, y, 4);
                    else world.setForeMap(x + 1, y, 68);
                } else if (world.getForeMap(x + 1, y) == 61 && (world.getForeMap(x + 2, y) == 8 || world.getForeMap(x + 2, y) == 60))
                    world.setForeMap(x + 1, y, 8);

                if (world.getForeMap(x - 1, y) == 0 ||
                        (!Items.blocks.getValueAt(world.getForeMap(x - 1, y)).coll && !Items.isFluid(world.getForeMap(x - 1, y))) ||
                        (Items.isWater(world.getForeMap(x - 1, y)) && world.getForeMap(x - 1, y) > 61)) {
                    world.setForeMap(x - 1, y, 61);
                    updateBlock(x - 1, y + 1);
                } else if (Items.isLava(world.getForeMap(x - 1, y))) {
                    if (world.getForeMap(x - 1, y) > 9) world.setForeMap(x - 1, y, 4);
                    else world.setForeMap(x - 1, y, 68);
                } else if (world.getForeMap(x - 1, y) == 61 && (world.getForeMap(x - 2, y) == 8 || world.getForeMap(x - 2, y) == 60))
                    world.setForeMap(x - 1, y, 8);
            }
            return;
        }
        if (world.getForeMap(x, y) == 61) {
            if (world.getForeMap(x, y + 1) == 0 || (world.getForeMap(x, y + 1) >= 61 && world.getForeMap(x, y + 1) <= 63) ||
                    (!Items.blocks.getValueAt(world.getForeMap(x, y + 1)).coll && !Items.isFluid(world.getForeMap(x, y + 1)))) {
                world.setForeMap(x, y + 1, 60);
                updateBlock(x, y + 2);
            } else if (Items.isLava(world.getForeMap(x, y + 1))) {
                if (world.getForeMap(x, y + 1) > 9) world.setForeMap(x, y + 1, 4);
                else world.setForeMap(x, y + 1, 68);
            } else if (Items.blocks.getValueAt(world.getForeMap(x, y + 1)).coll) {
                if (world.getForeMap(x + 1, y) == 0 ||
                        (!Items.blocks.getValueAt(world.getForeMap(x + 1, y)).coll && !Items.isFluid(world.getForeMap(x + 1, y))) ||
                        (Items.isWater(world.getForeMap(x + 1, y)) && world.getForeMap(x + 1, y) > 62)) {
                    world.setForeMap(x + 1, y, 62);
                    updateBlock(x + 1, y + 1);
                } else if (Items.isLava(world.getForeMap(x + 1, y))) {
                    if (world.getForeMap(x + 1, y) > 9) world.setForeMap(x + 1, y, 4);
                    else world.setForeMap(x + 1, y, 68);
                }

                if (world.getForeMap(x - 1, y) == 0 ||
                        (!Items.blocks.getValueAt(world.getForeMap(x - 1, y)).coll && !Items.isFluid(world.getForeMap(x - 1, y))) ||
                        (Items.isWater(world.getForeMap(x - 1, y)) && world.getForeMap(x - 1, y) > 62)) {
                    world.setForeMap(x - 1, y, 62);
                    updateBlock(x - 1, y + 1);
                } else if (Items.isLava(world.getForeMap(x - 1, y))) {
                    if (world.getForeMap(x - 1, y) > 9) world.setForeMap(x - 1, y, 4);
                    else world.setForeMap(x - 1, y, 68);
                }
            }
            return;
        }
        if (world.getForeMap(x, y) == 62) {
            if (world.getForeMap(x, y + 1) == 0 || (world.getForeMap(x, y + 1) >= 61 && world.getForeMap(x, y + 1) <= 63) ||
                    (!Items.blocks.getValueAt(world.getForeMap(x, y + 1)).coll && !Items.isFluid(world.getForeMap(x, y + 1)))) {
                world.setForeMap(x, y + 1, 60);
                updateBlock(x, y + 2);
            } else if (Items.isLava(world.getForeMap(x, y + 1))) {
                if (world.getForeMap(x, y + 1) > 9) world.setForeMap(x, y + 1, 4);
                else world.setForeMap(x, y + 1, 68);
            } else if (Items.blocks.getValueAt(world.getForeMap(x, y + 1)).coll) {
                if (world.getForeMap(x + 1, y) == 0 ||
                        (!Items.blocks.getValueAt(world.getForeMap(x + 1, y)).coll && !Items.isFluid(world.getForeMap(x + 1, y)))) {
                    world.setForeMap(x + 1, y, 63);
                    updateBlock(x + 1, y + 1);
                } else if (Items.isLava(world.getForeMap(x + 1, y))) {
                    if (world.getForeMap(x + 1, y) > 9) world.setForeMap(x + 1, y, 4);
                    else world.setForeMap(x + 1, y, 68);
                }

                if (world.getForeMap(x - 1, y) == 0 ||
                        (!Items.blocks.getValueAt(world.getForeMap(x - 1, y)).coll && !Items.isFluid(world.getForeMap(x - 1, y)))) {
                    world.setForeMap(x - 1, y, 63);
                    updateBlock(x - 1, y + 1);
                } else if (Items.isLava(world.getForeMap(x - 1, y))) {
                    if (world.getForeMap(x - 1, y) > 9) world.setForeMap(x - 1, y, 4);
                    else world.setForeMap(x - 1, y, 68);
                }
            }
            return;
        }
        if (world.getForeMap(x, y) == 63) {
            if (world.getForeMap(x, y + 1) == 0 || (world.getForeMap(x, y + 1) >= 61 && world.getForeMap(x, y + 1) <= 63) ||
                    (!Items.blocks.getValueAt(world.getForeMap(x, y + 1)).coll && !Items.isFluid(world.getForeMap(x, y + 1)))) {
                world.setForeMap(x, y + 1, 60);
                updateBlock(x, y + 2);
            } else if (Items.isLava(world.getForeMap(x, y + 1))) {
                if (world.getForeMap(x, y + 1) > 9) world.setForeMap(x, y + 1, 4);
                else world.setForeMap(x, y + 1, 68);
            }
            return;
        }

        if (Items.isLava(world.getForeMap(x, y)) && world.getForeMap(x, y) != 9) {
            if (world.getForeMap(x, y) == 64) {
                if (!Items.isLava(world.getForeMap(x, y - 1)))
                    world.setForeMap(x, y, world.getForeMap(x, y) + 1);
            } else if ((!Items.isLava(world.getForeMap(x, y - 1))) &&
                    (!Items.isLava(world.getForeMap(x - 1, y)) ||
                            (Items.isLava(world.getForeMap(x, y)) && world.getForeMap(x - 1, y) >= world.getForeMap(x, y))) &&
                    (!Items.isLava(world.getForeMap(x + 1, y)) ||
                            (Items.isLava(world.getForeMap(x, y)) && world.getForeMap(x + 1, y) >= world.getForeMap(x, y)))) {
                world.setForeMap(x, y, world.getForeMap(x, y) + 1);
            }
            if (world.getForeMap(x, y) > 67) world.setForeMap(x, y, 0);
        }

        if (world.getForeMap(x, y) == 9 || world.getForeMap(x, y) == 64) {
            if (world.getForeMap(x, y + 1) == 0 || (world.getForeMap(x, y + 1) >= 65 && world.getForeMap(x, y + 1) <= 67) ||
                    (!Items.blocks.getValueAt(world.getForeMap(x, y + 1)).coll && !Items.isFluid(world.getForeMap(x, y + 1)))) {
                world.setForeMap(x, y + 1, 64);
                updateBlock(x, y + 2);
            } else if (Items.isWater(world.getForeMap(x, y + 1))) {
                world.setForeMap(x, y + 1, 1);
            } else if (Items.blocks.getValueAt(world.getForeMap(x, y + 1)).coll) {
                if (world.getForeMap(x + 1, y) == 0 ||
                        (!Items.blocks.getValueAt(world.getForeMap(x + 1, y)).coll && !Items.isFluid(world.getForeMap(x + 1, y))) ||
                        (Items.isLava(world.getForeMap(x + 1, y)) && world.getForeMap(x + 1, y) > 65)) {
                    world.setForeMap(x + 1, y, 65);
                    updateBlock(x + 1, y + 1);
                } else if (Items.isWater(world.getForeMap(x + 1, y))) {
                    world.setForeMap(x + 1, y, 1);
                }

                if (world.getForeMap(x - 1, y) == 0 ||
                        (!Items.blocks.getValueAt(world.getForeMap(x - 1, y)).coll && !Items.isFluid(world.getForeMap(x - 1, y))) ||
                        (Items.isLava(world.getForeMap(x - 1, y)) && world.getForeMap(x - 1, y) > 65)) {
                    world.setForeMap(x - 1, y, 65);
                    updateBlock(x - 1, y + 1);
                } else if (Items.isWater(world.getForeMap(x - 1, y))) {
                    world.setForeMap(x - 1, y, 1);
                }
            }
            return;
        }
        if (world.getForeMap(x, y) == 65) {
            if (world.getForeMap(x, y + 1) == 0 || (world.getForeMap(x, y + 1) >= 65 && world.getForeMap(x, y + 1) <= 67) ||
                    (!Items.blocks.getValueAt(world.getForeMap(x, y + 1)).coll && !Items.isFluid(world.getForeMap(x, y + 1)))) {
                world.setForeMap(x, y + 1, 64);
                updateBlock(x, y + 2);
            } else if (Items.isWater(world.getForeMap(x, y + 1))) {
                world.setForeMap(x, y + 1, 1);
            } else if (Items.blocks.getValueAt(world.getForeMap(x, y + 1)).coll) {
                if (world.getForeMap(x + 1, y) == 0 ||
                        (!Items.blocks.getValueAt(world.getForeMap(x + 1, y)).coll && !Items.isFluid(world.getForeMap(x + 1, y))) ||
                        (Items.isLava(world.getForeMap(x + 1, y)) && world.getForeMap(x + 1, y) > 66)) {
                    world.setForeMap(x + 1, y, 66);
                    updateBlock(x + 1, y + 1);
                } else if (Items.isWater(world.getForeMap(x + 1, y))) {
                    world.setForeMap(x + 1, y, 1);
                }

                if (world.getForeMap(x - 1, y) == 0 ||
                        (!Items.blocks.getValueAt(world.getForeMap(x - 1, y)).coll && !Items.isFluid(world.getForeMap(x - 1, y))) ||
                        (Items.isLava(world.getForeMap(x - 1, y)) && world.getForeMap(x - 1, y) > 66)) {
                    world.setForeMap(x - 1, y, 66);
                    updateBlock(x - 1, y + 1);
                } else if (Items.isWater(world.getForeMap(x - 1, y))) {
                    world.setForeMap(x - 1, y, 1);
                }
            }
            return;
        }
        if (world.getForeMap(x, y) == 66) {
            if (world.getForeMap(x, y + 1) == 0 || (world.getForeMap(x, y + 1) >= 65 && world.getForeMap(x, y + 1) <= 67) ||
                    (!Items.blocks.getValueAt(world.getForeMap(x, y + 1)).coll && !Items.isFluid(world.getForeMap(x, y + 1)))) {
                world.setForeMap(x, y + 1, 64);
                updateBlock(x, y + 2);
            } else if (Items.isWater(world.getForeMap(x, y + 1))) {
                world.setForeMap(x, y + 1, 1);
            } else if (Items.blocks.getValueAt(world.getForeMap(x, y + 1)).coll) {
                if (world.getForeMap(x + 1, y) == 0 ||
                        (!Items.blocks.getValueAt(world.getForeMap(x + 1, y)).coll && !Items.isFluid(world.getForeMap(x + 1, y)))) {
                    world.setForeMap(x + 1, y, 67);
                    updateBlock(x + 1, y + 1);
                } else if (Items.isWater(world.getForeMap(x + 1, y))) {
                    world.setForeMap(x + 1, y, 1);
                }

                if (world.getForeMap(x - 1, y) == 0 ||
                        (!Items.blocks.getValueAt(world.getForeMap(x - 1, y)).coll && !Items.isFluid(world.getForeMap(x - 1, y)))) {
                    world.setForeMap(x - 1, y, 67);
                    updateBlock(x - 1, y + 1);
                } else if (Items.isWater(world.getForeMap(x - 1, y))) {
                    world.setForeMap(x - 1, y, 1);
                }
            }
            return;
        }
        if (world.getForeMap(x, y) == 67) {
            if (world.getForeMap(x, y + 1) == 0 || (world.getForeMap(x, y + 1) >= 65 && world.getForeMap(x, y + 1) <= 67) ||
                    (!Items.blocks.getValueAt(world.getForeMap(x, y + 1)).coll && !Items.isFluid(world.getForeMap(x, y + 1)))) {
                world.setForeMap(x, y + 1, 64);
                updateBlock(x, y + 2);
            } else if (Items.isWater(world.getForeMap(x, y + 1))) {
                world.setForeMap(x, y + 1, 1);
            }
            return;
        }
    }

    private void updateBlock(int x, int y) {
        if (world.getForeMap(x, y) == 10) {
            if (world.getForeMap(x, y + 1) == 0 || !Items.blocks.getValueAt(world.getForeMap(x, y + 1)).coll) {
                world.setForeMap(x, y, 0);
                mobs.add(new FallingSand(x * 16, y * 16));
                updateBlock(x, y - 1);
            }
        }

        if (world.getForeMap(x, y) == 11) {
            if (world.getForeMap(x, y + 1) == 0 || !Items.blocks.getValueAt(world.getForeMap(x, y + 1)).coll) {
                world.setForeMap(x, y, 0);
                mobs.add(new FallingGravel(x * 16, y * 16));
                updateBlock(x, y - 1);
            }
        }

        if (world.getForeMap(x, y) == 59) {
            if (world.getForeMap(x, y + 1) == 0 || !Items.blocks.getValueAt(world.getForeMap(x, y + 1)).coll) {
                world.setForeMap(x, y, 0);
                updateBlock(x, y - 1);
            }
        }

        if (world.getForeMap(x, y) == 2) {
            if (world.getForeMap(x, y - 1) > 0 && (Items.blocks.getValueAt(world.getForeMap(x, y - 1)).coll ||
                    Items.isFluid(world.getForeMap(x, y - 1)))) {
                world.setForeMap(x, y, 3);
            }
        }
    }

    public void useItem(int x, int y, int id, boolean bg) {
        if (id > 0 && Items.items.get(id).getType() == 0) {
            if (!bg) world.placeToForeground(x, y, Items.items.get(id).getBlock());
            else world.placeToBackground(x, y, Items.items.get(id).getBlock());
        }
    }

    public void update(float delta) {
        RUN_TIME += delta;

        if (DO_UPD) {
            for (int y = UPD_Y; y < UPD_Y + 16; y++)
                for (int x = UPD_X; x < UPD_X + 16; x++) {
                    updateBlock(x, y);
                }
            DO_UPD = false;
        }

        for (int y = 0; y < world.getHeight(); y++) {
            for (int x = (int) renderer.getCamX() / 16 - 1; x < (int) (renderer.getCamX() + renderer.getWidth()) / 16 + 1; x++) {
                updateFluids(x, y);
            }
        }

        physics.update(delta);
        moveCursor();
        checkCursorBounds();

        if (isTouchDown && touchDownBtn == Input.Buttons.LEFT) {
            if (world.getForeMap(curX, curY) > 0 &&
                    Items.blocks.getValueAt(world.getForeMap(curX, curY)).getHp() >= 0) {// || world.getBackMap(curX, curY) > 0) {
                blockDmg++;
                if (blockDmg >= Items.blocks.getValueAt(world.getForeMap(curX, curY)).getHp()) {
                    if (Items.blocks.getValueAt(world.getForeMap(curX, curY)).getDrop() > 0)
                        drops.add(new Drop(curX * 16 + 4, curY * 16 + 4, Items.blocks.getValueAt(world.getForeMap(curX, curY)).getDrop()));
                    world.placeToForeground(curX, curY, 0);
                    blockDmg = 0;
                }
            }
        }

        if (isTouchDown && TimeUtils.timeSinceMillis(touchDownTime) > 500) {
            if (touchDownBtn == Input.Buttons.RIGHT) {
                useItem(curX, curY, player.inventory[invSlot], true);
                isTouchDown = false;
            } else if (touchDownY < Assets.invBar.getRegionHeight() &&
                    touchDownX > renderer.getWidth() / 2 - Assets.invBar.getRegionWidth() / 2 &&
                    touchDownX < renderer.getWidth() / 2 + Assets.invBar.getRegionWidth() / 2) {
                CaveGame.STATE = AppState.GAME_CREATIVE_INV;
                isTouchDown = false;
            }
        }
    }

}
