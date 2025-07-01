package ru.fredboy.cavedroid.ux.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import org.jetbrains.annotations.Nullable;
import ru.fredboy.cavedroid.common.di.GameScope;
import ru.fredboy.cavedroid.common.utils.MeasureUnitsUtilsKt;
import ru.fredboy.cavedroid.common.utils.RenderingUtilsKt;
import ru.fredboy.cavedroid.domain.assets.usecase.GetFontUseCase;
import ru.fredboy.cavedroid.domain.configuration.model.CameraContext;
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository;
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository;
import ru.fredboy.cavedroid.entity.mob.model.Player;
import ru.fredboy.cavedroid.game.controller.mob.MobController;
import ru.fredboy.cavedroid.game.window.TooltipManager;
import ru.fredboy.cavedroid.game.world.GameWorld;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@GameScope
public class GameRenderer {

    private static final float CAMERA_SPEED = 72f;
    private static final float MAX_CAM_DISTANCE_FROM_PLAYER = 64f;
    private final ApplicationContextRepository mApplicationContextRepository;
    private final GameContextRepository mGameContextRepository;
    private final MobController mMobsController;
    private final GameWorld mGameWorld;
    private final List<IGameRenderer> mRenderers;
    private final TooltipManager mTooltipManager;
    private final GetFontUseCase mGetFontUseCase;
    private final Vector2 mCamCenterToPlayer = new Vector2();
    private long mCameraDelayMs = 0L;

    private final ShapeRenderer shaper;
    private final SpriteBatch spriter;

    @Inject
    public GameRenderer(ApplicationContextRepository applicationContextRepository,
                GameContextRepository gameContextRepository,
                 MobController mobsController,
                 GameWorld gameWorld,
                 Set<IGameRenderer> renderers,
                 TooltipManager tooltipManager,
                 GetFontUseCase getFontUseCase) {
        @Nullable final CameraContext cameraContext = gameContextRepository.getCameraContext();

        shaper = new ShapeRenderer();
        spriter = new SpriteBatch();

        if (cameraContext != null) {
            shaper.setProjectionMatrix(cameraContext.getCamera().combined);
            spriter.setProjectionMatrix(cameraContext.getCamera().combined);
        } else {
            Gdx.app.error("GameRenderer", "Camera context was not set");
        }

        mApplicationContextRepository = applicationContextRepository;
        mGameContextRepository = gameContextRepository;
        mMobsController = mobsController;
        mGameWorld = gameWorld;
        mRenderers = new ArrayList<>(renderers);
        kotlin.collections.CollectionsKt.sortWith(mRenderers, new Comparator<IGameRenderer>() {
            @Override
            public int compare(IGameRenderer o1, IGameRenderer o2) {
                return o1.getRenderLayer() - o2.getRenderLayer();
            }
        });
        mTooltipManager = tooltipManager;
        mGetFontUseCase = getFontUseCase;

        Gdx.gl.glClearColor(0f, .6f, .6f, 1f);
    }

    private float getCamX() {
        @Nullable final CameraContext cameraContext = mGameContextRepository.getCameraContext();
        if (cameraContext != null) {
            return cameraContext.getViewport().x;
        } else {
            Gdx.app.error("GameRenderer", "Camera context was not set");
            return 0;
        }
    }

    private float getCamY() {
        @Nullable final CameraContext cameraContext = mGameContextRepository.getCameraContext();
        if (cameraContext != null) {
            return cameraContext.getViewport().y;
        } else {
            Gdx.app.error("GameRenderer", "Camera context was not set");
            return 0;
        }
    }

    private float getWidth() {
        return mApplicationContextRepository.getWidth();
    }
    private float getHeight() {
        return mApplicationContextRepository.getHeight();
    }

    private void setCamPos(float x, float y) {
        @Nullable final CameraContext cameraContext = mGameContextRepository.getCameraContext();

        if (cameraContext != null) {
            cameraContext.getCamera().position.set(x, y, 0);
            cameraContext.getViewport().x = x;
            cameraContext.getViewport().y = y;
        } else {
            Gdx.app.error("GameRenderer", "Camera context was not set");
        }
    }

    private Rectangle getCameraViewport() {
        @Nullable final CameraContext cameraContext = mGameContextRepository.getCameraContext();
        return cameraContext != null ? cameraContext.getViewport() : new Rectangle();
    }

