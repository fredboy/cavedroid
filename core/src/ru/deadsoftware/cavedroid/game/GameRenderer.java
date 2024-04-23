package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ObjectMap;
import ru.deadsoftware.cavedroid.MainConfig;
import ru.deadsoftware.cavedroid.game.input.IGameInputHandler;
import ru.deadsoftware.cavedroid.game.input.action.KeyboardInputAction;
import ru.deadsoftware.cavedroid.game.input.action.MouseInputAction;
import ru.deadsoftware.cavedroid.game.input.action.keys.MouseInputActionKey;
import ru.deadsoftware.cavedroid.game.input.handler.mouse.CursorMouseInputHandler;
import ru.deadsoftware.cavedroid.game.input.mapper.KeyboardInputActionMapper;
import ru.deadsoftware.cavedroid.game.input.mapper.MouseInputActionMapper;
import ru.deadsoftware.cavedroid.game.mobs.MobsController;
import ru.deadsoftware.cavedroid.game.mobs.Player;
import ru.deadsoftware.cavedroid.game.objects.TouchButton;
import ru.deadsoftware.cavedroid.game.render.IGameRenderer;
import ru.deadsoftware.cavedroid.misc.Assets;
import ru.deadsoftware.cavedroid.misc.Renderer;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@GameScope
public class GameRenderer extends Renderer {

    private final MainConfig mMainConfig;
    private final MobsController mMobsController;
    private final List<IGameRenderer> mRenderers;
    private final CursorMouseInputHandler mCursorMouseInputHandler;
    private final MouseInputActionMapper mMouseInputActionMapper;
    private final KeyboardInputActionMapper mKeyboardInputActionMapper;
    private final Set<IGameInputHandler<MouseInputAction>> mMouseInputHandlers;
    private final Set<IGameInputHandler<KeyboardInputAction>> mKeyboardInputHandlers;

    @Inject
    GameRenderer(MainConfig mainConfig,
                 MobsController mobsController,
                 Set<IGameRenderer> renderers,
                 CursorMouseInputHandler cursorMouseInputHandler,
                 MouseInputActionMapper mouseInputActionMapper,
                 KeyboardInputActionMapper keyboardInputActionMapper,
                 Set<IGameInputHandler<MouseInputAction>> mouseInputHandlers,
                 Set<IGameInputHandler<KeyboardInputAction>> keyboardInputHandlers) {
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

        Gdx.gl.glClearColor(0f, .6f, .6f, 1f);
    }

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
        final MouseInputAction action = new MouseInputAction(
                Gdx.input.getX() * (viewport.width / Gdx.graphics.getWidth()),
                Gdx.input.getY() * (viewport.height / Gdx.graphics.getHeight()),
                MouseInputActionKey.None.INSTANCE,
                viewport);

        mCursorMouseInputHandler.handle(action);
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

    private boolean onMouseActionEvent(int mouseX, int mouseY, int button, boolean touchUp) {
        @CheckForNull MouseInputAction action = mMouseInputActionMapper
                .map((float) mouseX, (float) mouseY, getCameraViewport(), button, touchUp);
        return handleMouseAction(action);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        float touchX = transformScreenX(screenX);
        float touchY = transformScreenY(screenY);

        if (mMainConfig.isTouch()) {
            @CheckForNull TouchButton touchedKey = getTouchedKey(touchX, touchY);
            if (touchedKey != null && touchedKey.isMouse()) {
                return onMouseActionEvent(screenX, screenY, touchedKey.getCode(), true);
            } else if (touchedKey != null) {
                return keyUp(touchedKey.getCode());
            }
        }

        return onMouseActionEvent(screenX, screenY, button, true);
    }

    @CheckForNull
    private TouchButton getTouchedKey(float touchX, float touchY) {
        for (ObjectMap.Entry<String, TouchButton> entry : Assets.guiMap) {
            TouchButton button = entry.value;
            if (button.getRect().contains(touchX, touchY)) {
                return button;
            }
        }
        return null;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        float touchX = transformScreenX(screenX);
        float touchY = transformScreenY(screenY);

        if (mMainConfig.isTouch()) {
            @CheckForNull TouchButton touchedKey = getTouchedKey(touchX, touchY);
            if (touchedKey != null && touchedKey.isMouse()) {
                return onMouseActionEvent(screenX, screenY, touchedKey.getCode(), false);
            } else if (touchedKey != null) {
                return keyDown(touchedKey.getCode());
            }
        }

        return onMouseActionEvent(screenX, screenY, button, false);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        @CheckForNull MouseInputAction action =
                mMouseInputActionMapper.mapDragged(screenX, screenY, getCameraViewport());
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
        handleMousePosition();
//        mGameInput.moveCursor(this);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        spriter.begin();
        mRenderers.forEach(iGameRenderer -> iGameRenderer.draw(spriter, shaper, getCameraViewport(), delta));
        spriter.end();
    }

}
