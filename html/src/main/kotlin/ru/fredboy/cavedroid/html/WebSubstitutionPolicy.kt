package ru.fredboy.cavedroid.html

import org.teavm.extension.spi.substitution.SubstitutionPolicy
import org.teavm.extension.spi.substitution.SubstitutionSink

/** Registers the browser implementations of JDK concurrency classes and Box2D. */
class WebSubstitutionPolicy : SubstitutionPolicy {
    override fun contribute(sink: SubstitutionSink) {
        sink.substitutePackage("java", "emu.java")
        sink.substitutePackage("com.badlogic.gdx.physics.box2d", "emu.com.badlogic.gdx.physics.box2d")
        sink.substitutePackage("org.jbox2d", "emu.org.jbox2d")
    }
}
