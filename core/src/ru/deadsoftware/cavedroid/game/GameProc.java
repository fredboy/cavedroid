package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.utils.Disposable;

import javax.inject.Inject;

@GameScope
public class GameProc implements Disposable {

    public static final int MAX_CREATIVE_SCROLL = GameItems.getItemsSize() / 8;

    private final GameWorld mGameWorld;
    private final GamePhysics mGamePhysics;
    private final GameInput mGameInput;
    private final GameRenderer mGameRenderer;

    @Inject
    public GameProc(GameWorld gameWorld,
                    GamePhysics gamePhysics,
                    GameInput gameInput,
                    GameRenderer gameRenderer) {
        mGameWorld = gameWorld;
        mGamePhysics = gamePhysics;
        mGameInput = gameInput;
        mGameRenderer = gameRenderer;

        mGameWorld.startFluidsThread();
    }

    public void update(float delta) {
        mGamePhysics.update(delta);
        mGameInput.update();
        mGameWorld.update();
        mGameRenderer.render(delta);
    }

    @Override
    public void dispose() {
        mGameWorld.dispose();
    }
}
