package ru.fredboy.cavedroid.gdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Timer;
import kotlin.random.Random;
import ru.fredboy.cavedroid.common.di.GameScope;
import ru.fredboy.cavedroid.common.model.GameMode;
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository;
import ru.fredboy.cavedroid.game.controller.container.ContainerController;
import ru.fredboy.cavedroid.game.controller.drop.DropController;
import ru.fredboy.cavedroid.game.controller.fire.FireController;
import ru.fredboy.cavedroid.game.controller.explosion.ExplosionController;
import ru.fredboy.cavedroid.game.controller.mob.MobController;
import ru.fredboy.cavedroid.game.controller.projectile.ProjectileController;
import ru.fredboy.cavedroid.game.controller.stats.StatsController;
import ru.fredboy.cavedroid.game.world.GameWorld;
import ru.fredboy.cavedroid.gameplay.controls.GameInputProcessor;
import ru.fredboy.cavedroid.gameplay.physics.task.GameWorldBlocksLogicControllerTask;
import ru.fredboy.cavedroid.gameplay.physics.task.GameWorldFireLogicControllerTask;
import ru.fredboy.cavedroid.gameplay.physics.task.GameWorldFluidsLogicControllerTask;
import ru.fredboy.cavedroid.gameplay.physics.task.GameWorldGrowBlocksControllerTask;
import ru.fredboy.cavedroid.gameplay.physics.task.GameWorldMobDamageControllerTask;
import ru.fredboy.cavedroid.gameplay.physics.task.GameWorldMobSpawnControllerTask;
import ru.fredboy.cavedroid.gameplay.rendering.GameRenderer;
import ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.onboarding.OnboardingController;

import javax.inject.Inject;
import ru.fredboy.cavedroid.common.performance.PerformanceMonitor;
import ru.fredboy.cavedroid.common.performance.PerformanceMetric;
import ru.fredboy.cavedroid.common.performance.PerformanceOperation;
import ru.fredboy.cavedroid.common.performance.PerformanceTrigger;
import ru.fredboy.cavedroid.common.performance.PerformanceSpan;

@GameScope
public class GameProc implements Disposable {

    private final PerformanceMonitor mPerformanceMonitor;
    private final GameRenderer mGameRenderer;
    private final MobController mMobsController;
    private final DropController mDropController;
    private final ContainerController mContainerController;
    private final GameWorldFluidsLogicControllerTask mGameWorldFluidsLogicControllerTask;
    private final GameWorldBlocksLogicControllerTask mGameWorldBlocksLogicControllerTask;
    private final GameWorldGrowBlocksControllerTask mGameWorldGrowBlocksControllerTask;
    private final GameWorldMobDamageControllerTask mGameWorldMobDamageControllerTask;
    private final GameWorldMobSpawnControllerTask mGameWorldMobSpawnControllerTask;
    private final GameWorldFireLogicControllerTask mGameWorldFireLogicControllerTask;
    private final FireController mFireController;
    private final ExplosionController mExplosionController;
    private final GameInputProcessor mGameInputProcessor;
    private final GameWorld mGameWorld;
    private final ProjectileController mProjectileController;
    private final WeatherSoundController mWeatherSoundController;
    private final StatsController mStatsController;
    private final OnboardingController mOnboardingController;

    private final Timer mWorldLogicTimer = new Timer();

