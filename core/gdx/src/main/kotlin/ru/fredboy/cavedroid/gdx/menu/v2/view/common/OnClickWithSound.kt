package ru.fredboy.cavedroid.gdx.menu.v2.view.common

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import ktx.actors.onClick

fun <T : Actor> T.onClickWithSound(viewModel: BaseViewModel, listener: T.() -> Unit): ClickListener {
    return onClick {
        viewModel.playClickSound()
        listener()
    }
}
