package net.ignissak.deadbydaylight.event

import cz.craftmania.craftcore.spigot.events.worldguard.RegionEnterEvent
import net.ignissak.deadbydaylight.DeadByDaylight
import net.ignissak.deadbydaylight.game.interfaces.GameRegion
import net.ignissak.deadbydaylight.game.interfaces.SurvivalState
import net.ignissak.deadbydaylight.game.modules.Survivor
import net.ignissak.deadbydaylight.utils.getGamePlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class RegionListener : Listener {

    @EventHandler
    fun onRegionEnter(event: RegionEnterEvent) {
        val id = event.region.id
        val gameRegion = GameRegion.getRegionByRegionName(id) ?: return
        val gamePlayer = event.player.getGamePlayer() ?: return

        if (gameRegion == GameRegion.ESCAPE_1 || gameRegion == GameRegion.ESCAPE_2) {
            if (gamePlayer !is Survivor) {
                // Killer cannot access this region
                event.isCancelled = true
                return
            }

            if (DeadByDaylight.gameManager.gates.none { it.isOpened })
                return

            if (gamePlayer.survivalState == SurvivalState.PLAYING) {
                gamePlayer.win()
            }
        }
    }
}