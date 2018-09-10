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
import ru.deadsoftware.cavecraft.game.objects.Player;
import ru.deadsoftware.cavecraft.misc.AppState;
import ru.deadsoftware.cavecraft.misc.Assets;

import java.io.Serializable;
import java.util.ArrayList;

public class GameProc implements Serializable{

    public static double RUN_TIME = 0;

    public static boolean DO_UPD = false;
    public static int UPD_X = -1, UPD_Y = -1;
    public static int FUPD_X, FUPD_Y;

    public Player player;

    public ArrayList<Mob> mobs;

    public transient GameWorld world;
    public transient GameRenderer renderer;
    public transient GamePhysics physics;

    public int cursorX, cursorY;
    public int invSlot;
    public int ctrlMode;
    public int creativeScroll, maxCreativeScroll;

    public boolean isTouchDown, isKeyDown, swim;
    public int touchDownX, touchDownY, keyDownCode;
    public int touchDownButton;
    public long touchDownTime;

    public GameProc() {
        world = new GameWorld();
        world.generate(1024,256);
        player = new Player(world.getSpawnPoint());
        mobs = new ArrayList<Mob>();
        for (int i=0; i<16; i++) {
            mobs.add(new Pig(i*256, 196*16));
        }
        physics = new GamePhysics(this);
        if (CaveGame.TOUCH) {
            renderer = new GameRenderer(this,320,
                    320*((float)GameScreen.getHeight()/GameScreen.getWidth()));
        } else {
            ctrlMode = 1;
            renderer = new GameRenderer(this,480,
                    480*((float)GameScreen.getHeight()/GameScreen.getWidth()));
        }
        maxCreativeScroll = Items.BLOCKS.size/8;
        GameSaver.save(this);
    }

    public void resetRenderer() {
        if (CaveGame.TOUCH) {
            renderer = new GameRenderer(this,320,
                    320*((float)GameScreen.getHeight()/GameScreen.getWidth()));
        } else {
            renderer = new GameRenderer(this,480,
                    480*((float)GameScreen.getHeight()/GameScreen.getWidth()));
        }
    }

    private boolean isAutoselectable(int x, int y) {
        return (world.getForeMap(x,y)>0 &&
                Items.BLOCKS.getValueAt(world.getForeMap(x,y)).collision);
    }

    private void moveCursor() {
        if (ctrlMode == 0 && CaveGame.TOUCH) {
            cursorX = (int) (player.position.x + player.texWidth / 2) / 16;
            if (player.dir == 0) cursorX--;
                else cursorX++;
            cursorY = (int) (player.position.y + player.texWidth) / 16;
            if (!isAutoselectable(cursorX, cursorY)) {
                cursorY++;
            }
            if (!isAutoselectable(cursorX, cursorY)) {
                cursorY++;
            }
            if (!isAutoselectable(cursorX, cursorY)) {
                if (player.dir == 0) cursorX++;
                else cursorX--;
            }
        } else if (!CaveGame.TOUCH){
            cursorX = (int)(Gdx.input.getX()*
                    (renderer.camera.viewportWidth/GameScreen.getWidth())+renderer.camera.position.x)/16;
            cursorY = (int)(Gdx.input.getY()*
                    (renderer.camera.viewportHeight/GameScreen.getHeight())+renderer.camera.position.y)/16;
            if ((Gdx.input.getX()*
                    (renderer.camera.viewportWidth/GameScreen.getWidth())+renderer.camera.position.x)<0)
                cursorX--;
        }
    }

    private void checkCursorBounds() {
        if (cursorY < 0) cursorY = 0;
        if (cursorY >= world.getHeight()) cursorY = world.getHeight()-1;
        if (ctrlMode==1) {
            if (cursorX*16+8<player.position.x+player.texWidth/2)
                player.dir=0;
            if (cursorX*16+8>player.position.x+player.texWidth/2)
                player.dir=1;
        }
    }

