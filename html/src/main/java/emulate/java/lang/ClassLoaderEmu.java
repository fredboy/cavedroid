package emulate.java.lang;

import com.github.xpenatan.gdx.teavm.backends.web.gen.Emulate;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Additive emulation: adds methods to TeaVM's existing java.lang.ClassLoader
 * stub. kotlinx-coroutines reaches into these during ServiceLoader-style
 * initialization paths that never actually fire in the browser.
 */
@Emulate(value = ClassLoader.class, updateCode = true)
public class ClassLoaderEmu {

    @Emulate
    public Enumeration<java.net.URL> getResources(String name) {
        return Collections.emptyEnumeration();
    }

    @Emulate
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        throw new ClassNotFoundException(name);
    }
}
