package ru.fredboy.cavedroid.gdx.menu.action

interface IMenuAction {

    fun perform()

    fun canPerform(): Boolean = true
}
