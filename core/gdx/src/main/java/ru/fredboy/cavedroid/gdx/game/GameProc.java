package ru.fredboy.cavedroid.gdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Timer;
import ru.fredboy.cavedroid.common.di.GameScope;
import ru.fredboy.cavedroid.common.model.GameMode;
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository;
import ru.fredboy.cavedroid.game.controller.container.ContainerController;
import ru.fredboy.cavedroid.game.controller.drop.DropController;
import ru.fredboy.cavedroid.game.controller.mob.MobController;
import ru.fredboy.cavedroid.game.controller.projectile.ProjectileController;
import ru.fredboy.cavedroid.game.world.GameWorld;
import ru.fredboy.cavedroid.gameplay.controls.GameInputProcessor;
import ru.fredboy.cavedroid.gameplay.physics.task.GameWorldBlocksLogicControllerTask;
import ru.fredboy.cavedroid.gameplay.physics.task.GameWorldFluidsLogicControllerTask;
import ru.fredboy.cavedroid.gameplay.physics.task.GameWorldMobDamageControllerTask;
import ru.fredboy.cavedroid.gameplay.physics.task.GameWorldMobSpawnControllerTask;
import ru.fredboy.cavedroid.gameplay.rendering.GameRenderer;

import javax.inject.Inject;

@GameScope
public class GameProc implements Disposable {

    private final GameRenderer mGameRenderer;
    private final MobController mMobsController;
    private final DropController mDropController;
    private final ContainerController mContainerController;
    private final GameWorldFluidsLogicControllerTask mGameWorldFluidsLogicControllerTask;
    private final GameWorldBlocksLogicControllerTask mGameWorldBlocksLogicControllerTask;
    private final GameWorldMobDamageControllerTask mGameWorldMobDamageControllerTask;
    private final GameWorldMobSpawnControllerTask mGameWorldMobSpawnControllerTask;
    private final GameInputProcessor mGameInputProcessor;
    private final GameWorld mGameWorld;
    private final ProjectileController mProjectileController;

    private final Timer mWorldLogicTimer = new Timer();

    @Inject
    public GameProc(GameContextRepository gameContextRepository,
                    GameRenderer gameRenderer,
                    MobController mobsController,
                    DropController dropController,
                    ContainerController containerController,
                    GameWorldFluidsLogicControllerTask gameWorldFluidsLogicControllerTask,
                    GameWorldBlocksLogicControllerTask gameWorldBlocksLogicControllerTask,
                    GameWorldMobDamageControllerTask gameWorldMobDamageControllerTask,
                    GameWorldMobSpawnControllerTask gameWorldMobSpawnControllerTask,
                    GameInputProcessor gameInputProcessor,
                    GameWorld gameWorld,
                    ProjectileController projectileController
    ) {
        mGameRenderer = gameRenderer;
        mMobsController = mobsController;
        mDropController = dropController;
        mContainerController = containerController;
        mGameWorldFluidsLogicControllerTask = gameWorldFluidsLogicControllerTask;
        mGameWorldBlocksLogicControllerTask = gameWorldBlocksLogicControllerTask;
        mGameWorldMobDamageControllerTask = gameWorldMobDamageControllerTask;
        mGameWorldMobSpawnControllerTask = gameWorldMobSpawnControllerTask;
        mGameInputProcessor = gameInputProcessor;
        mGameWorld = gameWorld;
        mProjectileController = projectileController;

        mWorldLogicTimer.scheduleTask(gameWorldFluidsLogicControllerTask, 0,
                GameWorldFluidsLogicControllerTask.FLUID_UPDATE_INTERVAL_SEC);
        mWorldLogicTimer.scheduleTask(gameWorldBlocksLogicControllerTask, 0,
                GameWorldBlocksLogicControllerTask.WORLD_BLOCKS_LOGIC_UPDATE_INTERVAL_SEC);
        mWorldLogicTimer.scheduleTask(gameWorldMobDamageControllerTask, 0,
                GameWorldMobDamageControllerTask.ENVIRONMENTAL_MOB_DAMAGE_INTERVAL_SEC);
        mWorldLogicTimer.scheduleTask(gameWorldMobSpawnControllerTask,
                GameWorldMobSpawnControllerTask.SPAWN_INTERVAL_SEC,
                GameWorldMobSpawnControllerTask.SPAWN_INTERVAL_SEC);

        if (!gameContextRepository.isLoadGame() ||
                mGameWorld.getTotalGameTimeSec() - mGameWorld.getLastSpawnGameTime()
                        > GameWorldMobSpawnControllerTask.SPAWN_INTERVAL_SEC) {
            gameWorldMobSpawnControllerTask.exec();
        }

        mGameRenderer.resetCameraToPlayer();
    }

    public void setPlayerGameMode(GameMode gameMode) {
        mMobsController.getPlayer().setGameMode(gameMode);
    }

    public void update(float delta) {
        mGameWorld.update(delta);
        mMobsController.update(delta);
        mDropController.update(delta);
        mProjectileController.update(delta);
        mGameInputProcessor.update(delta);
        mGameRenderer.render(delta);
        mContainerController.update(delta);
    }

    public void onResize() {
        mGameRenderer.onResize();
    }

    public void onGamePaused() {
        mWorldLogicTimer.stop();
    }

    public void onGameResumed() {
        mWorldLogicTimer.start();
    }

    public void show() {
        Gdx.input.setInputProcessor(mGameInputProcessor);
    }

    @Override
    public void dispose() {
        mGameWorldFluidsLogicControllerTask.shutdownBlocking();
        mGameWorldBlocksLogicControllerTask.shutdownBlocking();
        mGameWorldMobDamageControllerTask.shutdownBlocking();
        mGameWorldMobSpawnControllerTask.shutdownBlocking();
        mWorldLogicTimer.stop();

        mGameRenderer.dispose();
        mProjectileController.dispose();
        mDropController.dispose();
        mMobsController.dispose();
        mContainerController.dispose();
        mGameWorld.dispose();
    }
}
