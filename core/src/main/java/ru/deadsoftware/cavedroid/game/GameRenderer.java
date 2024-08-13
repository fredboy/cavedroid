package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import org.jetbrains.annotations.Nullable;
import ru.deadsoftware.cavedroid.MainConfig;
import ru.deadsoftware.cavedroid.game.input.IKeyboardInputHandler;
import ru.deadsoftware.cavedroid.game.input.IMouseInputHandler;
import ru.deadsoftware.cavedroid.game.input.Joystick;
import ru.deadsoftware.cavedroid.game.input.action.KeyboardInputAction;
import ru.deadsoftware.cavedroid.game.input.action.MouseInputAction;
import ru.deadsoftware.cavedroid.game.input.action.keys.MouseInputActionKey;
import ru.deadsoftware.cavedroid.game.input.handler.mouse.CursorMouseInputHandler;
import ru.deadsoftware.cavedroid.game.input.mapper.KeyboardInputActionMapper;
import ru.deadsoftware.cavedroid.game.input.mapper.MouseInputActionMapper;
import ru.deadsoftware.cavedroid.game.render.IGameRenderer;
import ru.deadsoftware.cavedroid.game.ui.TooltipManager;
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager;
import ru.deadsoftware.cavedroid.misc.Renderer;
import ru.fredboy.cavedroid.common.di.GameScope;
import ru.fredboy.cavedroid.common.utils.MeasureUnitsUtilsKt;
import ru.fredboy.cavedroid.common.utils.RenderingUtilsKt;
import ru.fredboy.cavedroid.domain.assets.model.TouchButton;
import ru.fredboy.cavedroid.domain.assets.usecase.GetFontUseCase;
import ru.fredboy.cavedroid.domain.assets.usecase.GetTouchButtonsUseCase;
import ru.fredboy.cavedroid.game.controller.mob.MobController;
import ru.fredboy.cavedroid.game.controller.mob.model.Player;
import ru.fredboy.cavedroid.game.world.GameWorld;

import javax.inject.Inject;
import java.util.*;

@GameScope
public class GameRenderer extends Renderer {

    private static final float CAMERA_SPEED = 72f;
    private static final float MAX_CAM_DISTANCE_FROM_PLAYER = 64f;
    private static final float DRAG_THRESHOLD = 1f;
    private static final TouchButton nullButton = new TouchButton(new Rectangle(), -1, true);

    private final MainConfig mMainConfig;
    private final MobController mMobsController;
    private final GameWorld mGameWorld;
    private final List<IGameRenderer> mRenderers;
    private final CursorMouseInputHandler mCursorMouseInputHandler;
    private final MouseInputActionMapper mMouseInputActionMapper;
    private final KeyboardInputActionMapper mKeyboardInputActionMapper;
    private final Set<IMouseInputHandler> mMouseInputHandlers;
    private final Set<IKeyboardInputHandler> mKeyboardInputHandlers;
    private final GameWindowsManager mGameWindowsManager;
    private final TooltipManager mTooltipManager;
    private final GetFontUseCase mGetFontUseCase;
    private final GetTouchButtonsUseCase mGetTouchButtonsUseCase;

    private final TouchButton mouseLeftTouchButton, mouseRightTouchButton;

    private final Vector2 mCamCenterToPlayer = new Vector2();

    private float mTouchDownX, mTouchDownY;
    private long mCameraDelayMs = 0L;