    @Inject
    public GameProc(PerformanceMonitor performanceMonitor, GameContextRepository gameContextRepository,
                    GameWorld gameWorld,
                    MobController mobsController,
                    DropController dropController,
                    GameRenderer gameRenderer,
                    ContainerController containerController,
                    GameWorldFluidsLogicControllerTask gameWorldFluidsLogicControllerTask,
                    GameWorldBlocksLogicControllerTask gameWorldBlocksLogicControllerTask,
                    GameWorldGrowBlocksControllerTask gameWorldGrowBlocksControllerTask,
                    GameWorldMobDamageControllerTask gameWorldMobDamageControllerTask,
                    GameWorldMobSpawnControllerTask gameWorldMobSpawnControllerTask,
                    GameInputProcessor gameInputProcessor,
                    ProjectileController projectileController,
                    WeatherSoundController weatherSoundController,
                    StatsController statsController,
                    OnboardingController onboardingController,
                    GameWorldFireLogicControllerTask gameWorldFireLogicControllerTask,
                    FireController fireController,
                    ExplosionController explosionController
    ) {
        mPerformanceMonitor = performanceMonitor;
        mGameRenderer = gameRenderer;
        mMobsController = mobsController;
        mDropController = dropController;
        mContainerController = containerController;
        mGameWorldFluidsLogicControllerTask = gameWorldFluidsLogicControllerTask;
        mGameWorldBlocksLogicControllerTask = gameWorldBlocksLogicControllerTask;
        mGameWorldGrowBlocksControllerTask = gameWorldGrowBlocksControllerTask;
        mGameWorldMobDamageControllerTask = gameWorldMobDamageControllerTask;
        mGameWorldMobSpawnControllerTask = gameWorldMobSpawnControllerTask;
        mGameInputProcessor = gameInputProcessor;
        mGameWorld = gameWorld;
        mProjectileController = projectileController;
        mWeatherSoundController = weatherSoundController;
        mStatsController = statsController;
        mOnboardingController = onboardingController;
        mGameWorldFireLogicControllerTask = gameWorldFireLogicControllerTask;
        mFireController = fireController;
        mExplosionController = explosionController;

        mWorldLogicTimer.scheduleTask(gameWorldFluidsLogicControllerTask, 0,
                GameWorldFluidsLogicControllerTask.FLUID_UPDATE_INTERVAL_SEC);
        mWorldLogicTimer.scheduleTask(gameWorldBlocksLogicControllerTask, 0,
                GameWorldBlocksLogicControllerTask.WORLD_BLOCKS_LOGIC_UPDATE_INTERVAL_SEC);
        mWorldLogicTimer.scheduleTask(gameWorldGrowBlocksControllerTask,
                GameWorldGrowBlocksControllerTask.GROW_BLOCKS_UPDATE_INTERVAL_SEC,
                GameWorldGrowBlocksControllerTask.GROW_BLOCKS_UPDATE_INTERVAL_SEC);
        mWorldLogicTimer.scheduleTask(gameWorldMobDamageControllerTask, 0,
                GameWorldMobDamageControllerTask.ENVIRONMENTAL_MOB_DAMAGE_INTERVAL_SEC);
        mWorldLogicTimer.scheduleTask(gameWorldMobSpawnControllerTask,
                GameWorldMobSpawnControllerTask.SPAWN_INTERVAL_SEC,
                GameWorldMobSpawnControllerTask.SPAWN_INTERVAL_SEC);
        mWorldLogicTimer.scheduleTask(gameWorldFireLogicControllerTask, 0,
                GameWorldFireLogicControllerTask.FIRE_UPDATE_INTERVAL_SEC);

        if (!gameContextRepository.isLoadGame() ||
                mGameWorld.getTotalGameTimeSec() - mGameWorld.getLastSpawnGameTime()
                        > GameWorldMobSpawnControllerTask.SPAWN_INTERVAL_SEC) {
            PerformanceSpan spawn = performanceMonitor.beginOperation(
                    PerformanceOperation.STARTUP_SPAWN, PerformanceTrigger.INITIAL, null);
            try {
                gameWorldMobSpawnControllerTask.exec();
            } catch (Throwable error) {
                if (spawn != null) spawn.fail(error);
                throw error;
            } finally {
                if (spawn != null) spawn.finish(spawn.getResult());
            }
        }

        mGameRenderer.resetCameraToPlayer();
        mGameWorld.update(0f);
        mGameRenderer.render(0f);
        mGameWorld.getLightingSystem().recalculate();
    }

    public void setPlayerGameMode(GameMode gameMode) {
        mMobsController.getPlayer().setGameMode(gameMode);
    }

    public void update(float delta) {
        long updateStart = mPerformanceMonitor.startStage();
        mGameWorld.update(delta);
        long entitiesStart = mPerformanceMonitor.startStage();
        mMobsController.update(delta);
        mDropController.update(delta);
        mProjectileController.update(delta);
        mExplosionController.update(delta);
        mPerformanceMonitor.endStage(PerformanceMetric.ENTITIES_UPDATE, entitiesStart);
        mPerformanceMonitor.endStage(PerformanceMetric.UPDATE_TOTAL, updateStart);
        mGameRenderer.render(delta);
        long otherStart = mPerformanceMonitor.startStage();
        mGameInputProcessor.update(delta);
        mContainerController.update(delta);
        mWeatherSoundController.update();
        mStatsController.update(delta);
        mOnboardingController.update(delta);
        mPerformanceMonitor.endStage(PerformanceMetric.OTHER_UPDATE, otherStart);
        mPerformanceMonitor.endStage(PerformanceMetric.UPDATE_TOTAL, otherStart);
    }

    public void onResize() {
        mGameRenderer.onResize();
    }

    public void onGamePaused() {
        mGameInputProcessor.reset();
        mWorldLogicTimer.stop();
    }

    public void onGameResumed() {
        mWorldLogicTimer.start();
    }

    public void show() {
        Gdx.input.setInputProcessor(mGameInputProcessor);
    }

    public void respawnPlayer() {
        mGameInputProcessor.reset();
        mMobsController.respawnPlayer();
        mGameRenderer.resetCameraToPlayer();
        mGameWorld.update(0f);
        mGameRenderer.render(0f);
    }

    @Override
    public void dispose() {
        mGameWorldFluidsLogicControllerTask.shutdownBlocking();
        mGameWorldBlocksLogicControllerTask.shutdownBlocking();
        mGameWorldGrowBlocksControllerTask.shutdownBlocking();
        mGameWorldMobDamageControllerTask.shutdownBlocking();
        mGameWorldMobSpawnControllerTask.shutdownBlocking();
        mGameWorldFireLogicControllerTask.shutdownBlocking();
        mWorldLogicTimer.stop();

        mWeatherSoundController.dispose();
        mGameRenderer.dispose();
        mProjectileController.dispose();
        mDropController.dispose();
        mMobsController.dispose();
        mContainerController.dispose();
        mExplosionController.dispose();
        mFireController.dispose();
        mStatsController.dispose();
        mGameWorld.dispose();
    }
}
