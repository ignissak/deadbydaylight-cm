package net.ignissak.deadbydaylight.game.interfaces

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.bukkit.BukkitWorld
import net.ignissak.deadbydaylight.DeadByDaylight
import org.bukkit.Location

enum class GameRegion(val regionNames: Array<String>, val title: String, val isGate: Boolean) {

    ;
    // TODO

    fun getRegionAt(location: Location): GameRegion? {
        val regionManager = DeadByDaylight.regionContainer.get(BukkitAdapter.adapt(location.world))
        return null
    }

    private fun getRegionByRegionName(name: String): GameRegion? {
        values().forEach { if (it.regionNames.contains(name)) return it }
        return null
    }
}