    private void updateDynamicCameraPosition(float delta) {
        Player player = mMobsController.getPlayer();

        float plTargetX = player.getX() + player.getWidth() / 2;
        float plTargetY = player.getY() + player.getHeight() / 2;

        float camCenterX = getCamX() + getWidth() / 2;
        float camCenterY = getCamY() + getHeight() / 2;

        float camTargetX, camTargetY;

        boolean followPlayer = player.getControlMode() == Player.ControlMode.WALK || !mApplicationContextRepository.isTouch();

        if (followPlayer) {
            camTargetX = plTargetX + Math.min(player.getVelocity().x * 2, getWidth() / 2);
            camTargetY = plTargetY + player.getVelocity().y;
        } else {
            camTargetX = MeasureUnitsUtilsKt.getPx(player.getCursorX()) + MeasureUnitsUtilsKt.getPx(1) / 2;
            camTargetY = MeasureUnitsUtilsKt.getPx(player.getCursorY()) + MeasureUnitsUtilsKt.getPx(1) / 2;
        }

        Vector2 moveVector = new Vector2(camTargetX - camCenterX, camTargetY - camCenterY);

        if (followPlayer && player.getVelocity().isZero()) {
            mCameraDelayMs = TimeUtils.millis();
            mCamCenterToPlayer.x = plTargetX - camCenterX;
            mCamCenterToPlayer.y = plTargetY - camCenterY;
        }

        if (TimeUtils.timeSinceMillis(mCameraDelayMs) < 500L && !player.getVelocity().isZero()) {
            updateStaticCameraPosition(plTargetX - mCamCenterToPlayer.x,
                    camCenterY + moveVector.y * delta * 2);
            return;
        }

        float camX = getCamX();
        float camY = getCamY();
        float fullWorldPx = MeasureUnitsUtilsKt.getPx(mGameWorld.getWidth());
        float worldWidthScreenOffset = fullWorldPx - getWidth() / 2;

        if (moveVector.x >= worldWidthScreenOffset) {
            camX += fullWorldPx;
            moveVector.x -= fullWorldPx;
        } else if (moveVector.x <= -worldWidthScreenOffset) {
            camX -= fullWorldPx;
            moveVector.x += fullWorldPx;
        }

        setCamPos(camX + moveVector.x * delta * 2, camY + moveVector.y * delta * 2);


        camX = getCamX();
        camY = getCamY();

        if (camX + getWidth() / 2 > plTargetX + MAX_CAM_DISTANCE_FROM_PLAYER) {
            camX = plTargetX + MAX_CAM_DISTANCE_FROM_PLAYER - getWidth() / 2;
        }

        if (camY + getHeight() / 2 > plTargetY + MAX_CAM_DISTANCE_FROM_PLAYER) {
            camY = plTargetY + MAX_CAM_DISTANCE_FROM_PLAYER - getHeight() / 2;
        }

        if (camX + getWidth() / 2 < plTargetX - MAX_CAM_DISTANCE_FROM_PLAYER) {
            camX = plTargetX - MAX_CAM_DISTANCE_FROM_PLAYER - getWidth() / 2;
        }

        if (camY + getHeight() / 2 < plTargetY - MAX_CAM_DISTANCE_FROM_PLAYER) {
            camY = plTargetY - MAX_CAM_DISTANCE_FROM_PLAYER - getHeight() / 2;
        }

        setCamPos(camX, camY);
    }

    private void updateStaticCameraPosition(float targetX, float targetY) {
        setCamPos(targetX - getWidth() / 2, targetY - getHeight() / 2);
    }

    private void updateStaticCameraPosition() {
        Player player = mMobsController.getPlayer();

        updateStaticCameraPosition(player.getX() + player.getWidth() / 2,
                player.getY() + player.getHeight() / 2);
    }

    private void updateCameraPosition(float delta) {
        if (mApplicationContextRepository.useDynamicCamera()) {
            updateDynamicCameraPosition(delta);
        } else {
            updateStaticCameraPosition();
        }
    }

    private float transformScreenX(int screenX) {
        return getWidth() / Gdx.graphics.getWidth() * screenX;
    }

    private float transformScreenY(int screenY) {
        return getHeight() / Gdx.graphics.getHeight() * screenY;
    }

    private void handleMousePosition() {
        final float screenX = transformScreenX(Gdx.input.getX());
        final float screenY = transformScreenY(Gdx.input.getY());

        if (!mTooltipManager.getCurrentMouseTooltip().isEmpty()) {
            RenderingUtilsKt.drawString(spriter, mGetFontUseCase.invoke(),
                    mTooltipManager.getCurrentMouseTooltip(), screenX + 1, screenY + 1, Color.BLACK);
            RenderingUtilsKt.drawString(spriter, mGetFontUseCase.invoke(),
                    mTooltipManager.getCurrentMouseTooltip(), screenX, screenY, Color.WHITE);
        }
    }

    public void render(float delta) {
        updateCameraPosition(delta);

        if (mGameContextRepository.getJoystick() != null && mGameContextRepository.getJoystick().getActive()) {
            mGameContextRepository.getJoystick().updateState(
                    transformScreenX(Gdx.input.getX(mGameContextRepository.getJoystick().getPointer())),
                    transformScreenY(Gdx.input.getY(mGameContextRepository.getJoystick().getPointer()))
            );
        }

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        spriter.begin();
        for (IGameRenderer iGameRenderer : mRenderers) {
            iGameRenderer.draw(spriter, shaper, getCameraViewport(), delta);
        }
        handleMousePosition();
        spriter.end();

    }

}