    private void updateFluids(int x, int y) {
        if (Items.isWater(world.getForeMap(x, y)) && world.getForeMap(x, y)!=8) {
            if ((!Items.isWater(world.getForeMap(x-1,y)) ||
                    (Items.isWater(world.getForeMap(x,y)) && world.getForeMap(x-1, y)>=world.getForeMap(x, y))) &&
                    (!Items.isWater(world.getForeMap(x+1,y)) ||
                            (Items.isWater(world.getForeMap(x,y)) && world.getForeMap(x+1, y)>=world.getForeMap(x, y)))){
                world.setForeMap(x, y, world.getForeMap(x, y)+1);
                if (world.getForeMap(x, y)>62) world.setForeMap(x, y, 0);
            }
        }

        if (world.getForeMap(x, y) == 8) {
            if (world.getForeMap(x, y+1)==0 || (world.getForeMap(x, y+1)>=60 && world.getForeMap(x, y+1)<=62) ||
                    (!Items.BLOCKS.getValueAt(world.getForeMap(x, y+1)).collision && !Items.isFluid(world.getForeMap(x, y+1)))) {
                world.setForeMap(x,y+1,8);
                updateBlock(x, y+2);
            } else if (Items.isLava(world.getForeMap(x, y+1))) {
                if (world.getForeMap(x, y+1)>9) world.setForeMap(x, y+1, 4);
                else world.setForeMap(x, y+1, 66);
            } else if (Items.BLOCKS.getValueAt(world.getForeMap(x, y+1)).collision) {
                if (world.getForeMap(x+1, y)==0 ||
                        (!Items.BLOCKS.getValueAt(world.getForeMap(x+1, y)).collision && !Items.isFluid(world.getForeMap(x+1, y))) ||
                        (Items.isWater(world.getForeMap(x+1, y)) && world.getForeMap(x+1, y)>60)) {
                    world.setForeMap(x+1,y,60);
                    updateBlock(x+1, y+1);
                } else if (Items.isLava(world.getForeMap(x+1, y))) {
                    if (world.getForeMap(x+1, y)>9) world.setForeMap(x+1, y, 4);
                    else world.setForeMap(x+1, y, 66);
                } else if (world.getForeMap(x+1, y)==60 && world.getForeMap(x+2, y)==8) world.setForeMap(x+1, y, 8);

                if (world.getForeMap(x-1, y)==0 ||
                        (!Items.BLOCKS.getValueAt(world.getForeMap(x-1, y)).collision && !Items.isFluid(world.getForeMap(x-1, y))) ||
                        (Items.isWater(world.getForeMap(x-1, y)) && world.getForeMap(x-1, y)>60)) {
                    world.setForeMap(x-1,y,60);
                    updateBlock(x-1, y+1);
                } else if (Items.isLava(world.getForeMap(x-1, y))) {
                    if (world.getForeMap(x-1, y)>9) world.setForeMap(x-1, y, 4);
                    else world.setForeMap(x-1, y, 66);
                } else if (world.getForeMap(x-1, y)==60 && world.getForeMap(x-2, y)==8) world.setForeMap(x-1, y, 8);
            }
            return;
        }
        if (world.getForeMap(x, y) == 60) {
            if (world.getForeMap(x, y+1)==0 || (world.getForeMap(x, y+1)>=60 && world.getForeMap(x, y+1)<=62) ||
                    (!Items.BLOCKS.getValueAt(world.getForeMap(x, y+1)).collision && !Items.isFluid(world.getForeMap(x, y+1)))) {
                world.setForeMap(x,y+1,8);
                updateBlock(x, y+2);
            } else if (Items.isLava(world.getForeMap(x, y+1))) {
                if (world.getForeMap(x, y+1)>9) world.setForeMap(x, y+1, 4);
                else world.setForeMap(x, y+1, 66);
            } else if (Items.BLOCKS.getValueAt(world.getForeMap(x, y+1)).collision) {
                if (world.getForeMap(x+1, y)==0 ||
                        (!Items.BLOCKS.getValueAt(world.getForeMap(x+1, y)).collision && !Items.isFluid(world.getForeMap(x+1, y))) ||
                        (Items.isWater(world.getForeMap(x+1, y)) && world.getForeMap(x+1, y)>61)){
                    world.setForeMap(x+1,y,61);
                    updateBlock(x+1, y+1);
                } else if (Items.isLava(world.getForeMap(x+1, y))) {
                    if (world.getForeMap(x+1, y)>9) world.setForeMap(x+1, y, 4);
                    else world.setForeMap(x+1, y, 66);
                }

                if (world.getForeMap(x-1, y)==0 ||
                        (!Items.BLOCKS.getValueAt(world.getForeMap(x-1, y)).collision && !Items.isFluid(world.getForeMap(x-1, y))) ||
                        (Items.isWater(world.getForeMap(x-1, y)) && world.getForeMap(x-1, y)>61)){
                    world.setForeMap(x-1,y,61);
                    updateBlock(x-1, y+1);
                } else if (Items.isLava(world.getForeMap(x-1, y))) {
                    if (world.getForeMap(x-1, y)>9) world.setForeMap(x-1, y, 4);
                    else world.setForeMap(x-1, y, 66);
                }
            }
            return;
        }
        if (world.getForeMap(x, y) == 61) {
            if (world.getForeMap(x, y+1)==0 || (world.getForeMap(x, y+1)>=60 && world.getForeMap(x, y+1)<=62) ||
                    (!Items.BLOCKS.getValueAt(world.getForeMap(x, y+1)).collision && !Items.isFluid(world.getForeMap(x, y+1)))) {
                world.setForeMap(x,y+1,8);
                updateBlock(x, y+2);
            } else if (Items.isLava(world.getForeMap(x, y+1))) {
                if (world.getForeMap(x, y+1)>9) world.setForeMap(x, y+1, 4);
                else world.setForeMap(x, y+1, 66);
            } else if (Items.BLOCKS.getValueAt(world.getForeMap(x, y+1)).collision) {
                if (world.getForeMap(x+1, y)==0 ||
                        (!Items.BLOCKS.getValueAt(world.getForeMap(x+1, y)).collision && !Items.isFluid(world.getForeMap(x+1, y))) ){
                    world.setForeMap(x+1,y,62);
                    updateBlock(x+1, y+1);
                } else if (Items.isLava(world.getForeMap(x+1, y))) {
                    if (world.getForeMap(x+1, y)>9) world.setForeMap(x+1, y, 4);
                    else world.setForeMap(x+1, y, 66);
                }

                if (world.getForeMap(x-1, y)==0 ||
                        (!Items.BLOCKS.getValueAt(world.getForeMap(x-1, y)).collision && !Items.isFluid(world.getForeMap(x-1, y))) ){
                    world.setForeMap(x-1,y,62);
                    updateBlock(x-1, y+1);
                } else if (Items.isLava(world.getForeMap(x-1, y))) {
                    if (world.getForeMap(x-1, y)>9) world.setForeMap(x-1, y, 4);
                    else world.setForeMap(x-1, y, 66);
                }
            }
            return;
        }
        if (world.getForeMap(x, y) == 62) {
            if (world.getForeMap(x, y+1)==0 || (world.getForeMap(x, y+1)>=60 && world.getForeMap(x, y+1)<=62) ||
                    (!Items.BLOCKS.getValueAt(world.getForeMap(x, y+1)).collision && !Items.isFluid(world.getForeMap(x, y+1)))) {
                world.setForeMap(x,y+1,8);
                updateBlock(x, y+2);
            } else if (Items.isLava(world.getForeMap(x, y+1))) {
                if (world.getForeMap(x, y+1)>9) world.setForeMap(x, y+1, 4);
                else world.setForeMap(x, y+1, 66);
            }
            return;
        }

        if (Items.isLava(world.getForeMap(x, y)) && world.getForeMap(x, y)!=9) {
            if ((!Items.isLava(world.getForeMap(x-1,y)) ||
                    (Items.isLava(world.getForeMap(x,y)) && world.getForeMap(x-1, y)>=world.getForeMap(x, y))) &&
                    (!Items.isLava(world.getForeMap(x+1,y)) ||
                            (Items.isLava(world.getForeMap(x,y)) && world.getForeMap(x+1, y)>=world.getForeMap(x, y)))){
                world.setForeMap(x, y, world.getForeMap(x, y)+1);
                if (world.getForeMap(x, y)>65) world.setForeMap(x, y, 0);
            }
        }

        if (world.getForeMap(x, y) == 9) {
            if (world.getForeMap(x, y+1)==0 || (world.getForeMap(x, y+1)>=63 && world.getForeMap(x, y+1)<=65) ||
                    (!Items.BLOCKS.getValueAt(world.getForeMap(x, y+1)).collision && !Items.isFluid(world.getForeMap(x, y+1)))) {
                world.setForeMap(x,y+1,9);
                updateBlock(x, y+2);
            } else if (Items.isWater(world.getForeMap(x, y+1))) {
                world.setForeMap(x, y+1, 1);
            } else if (Items.BLOCKS.getValueAt(world.getForeMap(x, y+1)).collision) {
                if (world.getForeMap(x+1, y)==0 ||
                        (!Items.BLOCKS.getValueAt(world.getForeMap(x+1, y)).collision && !Items.isFluid(world.getForeMap(x+1, y))) ||
                        (Items.isLava(world.getForeMap(x+1, y)) && world.getForeMap(x+1, y)>63)) {
                    world.setForeMap(x+1,y,63);
                    updateBlock(x+1, y+1);
                } else if (Items.isWater(world.getForeMap(x+1, y))) {
                    world.setForeMap(x+1, y, 1);
                }

                if (world.getForeMap(x-1, y)==0 ||
                        (!Items.BLOCKS.getValueAt(world.getForeMap(x-1, y)).collision && !Items.isFluid(world.getForeMap(x-1, y))) ||
                        (Items.isLava(world.getForeMap(x-1, y)) && world.getForeMap(x-1, y)>63)) {
                    world.setForeMap(x-1,y,63);
                    updateBlock(x-1, y+1);
                } else if (Items.isWater(world.getForeMap(x-1, y))) {
                    world.setForeMap(x-1, y, 1);
                }
            }
            return;
        }
        if (world.getForeMap(x, y) == 63) {
            if (world.getForeMap(x, y+1)==0 || (world.getForeMap(x, y+1)>=63 && world.getForeMap(x, y+1)<=65) ||
                    (!Items.BLOCKS.getValueAt(world.getForeMap(x, y+1)).collision && !Items.isFluid(world.getForeMap(x, y+1)))) {
                world.setForeMap(x,y+1,9);
                updateBlock(x, y+2);
            } else if (Items.isWater(world.getForeMap(x, y+1))) {
                world.setForeMap(x, y+1, 1);
            } else if (Items.BLOCKS.getValueAt(world.getForeMap(x, y+1)).collision) {
                if (world.getForeMap(x+1, y)==0 ||
                        (!Items.BLOCKS.getValueAt(world.getForeMap(x+1, y)).collision && !Items.isFluid(world.getForeMap(x+1, y))) ||
                        (Items.isLava(world.getForeMap(x+1, y)) && world.getForeMap(x+1, y)>64)){
                    world.setForeMap(x+1,y,64);
                    updateBlock(x+1, y+1);
                } else if (Items.isWater(world.getForeMap(x+1, y))) {
                    world.setForeMap(x+1, y, 1);
                }

                if (world.getForeMap(x-1, y)==0 ||
                        (!Items.BLOCKS.getValueAt(world.getForeMap(x-1, y)).collision && !Items.isFluid(world.getForeMap(x-1, y))) ||
                        (Items.isLava(world.getForeMap(x-1, y)) && world.getForeMap(x-1, y)>64)){
                    world.setForeMap(x-1,y,64);
                    updateBlock(x-1, y+1);
                } else if (Items.isWater(world.getForeMap(x-1, y))) {
                    world.setForeMap(x-1, y, 1);
                }
            }
            return;
        }
        if (world.getForeMap(x, y) == 64) {
            if (world.getForeMap(x, y+1)==0 || (world.getForeMap(x, y+1)>=63 && world.getForeMap(x, y+1)<=65) ||
                    (!Items.BLOCKS.getValueAt(world.getForeMap(x, y+1)).collision && !Items.isFluid(world.getForeMap(x, y+1)))) {
                world.setForeMap(x,y+1,9);
                updateBlock(x, y+2);
            } else if (Items.isWater(world.getForeMap(x, y+1))) {
                world.setForeMap(x, y+1, 1);
            } else if (Items.BLOCKS.getValueAt(world.getForeMap(x, y+1)).collision) {
                if (world.getForeMap(x+1, y)==0 ||
                        (!Items.BLOCKS.getValueAt(world.getForeMap(x+1, y)).collision && !Items.isFluid(world.getForeMap(x+1, y))) ){
                    world.setForeMap(x+1,y,65);
                    updateBlock(x+1, y+1);
                } else if (Items.isWater(world.getForeMap(x+1, y))) {
                    world.setForeMap(x+1, y, 1);
                }

                if (world.getForeMap(x-1, y)==0 ||
                        (!Items.BLOCKS.getValueAt(world.getForeMap(x-1, y)).collision && !Items.isFluid(world.getForeMap(x-1, y))) ){
                    world.setForeMap(x-1,y,65);
                    updateBlock(x-1, y+1);
                } else if (Items.isWater(world.getForeMap(x-1, y))) {
                    world.setForeMap(x-1, y, 1);
                }
            }
            return;
        }
        if (world.getForeMap(x, y) == 65) {
            if (world.getForeMap(x, y+1)==0 || (world.getForeMap(x, y+1)>=63 && world.getForeMap(x, y+1)<=65) ||
                    (!Items.BLOCKS.getValueAt(world.getForeMap(x, y+1)).collision && !Items.isFluid(world.getForeMap(x, y+1)))) {
                world.setForeMap(x,y+1,9);
                updateBlock(x, y+2);
            } else if (Items.isWater(world.getForeMap(x, y+1))) {
                world.setForeMap(x, y+1, 1);
            }
            return;
        }
    }