    @Inject
    GameRenderer(MainConfig mainConfig,
                 MobController mobsController,
                 GameWorld gameWorld,
                 Set<IGameRenderer> renderers,
                 CursorMouseInputHandler cursorMouseInputHandler,
                 MouseInputActionMapper mouseInputActionMapper,
                 KeyboardInputActionMapper keyboardInputActionMapper,
                 Set<IMouseInputHandler> mouseInputHandlers,
                 Set<IKeyboardInputHandler> keyboardInputHandlers,
                 GameWindowsManager gameWindowsManager,
                 TooltipManager tooltipManager,
                 GetFontUseCase getFontUseCase,
                 GetTouchButtonsUseCase getTouchButtonsUseCase) {
        super(mainConfig.getWidth(), mainConfig.getHeight());

        mMainConfig = mainConfig;
        mMobsController = mobsController;
        mGameWorld = gameWorld;
        mRenderers = new ArrayList<>(renderers);
        kotlin.collections.CollectionsKt.sortWith(mRenderers, new Comparator<IGameRenderer>() {
            @Override
            public int compare(IGameRenderer o1, IGameRenderer o2) {
                return o1.getRenderLayer() - o2.getRenderLayer();
            }
        });
        mCursorMouseInputHandler = cursorMouseInputHandler;
        mMouseInputActionMapper = mouseInputActionMapper;
        mKeyboardInputActionMapper = keyboardInputActionMapper;
        mMouseInputHandlers = mouseInputHandlers;
        mKeyboardInputHandlers = keyboardInputHandlers;
        mGameWindowsManager = gameWindowsManager;
        mTooltipManager = tooltipManager;
        mGetFontUseCase = getFontUseCase;
        mGetTouchButtonsUseCase = getTouchButtonsUseCase;

        mouseLeftTouchButton = new TouchButton(new Rectangle(getWidth() / 2, 0f, getWidth() / 2, getHeight() / 2), Input.Buttons.LEFT, true);
        mouseRightTouchButton = new TouchButton(new Rectangle(getWidth() / 2, getHeight() / 2, getWidth() / 2, getHeight() / 2), Input.Buttons.RIGHT, true);

        mMainConfig.setJoystick(new Joystick(mMobsController.getPlayer().getSpeed()));

        Gdx.gl.glClearColor(0f, .6f, .6f, 1f);
    }

