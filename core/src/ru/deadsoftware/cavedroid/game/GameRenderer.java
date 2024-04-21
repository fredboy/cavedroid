package ru.deadsoftware.cavedroid.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import ru.deadsoftware.cavedroid.MainConfig;
import ru.deadsoftware.cavedroid.game.mobs.MobsController;
import ru.deadsoftware.cavedroid.game.mobs.Player;
import ru.deadsoftware.cavedroid.game.render.IGameRenderer;
import ru.deadsoftware.cavedroid.misc.Renderer;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@GameScope
public class GameRenderer extends Renderer {

    private final GameInput mGameInput;
    private final MobsController mMobsController;
    private final List<IGameRenderer> mRenderers;

    @Inject
    GameRenderer(MainConfig mainConfig,
                 GameInput gameInput,
                 MobsController mobsController,
                 Set<IGameRenderer> renderers) {
        super(mainConfig.getWidth(), mainConfig.getHeight());

        mGameInput = gameInput;
        mMobsController = mobsController;
        mRenderers = new ArrayList<>(renderers);
        mRenderers.sort(Comparator.comparingInt(IGameRenderer::getRenderLayer));

        Gdx.gl.glClearColor(0f, .6f, .6f, 1f);
    }

    private void updateCameraPosition() {
        Player player = mMobsController.getPlayer();
        setCamPos(player.getX() + player.getWidth() / 2 - getWidth() / 2,
                player.getY() + player.getHeight() / 2 - getHeight() / 2);
    }


    @Override
    public void render(float delta) {
        updateCameraPosition();
        mGameInput.moveCursor(this);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        spriter.begin();
        mRenderers.forEach(iGameRenderer -> iGameRenderer.draw(spriter, shaper, getCameraViewport(), delta));
        spriter.end();
    }

}
