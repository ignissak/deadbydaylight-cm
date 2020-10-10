package net.ignissak.deadbydaylight.game.modules

import me.libraryaddict.disguise.DisguiseAPI
import me.libraryaddict.disguise.disguisetypes.DisguiseType
import me.libraryaddict.disguise.disguisetypes.MobDisguise
import net.ignissak.deadbydaylight.game.ItemManager
import net.ignissak.deadbydaylight.game.interfaces.GamePlayer
import org.bukkit.Bukkit
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

    fun disguise() {
        val mobDisguise = MobDisguise(DisguiseType.IRON_GOLEM)

        DisguiseAPI.disguiseToPlayers(this.player, mobDisguise, Bukkit.getOnlinePlayers())
    }

}