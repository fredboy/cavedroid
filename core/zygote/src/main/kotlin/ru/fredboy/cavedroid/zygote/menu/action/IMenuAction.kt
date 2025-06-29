package ru.fredboy.cavedroid.zygote.menu.action

interface IMenuAction {

    fun perform()

    fun canPerform(): Boolean = true

}