package ru.deadsoftware.cavedroid.game.mobs;

import com.badlogic.gdx.Gdx;
import ru.deadsoftware.cavedroid.game.GameScope;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.LinkedList;

@GameScope
public class MobsController {

    public interface Callback {
        void run(Mob mob);
    }

    private static final String TAG = "MobsController";

    private final Player mPlayer;
    private final LinkedList<Mob> mMobs = new LinkedList<>();

    @Inject
    public MobsController() {
        mPlayer = new Player();
    }

    public Player getPlayer() {
        return mPlayer;
    }

    public void addMob(Class<? extends Mob> mobClass, float x, float y) {
        try {
            mMobs.add(mobClass.getConstructor(float.class, float.class).newInstance(x, y));
        } catch (Exception e) {
            Gdx.app.error(TAG, e.getMessage());
        }
    }

    public int getSize() {
        return mMobs.size();
    }

    public void forEach(Callback callback) {
        mMobs.forEach(callback::run);
    }

    public Iterator<Mob> getIterator() {
        return mMobs.iterator();
    }

}