    private void updateDynamicCameraPosition(float delta) {
        Player player = mMobsController.getPlayer();

        float plTargetX = player.getX() + player.getWidth() / 2;
        float plTargetY = player.getY() + player.getHeight() / 2;

        float camCenterX = getCamX() + getWidth() / 2;
        float camCenterY = getCamY() + getHeight() / 2;

        float camTargetX, camTargetY;

        boolean followPlayer = player.getControlMode() == Player.ControlMode.WALK || !mMainConfig.isTouch();

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
        if (mMainConfig.isUseDynamicCamera()) {
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
        final Rectangle viewport = getCameraViewport();

        final float screenX = transformScreenX(Gdx.input.getX());
        final float screenY = transformScreenY(Gdx.input.getY());

        final MouseInputAction action = new MouseInputAction(
                screenX,
                screenY,
                MouseInputActionKey.None.INSTANCE,
                viewport);

        mCursorMouseInputHandler.handle(action);

        if (!mTooltipManager.getCurrentMouseTooltip().isEmpty()) {
            RenderingUtilsKt.drawString(spriter, mGetFontUseCase.invoke(),
                    mTooltipManager.getCurrentMouseTooltip(), screenX + 1, screenY + 1, Color.BLACK);
            RenderingUtilsKt.drawString(spriter, mGetFontUseCase.invoke(),
                    mTooltipManager.getCurrentMouseTooltip(), screenX, screenY, Color.WHITE);
        }
    }

    private boolean handleMouseAction(@Nullable MouseInputAction action) {
        if (action == null) {
            return false;
        }

        boolean anyProcessed = false;

        for (IMouseInputHandler handler : mMouseInputHandlers) {
            final boolean conditions = handler.checkConditions(action);
            if (conditions) {
                anyProcessed = true;
                handler.handle(action);
                break;
            }
//            anyProcessed = anyProcessed || conditions;
        }
        return anyProcessed;
    }

    private boolean onMouseActionEvent(int mouseX, int mouseY, int button, boolean touchUp, int pointer) {
        @Nullable MouseInputAction action = mMouseInputActionMapper
                .map((float) mouseX, (float) mouseY, getCameraViewport(), button, touchUp, pointer);
        return handleMouseAction(action);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        float touchX = transformScreenX(screenX);
        float touchY = transformScreenY(screenY);

        final Joystick joy = mMainConfig.getJoystick();

        if (mMainConfig.isTouch()) {
            if (joy != null && joy.getActive() && joy.getPointer() == pointer) {
                return onMouseActionEvent(screenX, screenY, nullButton.getCode(), true, pointer);
            }

            TouchButton touchedKey = getTouchedKey(touchX, touchY);
            if (touchedKey.isMouse()) {
                return onMouseActionEvent(screenX, screenY, touchedKey.getCode(), true, pointer);
            } else {
                return keyUp(touchedKey.getCode());
            }
        }

        return onMouseActionEvent(screenX, screenY, button, true, pointer);
    }

    private TouchButton getTouchedKey(float touchX, float touchY) {
        if (mGameWindowsManager.getCurrentWindowType() != GameUiWindow.NONE) {
            return nullButton;
        }
        for (Map.Entry<String, TouchButton> entry : mGetTouchButtonsUseCase.invoke().entrySet()) {
            TouchButton button = entry.getValue();
            if (button.getRectangle().contains(touchX, touchY)) {
                return button;
            }
        }

        if (mouseLeftTouchButton.getRectangle().contains(touchX, touchY)) {
            return mouseLeftTouchButton;
        }

        if (mouseRightTouchButton.getRectangle().contains(touchX, touchY)) {
            return mouseRightTouchButton;
        }

        return nullButton;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        float touchX = transformScreenX(screenX);
        float touchY = transformScreenY(screenY);

        mTouchDownX = touchX;
        mTouchDownY = touchY;

        if (mMainConfig.isTouch()) {
            TouchButton touchedKey = getTouchedKey(touchX, touchY);
            if (touchedKey.isMouse()) {
                return onMouseActionEvent(screenX, screenY, touchedKey.getCode(), false, pointer);
            } else {
                return keyDown(touchedKey.getCode());
            }
        }

        return onMouseActionEvent(screenX, screenY, button, false, pointer);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        float touchX = transformScreenX(screenX);
        float touchY = transformScreenY(screenY);

        if (Math.abs(touchX - mTouchDownX) < 16 && Math.abs(touchY - mTouchDownY) < DRAG_THRESHOLD) {
            return false;
        }

        @Nullable MouseInputAction action =
                mMouseInputActionMapper.mapDragged(screenX, screenY, getCameraViewport(), pointer);
        return handleMouseAction(action);
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        @Nullable MouseInputAction action = mMouseInputActionMapper
                .mapScrolled(Gdx.input.getX(), Gdx.input.getY(), amountX, amountY, getCameraViewport());
        return handleMouseAction(action);
    }

    private boolean handleKeyboardAction(int keycode, boolean isKeyDown) {
        @Nullable final KeyboardInputAction action = mKeyboardInputActionMapper
                .map(keycode, isKeyDown);

        if (action == null) {
            return false;
        }

        boolean anyProcessed = false;

        for (IKeyboardInputHandler handler : mKeyboardInputHandlers) {
            final boolean conditions = handler.checkConditions(action);
            if (conditions) {
                anyProcessed = true;
                handler.handle(action);
                break;
            }
        }

        return anyProcessed;
    }

    @Override
    public boolean keyDown(int keycode) {
        return handleKeyboardAction(keycode, true);
    }

    @Override
    public boolean keyUp(int keycode) {
        return handleKeyboardAction(keycode, false);
    }

    @Override
    public void render(float delta) {
        updateCameraPosition(delta);

        if (mMainConfig.getJoystick() != null && mMainConfig.getJoystick().getActive()) {
            mMainConfig.getJoystick().updateState(
                    transformScreenX(Gdx.input.getX(mMainConfig.getJoystick().getPointer())),
                    transformScreenY(Gdx.input.getY(mMainConfig.getJoystick().getPointer()))
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
