package net.ignissak.deadbydaylight.event

import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.LeavesDecayEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.server.TabCompleteEvent
import org.bukkit.event.weather.WeatherChangeEvent

class WorldListener : Listener {

    @EventHandler
    fun onWeatherChange(event: WeatherChangeEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        if (event.player.isOp && event.player.gameMode == GameMode.CREATIVE) return
        event.isCancelled = true
    }

    @EventHandler
    fun onPlace(event: BlockPlaceEvent) {
        if (event.player.isOp && event.player.gameMode == GameMode.CREATIVE) return
        event.isCancelled = true
    }

    @EventHandler
    fun onLeaveDecay(event: LeavesDecayEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        if (!event.player.isOp) event.isCancelled = true
    }

    @EventHandler
    fun onTabComplete(event: TabCompleteEvent) {
        if (!event.sender.isOp) event.completions = mutableListOf()
    }
}