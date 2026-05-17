package ru.fredboy.cavedroid.common.api

import com.badlogic.gdx.ApplicationListener
import ru.fredboy.cavedroid.common.model.StartGameConfig

interface ApplicationController : ApplicationListener {

    fun quitGame()

    fun startGame(startGameConfig: StartGameConfig)

    fun exitGame()

    fun triggerResize()

    fun resumeGame()

    fun pauseGame()

    fun saveGame()

    fun showDeathScreen()

    fun respawnPlayer()
}
