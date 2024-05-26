package ru.deadsoftware.cavedroid.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ObjectMap;
import ru.deadsoftware.cavedroid.CaveGame;
import ru.deadsoftware.cavedroid.MainConfig;
import ru.deadsoftware.cavedroid.menu.objects.Button;
import ru.deadsoftware.cavedroid.menu.submenus.*;
import ru.deadsoftware.cavedroid.misc.Renderer;
import ru.deadsoftware.cavedroid.misc.utils.RenderingUtilsKt;
import ru.fredboy.cavedroid.domain.assets.usecase.GetFontUseCase;
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringHeightUseCase;
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringWidthUseCase;
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase;

import javax.inject.Inject;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.deadsoftware.cavedroid.misc.Assets.*;

@MenuScope
public class MenuProc extends Renderer {

    public class Input {
        private void startNewGame(int gameMode) {
            mMainConfig.getCaveGame().newGame(gameMode);
        }

        public void newGameClicked() {
            mCurrentMenu = mMenuNewGame;
        }

        public void loadGameClicked() {
            mMainConfig.getCaveGame().loadGame();
        }

        public void optionsClicked() {
            mCurrentMenu = mMenuOptions;
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

        public void setPreference(String key, Object value) {
            mMainConfig.setPreference(key, value.toString());
        }
    }

    private final MainConfig mMainConfig;

    private final MenuMain mMenuMain;
    private final MenuNewGame mMenuNewGame;
    private final MenuOptions mMenuOptions;

    private final GetFontUseCase mGetFontUseCase;
    private final GetStringWidthUseCase mGetStringWidthUseCase;
    private final GetStringHeightUseCase mGetStringHeightUseCase;

    private final GetTextureRegionByNameUseCase mGetTextureRegionByNameUseCase;

    private Menu mCurrentMenu;

    @Inject
    public MenuProc(
            MainConfig mainConfig,
            MenusFactory menusFactory,
            GetFontUseCase getFontUseCase,
            GetStringWidthUseCase getStringWidthUseCase,
            GetStringHeightUseCase getStringHeightUseCase,
            GetTextureRegionByNameUseCase getTextureRegionByNameUseCase
    ) {
        super(mainConfig.getWidth(), mainConfig.getHeight());

        mMainConfig = mainConfig;
        mGetFontUseCase = getFontUseCase;
        mGetStringWidthUseCase = getStringWidthUseCase;
        mGetStringHeightUseCase = getStringHeightUseCase;
        mGetTextureRegionByNameUseCase = getTextureRegionByNameUseCase;

        Input menuInput = new Input();

        mMenuMain = menusFactory.getMainMenu(getWidth(), getHeight(), this::drawButton, menuInput);
        mMenuNewGame = menusFactory.getMenuNewGame(getWidth(), getHeight(), this::drawButton, menuInput);
        mMenuOptions = menusFactory.getMenuOptions(getWidth(), getHeight(), this::drawButton, menuInput);

        mCurrentMenu = mMenuMain;
    }

    private String processVariables(String raw) {
        final Pattern pattern = Pattern.compile("%%([A-Za-z]+)%%", Pattern.CASE_INSENSITIVE);
        final Matcher matcher = pattern.matcher(raw);
        while (matcher.find()) {
            for (int i = 0; i < matcher.groupCount(); i++) {
                try {
                    final String group = matcher.group(i);
                    final String name = group.replaceAll("%%", "");
                    final Method method = mMainConfig.getClass().getMethod(name);
                    final String value = method.invoke(mMainConfig).toString();
                    raw = raw.replace(group, value);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return raw;
    }

    private void drawButton(Button button) {
        spriter.draw(mGetTextureRegionByNameUseCase.get("button_" + button.getType()), button.getX(), button.getY());

        String label = processVariables(button.getLabel());

        RenderingUtilsKt.drawString(spriter, mGetFontUseCase.invoke(), label,
                (button.getX() + button.getWidth() / 2) - mGetStringWidthUseCase.invoke(label) / 2,
                (button.getY() + button.getHeight() / 2) - mGetStringHeightUseCase.invoke(label) / 2);
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
        RenderingUtilsKt.drawString(spriter, mGetFontUseCase.invoke(), "CaveDroid " + CaveGame.VERSION, 0,
                getHeight() - mGetStringHeightUseCase.invoke("CaveDroid " + CaveGame.VERSION) * 1.5f);
        spriter.end();
    }

    public void reset() {
        mCurrentMenu = mMenuMain;
    }
}
