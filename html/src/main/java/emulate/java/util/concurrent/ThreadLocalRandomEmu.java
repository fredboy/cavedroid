package emulate.java.util.concurrent;

import com.github.xpenatan.gdx.teavm.backends.web.gen.Emulate;
import java.util.concurrent.ThreadLocalRandom;

/**
 * TeaVM's java.util.concurrent.ThreadLocalRandom emulation has a constructor
 * that calls Random.<init>(seed), which itself dispatches to the overridden
 * setSeed — and TLR overrides setSeed to throw UnsupportedOperationException.
 * That makes TLR unusable: any first access (e.g. kotlin.random.Random.Default
 * via PlatformThreadLocalRandom) crashes the JVM-emulation in its &lt;clinit&gt;.
 *
 * Real JDK TLR's no-arg constructor never calls setSeed; the bogus dispatch
 * is purely a TeaVM emulation artifact. Replacing setSeed with a no-op
 * unbreaks the constructor chain; nothing in this app calls setSeed externally.
 */
@Emulate(value = ThreadLocalRandom.class, updateCode = true)
public class ThreadLocalRandomEmu {

    @Emulate
    public void setSeed(long seed) {
    }
}
