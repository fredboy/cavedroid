package ru.deadsoftware.cavedroid.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ObjectMap;
import ru.deadsoftware.cavedroid.CaveGame;
import ru.deadsoftware.cavedroid.MainConfig;
import ru.deadsoftware.cavedroid.menu.objects.Button;
import ru.deadsoftware.cavedroid.menu.submenus.Menu;
import ru.deadsoftware.cavedroid.menu.submenus.MenuMain;
import ru.deadsoftware.cavedroid.menu.submenus.MenuNewGame;
import ru.deadsoftware.cavedroid.misc.Renderer;

import javax.inject.Inject;

import static ru.deadsoftware.cavedroid.misc.Assets.*;

@MenuScope
public class MenuProc extends Renderer {

    public class Input {
        private void startNewGame(int gameMode) {
            mMainConfig.getCaveGame().newGame();
        }

        public void newGameClicked() {
            mCurrentMenu = mMenuNewGame;
        }

        public void loadGameClicked() {
            mMainConfig.getCaveGame().loadGame();
        }

        public void quitClicked() {
            Gdx.app.exit();
        }

        public void survivalClicked() {
            startNewGame(0);
        }

        public void creativeClicked() {
            startNewGame(1);
        }

        public void backClicked() {
            mCurrentMenu = mMenuMain;
        }
    }

    private final MainConfig mMainConfig;

    private final MenuMain mMenuMain;
    private final MenuNewGame mMenuNewGame;

    private Menu mCurrentMenu;

    @Inject
    public MenuProc(MainConfig mainConfig) {
        super(mainConfig.getWidth(), mainConfig.getHeight());

        mMainConfig = mainConfig;

        Input menuInput = new Input();

        mMenuMain = new MenuMain(getWidth(), getHeight(), this::drawButton, mainConfig, menuInput);
        mMenuNewGame = new MenuNewGame(getWidth(), getHeight(), this::drawButton, mainConfig, menuInput);

        mCurrentMenu = mMenuMain;
    }

    private void drawButton(Button button) {
        spriter.draw(textureRegions.get("button_" + button.getType()), button.getX(), button.getY());
        setFontColor(255, 255, 255);
        drawString(button.getLabel(),
                (button.getX() + button.getWidth() / 2) - (float) getStringWidth(button.getLabel()) / 2,
                (button.getY() + button.getHeight() / 2) - (float) getStringHeight(button.getLabel()) / 2);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int mb) {
        screenX *= getWidth() / Gdx.graphics.getWidth();
        screenY *= getHeight() / Gdx.graphics.getHeight();
        for (ObjectMap.Entry<String, Button> entry : mCurrentMenu.getButtons()) {
            Button button = entry.value;
            if (button.getRect().contains(screenX, screenY)) {
                if (button.getType() > 0) {
                    button.clicked();
                }
                break;
            }
        }
        return false;
    }

    @Override
    public void render(float delta) {
        spriter.begin();
        mCurrentMenu.draw(spriter);
        drawString("CaveDroid " + CaveGame.VERSION, 0,
                getHeight() - getStringHeight("CaveDroid " + CaveGame.VERSION) * 1.5f);
        spriter.end();
    }

    public void reset() {
        mCurrentMenu = mMenuMain;
    }
}