    private void updateBlock(int x, int y) {
        if (world.getForeMap(x, y) == 10) {
            if (world.getForeMap(x, y+1)==0 || !Items.BLOCKS.getValueAt(world.getForeMap(x,y+1)).collision) {
                world.setForeMap(x, y, 0);
                mobs.add(new FallingSand(x*16, y*16));
                updateBlock(x, y-1);
            }
        }

        if (world.getForeMap(x, y) == 11) {
            if (world.getForeMap(x, y+1)==0 || !Items.BLOCKS.getValueAt(world.getForeMap(x,y+1)).collision) {
                world.setForeMap(x, y, 0);
                mobs.add(new FallingGravel(x*16, y*16));
                updateBlock(x, y-1);
            }
        }

        if (world.getForeMap(x, y) == 59) {
            if (world.getForeMap(x, y+1)==0 || !Items.BLOCKS.getValueAt(world.getForeMap(x, y+1)).collision) {
                world.setForeMap(x,y,0);
                updateBlock(x, y-1);
            }
        }

        if (world.getForeMap(x, y) == 2) {
            if (world.getForeMap(x, y-1)>0 && (Items.BLOCKS.getValueAt(world.getForeMap(x, y-1)).collision ||
                    Items.isFluid(world.getForeMap(x, y-1)))) {
                world.setForeMap(x, y, 3);
            }
        }
    }

