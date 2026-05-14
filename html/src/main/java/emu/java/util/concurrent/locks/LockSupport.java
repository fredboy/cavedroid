package emu.java.util.concurrent.locks;

public class LockSupport {

    private LockSupport() {
    }

    public static void unpark(Thread thread) {
    }

    public static void park() {
    }

    public static void park(Object blocker) {
    }

    public static void parkNanos(long nanos) {
    }

    public static void parkNanos(Object blocker, long nanos) {
    }

    public static void parkUntil(long deadline) {
    }

    public static void parkUntil(Object blocker, long deadline) {
    }

    public static Object getBlocker(Thread t) {
        return null;
    }
}
