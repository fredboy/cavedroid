package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ObjectMap;
import ru.deadsoftware.cavedroid.MainConfig;
import ru.deadsoftware.cavedroid.game.input.IGameInputHandler;
import ru.deadsoftware.cavedroid.game.input.Joystick;
import ru.deadsoftware.cavedroid.game.input.action.KeyboardInputAction;
import ru.deadsoftware.cavedroid.game.input.action.MouseInputAction;
import ru.deadsoftware.cavedroid.game.input.action.keys.MouseInputActionKey;
import ru.deadsoftware.cavedroid.game.input.handler.mouse.CursorMouseInputHandler;
import ru.deadsoftware.cavedroid.game.input.mapper.KeyboardInputActionMapper;
import ru.deadsoftware.cavedroid.game.input.mapper.MouseInputActionMapper;
import ru.deadsoftware.cavedroid.game.mobs.MobsController;
import ru.deadsoftware.cavedroid.game.mobs.player.Player;
import ru.deadsoftware.cavedroid.game.objects.TouchButton;
import ru.deadsoftware.cavedroid.game.render.IGameRenderer;
import ru.deadsoftware.cavedroid.game.ui.TooltipManager;
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager;
import ru.deadsoftware.cavedroid.misc.Assets;
import ru.deadsoftware.cavedroid.misc.Renderer;
import ru.deadsoftware.cavedroid.misc.utils.RenderingUtilsKt;
import ru.deadsoftware.cavedroid.misc.utils.SpriteUtilsKt;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@GameScope
public class GameRenderer extends Renderer {

    private static final float DRAG_THRESHOLD = 1f;
    private static final TouchButton nullButton = new TouchButton(null, -1, true);

    private final MainConfig mMainConfig;
    private final MobsController mMobsController;
    private final List<IGameRenderer> mRenderers;
    private final CursorMouseInputHandler mCursorMouseInputHandler;
    private final MouseInputActionMapper mMouseInputActionMapper;
    private final KeyboardInputActionMapper mKeyboardInputActionMapper;
    private final Set<IGameInputHandler<MouseInputAction>> mMouseInputHandlers;
    private final Set<IGameInputHandler<KeyboardInputAction>> mKeyboardInputHandlers;
    private final GameWindowsManager mGameWindowsManager;
    private final TooltipManager mTooltipManager;

    private final TouchButton mouseLeftTouchButton, mouseRightTouchButton;

    @Inject
    GameRenderer(MainConfig mainConfig,
                 MobsController mobsController,
                 Set<IGameRenderer> renderers,
                 CursorMouseInputHandler cursorMouseInputHandler,
                 MouseInputActionMapper mouseInputActionMapper,
                 KeyboardInputActionMapper keyboardInputActionMapper,
                 Set<IGameInputHandler<MouseInputAction>> mouseInputHandlers,
                 Set<IGameInputHandler<KeyboardInputAction>> keyboardInputHandlers,
                 GameWindowsManager gameWindowsManager,
                 TooltipManager tooltipManager) {
        super(mainConfig.getWidth(), mainConfig.getHeight());

        mMainConfig = mainConfig;
        mMobsController = mobsController;
        mRenderers = new ArrayList<>(renderers);
        mRenderers.sort(Comparator.comparingInt(IGameRenderer::getRenderLayer));
        mCursorMouseInputHandler = cursorMouseInputHandler;
        mMouseInputActionMapper = mouseInputActionMapper;
        mKeyboardInputActionMapper = keyboardInputActionMapper;
        mMouseInputHandlers = mouseInputHandlers;
        mKeyboardInputHandlers = keyboardInputHandlers;
        mGameWindowsManager = gameWindowsManager;
        mTooltipManager = tooltipManager;

        mouseLeftTouchButton = new TouchButton(new Rectangle(getWidth() / 2, 0f, getWidth() / 2, getHeight() / 2), Input.Buttons.LEFT, true);
        mouseRightTouchButton = new TouchButton(new Rectangle(getWidth() / 2, getHeight() / 2, getWidth() / 2, getHeight() / 2), Input.Buttons.RIGHT, true);

        mMainConfig.setJoystick(new Joystick(mMobsController.getPlayer().getSpeed()));

        Gdx.gl.glClearColor(0f, .6f, .6f, 1f);
    }

    private float mTouchDownX, mTouchDownY;

    private void updateCameraPosition() {
        Player player = mMobsController.getPlayer();
        setCamPos(player.getX() + player.getWidth() / 2 - getWidth() / 2,
                player.getY() + player.getHeight() / 2 - getHeight() / 2);
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
            RenderingUtilsKt.drawString(spriter, mTooltipManager.getCurrentMouseTooltip(), screenX + 1, screenY + 1, Color.BLACK);
            RenderingUtilsKt.drawString(spriter, mTooltipManager.getCurrentMouseTooltip(), screenX, screenY, Color.WHITE);
        }
    }

    private boolean handleMouseAction(@CheckForNull  MouseInputAction action) {
        if (action == null) {
            return false;
        }

        boolean anyProcessed = false;

        for (IGameInputHandler<MouseInputAction> handler : mMouseInputHandlers) {
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
        @CheckForNull MouseInputAction action = mMouseInputActionMapper
                .map((float) mouseX, (float) mouseY, getCameraViewport(), button, touchUp, pointer);
        return handleMouseAction(action);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        float touchX = transformScreenX(screenX);
        float touchY = transformScreenY(screenY);

        if (mMainConfig.isTouch()) {
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
        for (ObjectMap.Entry<String, TouchButton> entry : Assets.guiMap) {
            TouchButton button = entry.value;
            if (button.getRect().contains(touchX, touchY)) {
                return button;
            }
        }

        if (mouseLeftTouchButton.getRect().contains(touchX, touchY)) {
            return mouseLeftTouchButton;
        }

        if (mouseRightTouchButton.getRect().contains(touchX, touchY)) {
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

        @CheckForNull MouseInputAction action =
                mMouseInputActionMapper.mapDragged(screenX, screenY, getCameraViewport(), pointer);
        return handleMouseAction(action);
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        @CheckForNull MouseInputAction action = mMouseInputActionMapper
                .mapScrolled(Gdx.input.getX(), Gdx.input.getY(), amountX, amountY, getCameraViewport());
        return handleMouseAction(action);
    }

    private boolean handleKeyboardAction(int keycode, boolean isKeyDown) {
        @CheckForNull final KeyboardInputAction action = mKeyboardInputActionMapper
                .map(keycode, isKeyDown);

        if (action == null) {
            return false;
        }

        boolean anyProcessed = false;

        for (IGameInputHandler<KeyboardInputAction> handler : mKeyboardInputHandlers) {
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
        updateCameraPosition();

        if (mMainConfig.getJoystick() != null && mMainConfig.getJoystick().getActive()) {
            mMainConfig.getJoystick().updateState(
                    transformScreenX(Gdx.input.getX(mMainConfig.getJoystick().getPointer())),
                    transformScreenY(Gdx.input.getY(mMainConfig.getJoystick().getPointer()))
            );
        }

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        spriter.begin();
        mRenderers.forEach(iGameRenderer -> iGameRenderer.draw(spriter, shaper, getCameraViewport(), delta));
        handleMousePosition();
        spriter.end();

    }

}
