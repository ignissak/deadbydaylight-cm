package net.ignissak.deadbydaylight.game.modules

import net.ignissak.deadbydaylight.game.ItemManager
import net.ignissak.deadbydaylight.game.interfaces.GamePlayer
import org.bukkit.entity.Player

class Killer(player: Player) : GamePlayer(player) {

    var playerHits: Int = 0
    var playerDowns: Int = 0
    var playerKills: Int = 0

    override fun giveStartingItems() {
        player.inventory.clear()
        player.inventory.setItem(0, ItemManager.axe)
        player.inventory.setItem(1, ItemManager.hook)
    }

    override fun giveStartingPotionEffects() {}

}