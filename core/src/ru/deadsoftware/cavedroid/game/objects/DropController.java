package ru.deadsoftware.cavedroid.game.objects;

import ru.deadsoftware.cavedroid.game.GameItemsHolder;
import ru.deadsoftware.cavedroid.game.GameScope;
import ru.deadsoftware.cavedroid.game.model.item.Item;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

@GameScope
public class DropController implements Serializable {

    public interface Callback {
        void run(Drop drop);
    }

    private final LinkedList<Drop> mDrops = new LinkedList<>();

    @Inject
    public DropController() {
    }

    public void initDrops(GameItemsHolder gameItemsHolder) {
        mDrops.forEach((drop) -> drop.initItem(gameItemsHolder));
    }

    public void addDrop(float x, float y, Item item) {
        mDrops.add(new Drop(x, y, item));
    }

    public int getSize() {
        return mDrops.size();
    }

    public void forEach(Callback callback) {
        mDrops.forEach(callback::run);
    }

    public Iterator<Drop> getIterator() {
        return mDrops.iterator();
    }

}
