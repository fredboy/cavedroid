package emulate.java.lang;

import com.github.xpenatan.gdx.teavm.backends.web.gen.Emulate;

/**
 * Additive emulation: adds methods to TeaVM's existing java.lang.Thread stub
 * that kotlinx-coroutines reaches into. The browser is single-threaded so all
 * thread-local / context-classloader machinery is a no-op.
 */
@Emulate(value = Thread.class, updateCode = true)
public class ThreadEmu {

    @Emulate
    public ClassLoader getContextClassLoader() {
        return null;
    }

    @Emulate
    public void setContextClassLoader(ClassLoader cl) {
    }
}
