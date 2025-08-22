package ru.fredboy.cavedroid.common.api

import ru.fredboy.cavedroid.common.model.StartGameConfig

interface ApplicationController {

    fun quitGame()

    fun startGame(startGameConfig: StartGameConfig)

    fun exitGame()

    fun triggerResize()
}
