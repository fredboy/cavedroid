package ru.deadsoftware.cavedroid;

import android.content.pm.PackageManager;
import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.hideStatusBar = true;
        config.useImmersiveMode = true;
        String gameFolder = null;
        try {
            gameFolder = getPackageManager().getPackageInfo(getPackageName(), 0).applicationInfo.dataDir;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            exit();
        }
        initialize(new CaveGame(gameFolder, true), config);
    }

    @Override
    public void onBackPressed() {
    }
}
