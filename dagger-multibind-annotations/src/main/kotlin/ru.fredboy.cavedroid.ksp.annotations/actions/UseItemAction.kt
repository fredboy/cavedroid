package ru.fredboy.cavedroid.ksp.annotations.actions

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class UseItemAction(val actionKey: String)
