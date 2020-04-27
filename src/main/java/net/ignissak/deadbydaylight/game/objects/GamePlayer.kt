package net.ignissak.deadbydaylight.game.objects

import org.bukkit.entity.Player

abstract class GamePlayer {
    private val player: Player? = null
    abstract fun giveStartingItems()
    abstract fun giveStartingPotionEffects()
}