    public void update(float delta) {
        RUN_TIME += delta;

        if (DO_UPD) {
            for (int y=UPD_Y; y<UPD_Y+16; y++)
                for (int x=UPD_X; x<UPD_X+16; x++) {
                    updateBlock(x, y);
                }
            DO_UPD = false;
        }

        for (int y=(int)renderer.camera.position.y/16-1; y<(int)(renderer.camera.position.y+renderer.camera.viewportHeight)/16+1; y++) {
            for (int x=(int)renderer.camera.position.x/16-1; x<(int)(renderer.camera.position.x+renderer.camera.viewportWidth)/16+1; x++) {
                updateFluids(x, y);
            }
        }

        updateFluids(FUPD_X, FUPD_Y);
        FUPD_X++;
        if (FUPD_X>=(int)(renderer.camera.position.x+renderer.camera.viewportWidth)/16+1) {
            FUPD_X = (int) renderer.camera.position.x / 16 - 1;
            FUPD_Y++;
            if (FUPD_Y>=(int)(renderer.camera.position.y+renderer.camera.viewportHeight)/16+1) {
                FUPD_Y = (int) renderer.camera.position.y / 16 - 1;
            }
        }

        physics.update(delta);
        moveCursor();
        checkCursorBounds();

        if (isTouchDown && TimeUtils.timeSinceMillis(touchDownTime) > 500) {
            if (touchDownButton== Input.Buttons.RIGHT) {
                world.placeToBackground(cursorX, cursorY,
                        player.inventory[invSlot]);
            } else if (touchDownY< Assets.invBar.getRegionHeight() &&
                    touchDownX>renderer.camera.viewportWidth/2-Assets.invBar.getRegionWidth()/2 &&
                    touchDownX<renderer.camera.viewportWidth/2+Assets.invBar.getRegionWidth()/2) {
                CaveGame.STATE = AppState.GAME_CREATIVE_INV;
            }
            isTouchDown = false;
        }
    }

}
