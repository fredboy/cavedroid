package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Timer;
import ru.deadsoftware.cavedroid.game.mobs.MobsController;
import ru.deadsoftware.cavedroid.game.world.GameWorldBlocksLogicControllerTask;
import ru.deadsoftware.cavedroid.game.world.GameWorldFluidsLogicControllerTask;

import javax.inject.Inject;

@GameScope
public class GameProc implements Disposable {

    private final GamePhysics mGamePhysics;
    private final GameInput mGameInput;
    private final GameRenderer mGameRenderer;
    private final MobsController mMobsController;
    private final GameWorldFluidsLogicControllerTask mGameWorldFluidsLogicControllerTask;
    private final GameWorldBlocksLogicControllerTask mGameWorldBlocksLogicControllerTask;

    private final Timer mWorldLogicTimer = new Timer();

    @Inject
    public GameProc(GamePhysics gamePhysics,
                    GameInput gameInput,
                    GameRenderer gameRenderer,
                    MobsController mobsController,
                    GameWorldFluidsLogicControllerTask gameWorldFluidsLogicControllerTask,
                    GameWorldBlocksLogicControllerTask gameWorldBlocksLogicControllerTask
    ) {
        mGamePhysics = gamePhysics;
        mGameInput = gameInput;
        mGameRenderer = gameRenderer;
        mMobsController = mobsController;
        mGameWorldFluidsLogicControllerTask = gameWorldFluidsLogicControllerTask;
        mGameWorldBlocksLogicControllerTask = gameWorldBlocksLogicControllerTask;



        mWorldLogicTimer.scheduleTask(gameWorldFluidsLogicControllerTask, 0,
                GameWorldFluidsLogicControllerTask.FLUID_UPDATE_INTERVAL_SEC);
        mWorldLogicTimer.scheduleTask(gameWorldBlocksLogicControllerTask, 0,
                GameWorldBlocksLogicControllerTask.WORLD_BLOCKS_LOGIC_UPDATE_INTERVAL_SEC);
    }

    public void setPlayerGameMode(int gameMode) {
        mMobsController.getPlayer().gameMode = gameMode;
    }

    public void update(float delta) {
        mGamePhysics.update(delta);
        mGameInput.update();
        mGameRenderer.render(delta);
    }

    @Override
    public void dispose() {
        mWorldLogicTimer.stop();
        mGameWorldFluidsLogicControllerTask.cancel();
        mGameWorldBlocksLogicControllerTask.cancel();
    }
}
