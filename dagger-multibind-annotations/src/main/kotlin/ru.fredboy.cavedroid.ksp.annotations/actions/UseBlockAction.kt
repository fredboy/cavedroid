package ru.fredboy.cavedroid.ksp.annotations.actions

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class UseBlockAction(val blockKey: String)
