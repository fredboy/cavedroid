package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Timer;
import ru.deadsoftware.cavedroid.MainConfig;
import ru.deadsoftware.cavedroid.game.mobs.MobsController;
import ru.deadsoftware.cavedroid.game.mobs.player.Player;
import ru.deadsoftware.cavedroid.game.objects.furnace.FurnaceController;
import ru.deadsoftware.cavedroid.game.world.GameWorldBlocksLogicControllerTask;
import ru.deadsoftware.cavedroid.game.world.GameWorldFluidsLogicControllerTask;
import ru.deadsoftware.cavedroid.game.world.GameWorldMobDamageControllerTask;

import javax.inject.Inject;

@GameScope
public class GameProc implements Disposable {

    private final GamePhysics mGamePhysics;
    private final GameRenderer mGameRenderer;
    private final MobsController mMobsController;
    private final FurnaceController mFurnaceController;
    private final GameItemsHolder mGameItemsHolder;
    private final GameWorldFluidsLogicControllerTask mGameWorldFluidsLogicControllerTask;
    private final GameWorldBlocksLogicControllerTask mGameWorldBlocksLogicControllerTask;
    private final GameWorldMobDamageControllerTask mGameWorldMobDamageControllerTask;

    private final Timer mWorldLogicTimer = new Timer();

    @Inject
    public GameProc(MainConfig mainConfig,
                    GamePhysics gamePhysics,
                    GameRenderer gameRenderer,
                    MobsController mobsController,
                    FurnaceController furnaceController,
                    GameItemsHolder gameItemsHolder,
                    GameWorldFluidsLogicControllerTask gameWorldFluidsLogicControllerTask,
                    GameWorldBlocksLogicControllerTask gameWorldBlocksLogicControllerTask,
                    GameWorldMobDamageControllerTask gameWorldMobDamageControllerTask
    ) {
        mGamePhysics = gamePhysics;
        mGameRenderer = gameRenderer;
        mMobsController = mobsController;
        mFurnaceController = furnaceController;
        mGameItemsHolder = gameItemsHolder;
        mGameWorldFluidsLogicControllerTask = gameWorldFluidsLogicControllerTask;
        mGameWorldBlocksLogicControllerTask = gameWorldBlocksLogicControllerTask;
        mGameWorldMobDamageControllerTask = gameWorldMobDamageControllerTask;

        mobsController.getPlayer().controlMode = mainConfig.isTouch() ? Player.ControlMode.WALK : Player.ControlMode.CURSOR;

        mWorldLogicTimer.scheduleTask(gameWorldFluidsLogicControllerTask, 0,
                GameWorldFluidsLogicControllerTask.FLUID_UPDATE_INTERVAL_SEC);
        mWorldLogicTimer.scheduleTask(gameWorldBlocksLogicControllerTask, 0,
                GameWorldBlocksLogicControllerTask.WORLD_BLOCKS_LOGIC_UPDATE_INTERVAL_SEC);
        mWorldLogicTimer.scheduleTask(gameWorldMobDamageControllerTask, 0,
                GameWorldMobDamageControllerTask.ENVIRONMENTAL_MOB_DAMAGE_INTERVAL_SEC);
    }

    public void setPlayerGameMode(int gameMode) {
        mMobsController.getPlayer().gameMode = gameMode;
    }

    public void update(float delta) {
        mGamePhysics.update(delta);
        mGameRenderer.render(delta);
        mFurnaceController.update(mGameItemsHolder);
    }

    public void show() {
        Gdx.input.setInputProcessor(mGameRenderer);
    }

    @Override
    public void dispose() {
        mWorldLogicTimer.stop();
        mGameWorldFluidsLogicControllerTask.cancel();
        mGameWorldBlocksLogicControllerTask.cancel();
        mGameWorldMobDamageControllerTask.cancel();
    }
}
