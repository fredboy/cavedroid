package emu.java.util.concurrent.locks;

public class ReentrantReadWriteLock {

    public static class ReadLock {
        public void lock() {
        }

        public void unlock() {
        }

        public boolean tryLock() {
            return true;
        }

        public void lockInterruptibly() {
        }
    }

    public static class WriteLock {
        public void lock() {
        }

        public void unlock() {
        }

        public boolean tryLock() {
            return true;
        }

        public void lockInterruptibly() {
        }

        public boolean isHeldByCurrentThread() {
            return true;
        }

        public int getHoldCount() {
            return 1;
        }
    }

    private final ReadLock readLock = new ReadLock();
    private final WriteLock writeLock = new WriteLock();

    public ReentrantReadWriteLock() {
    }

    public ReentrantReadWriteLock(boolean fair) {
    }

    public ReadLock readLock() {
        return readLock;
    }

    public WriteLock writeLock() {
        return writeLock;
    }

    public int getReadHoldCount() {
        return 0;
    }

    public int getWriteHoldCount() {
        return 0;
    }
}
