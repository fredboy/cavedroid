package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Timer;
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository;
import ru.fredboy.cavedroid.ux.controls.GameInputProcessor;
import ru.fredboy.cavedroid.ux.physics.GamePhysics;
import ru.fredboy.cavedroid.ux.physics.task.GameWorldBlocksLogicControllerTask;
import ru.fredboy.cavedroid.ux.physics.task.GameWorldFluidsLogicControllerTask;
import ru.fredboy.cavedroid.ux.physics.task.GameWorldMobDamageControllerTask;
import ru.fredboy.cavedroid.common.di.GameScope;
import ru.fredboy.cavedroid.entity.mob.model.Player;
import ru.fredboy.cavedroid.game.controller.container.ContainerController;
import ru.fredboy.cavedroid.game.controller.mob.MobController;
import ru.fredboy.cavedroid.ux.rendering.GameRenderer;

import javax.inject.Inject;

@GameScope
public class GameProc implements Disposable {

    private final GamePhysics mGamePhysics;
    private final GameRenderer mGameRenderer;
    private final MobController mMobsController;
    private final ContainerController mContainerController;
    private final GameWorldFluidsLogicControllerTask mGameWorldFluidsLogicControllerTask;
    private final GameWorldBlocksLogicControllerTask mGameWorldBlocksLogicControllerTask;
    private final GameWorldMobDamageControllerTask mGameWorldMobDamageControllerTask;
    private final GameInputProcessor mGameInputProcessor;

    private final Timer mWorldLogicTimer = new Timer();

    @Inject
    public GameProc(GameContextRepository gameContextRepository,
                    GamePhysics gamePhysics,
                    GameRenderer gameRenderer,
                    MobController mobsController,
                    ContainerController containerController,
                    GameWorldFluidsLogicControllerTask gameWorldFluidsLogicControllerTask,
                    GameWorldBlocksLogicControllerTask gameWorldBlocksLogicControllerTask,
                    GameWorldMobDamageControllerTask gameWorldMobDamageControllerTask,
                    GameInputProcessor gameInputProcessor
    ) {
        mGamePhysics = gamePhysics;
        mGameRenderer = gameRenderer;
        mMobsController = mobsController;
        mContainerController = containerController;
        mGameWorldFluidsLogicControllerTask = gameWorldFluidsLogicControllerTask;
        mGameWorldBlocksLogicControllerTask = gameWorldBlocksLogicControllerTask;
        mGameWorldMobDamageControllerTask = gameWorldMobDamageControllerTask;
        mGameInputProcessor = gameInputProcessor;

        mobsController.getPlayer().setControlMode(gameContextRepository.isTouch() ? Player.ControlMode.WALK : Player.ControlMode.CURSOR);

        mWorldLogicTimer.scheduleTask(gameWorldFluidsLogicControllerTask, 0,
                GameWorldFluidsLogicControllerTask.FLUID_UPDATE_INTERVAL_SEC);
        mWorldLogicTimer.scheduleTask(gameWorldBlocksLogicControllerTask, 0,
                GameWorldBlocksLogicControllerTask.WORLD_BLOCKS_LOGIC_UPDATE_INTERVAL_SEC);
        mWorldLogicTimer.scheduleTask(gameWorldMobDamageControllerTask, 0,
                GameWorldMobDamageControllerTask.ENVIRONMENTAL_MOB_DAMAGE_INTERVAL_SEC);
    }

    public void setPlayerGameMode(int gameMode) {
        mMobsController.getPlayer().setGameMode(gameMode);
    }

    public void update(float delta) {
        mMobsController.update(delta);
        mGamePhysics.update(delta);
        mGameInputProcessor.update(delta);
        mGameRenderer.render(delta);
        mContainerController.update(delta);
    }

    public void show() {
        Gdx.input.setInputProcessor(mGameInputProcessor);
    }

    @Override
    public void dispose() {
        mWorldLogicTimer.stop();
        mGameWorldFluidsLogicControllerTask.cancel();
        mGameWorldBlocksLogicControllerTask.cancel();
        mGameWorldMobDamageControllerTask.cancel();
    }
}
