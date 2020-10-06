package net.ignissak.deadbydaylight.game.task

import cz.craftmania.craftcore.spigot.messages.ActionBar
import net.ignissak.deadbydaylight.game.interfaces.GameRegion
import net.ignissak.deadbydaylight.game.interfaces.SurvivalState
import net.ignissak.deadbydaylight.game.modules.Survivor
import net.ignissak.deadbydaylight.utils.getGamePlayer
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

class LocationTask : BukkitRunnable() {

    override fun run() {
        Bukkit.getOnlinePlayers().forEach {
            val gamePlayer = it.getGamePlayer() ?: return@forEach

            if (gamePlayer is Survivor && gamePlayer.survivalState != SurvivalState.PLAYING) return@forEach

            val region = GameRegion.getRegionAt(it.location) ?: return@forEach

            ActionBar(region.title, 100).send(it)
        }
    }

}