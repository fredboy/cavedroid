package emu.java.util.concurrent.atomic;

public class AtomicReferenceArray<E> {

    private final Object[] array;

    public AtomicReferenceArray(int length) {
        this.array = new Object[length];
    }

    public AtomicReferenceArray(E[] values) {
        this.array = values.clone();
    }

    public final int length() {
        return array.length;
    }

    @SuppressWarnings("unchecked")
    public final E get(int i) {
        return (E) array[i];
    }

    public final void set(int i, E newValue) {
        array[i] = newValue;
    }

    public final void lazySet(int i, E newValue) {
        array[i] = newValue;
    }

    @SuppressWarnings("unchecked")
    public final E getAndSet(int i, E newValue) {
        E previous = (E) array[i];
        array[i] = newValue;
        return previous;
    }

    public final boolean compareAndSet(int i, E expect, E update) {
        if (array[i] == expect) {
            array[i] = update;
            return true;
        }
        return false;
    }

    public final boolean weakCompareAndSet(int i, E expect, E update) {
        return compareAndSet(i, expect, update);
    }
}
