package net.ignissak.deadbydaylight.game.interfaces

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.bukkit.BukkitWorld
import net.ignissak.deadbydaylight.DeadByDaylight
import org.bukkit.Location

enum class GameRegion(val regionNames: Array<String>, val title: String) {

    BAMBOO_FARM(arrayOf("bamboo"), "Bambusová farma"),
    DEEP_SPRUCE_FOREST(arrayOf("deepspruceforest"), "Hustý smrekový les"),
    DOOR(arrayOf("door1", "door2"), "Východ"),
    EAST_GATE(arrayOf("eastgate"), "Východní brána"),
    EAST_PATH(arrayOf("eastpath"), "Východní stezka"),
    GARDENS(arrayOf("gardens"), "Zahrady"),
    HOUSE_WITH_FARM(arrayOf("house1", "house2"), "Chalupa"),
    NORTH_DOOR(arrayOf("northdoor"), "Severní brána"),
    NORTH_RIVER(arrayOf("northriver"), "Severní řeka")
    ;

    companion object {

        private fun getRegionByRegionName(name: String?): GameRegion? {
            if (name == null) return null
            values().forEach { if (it.regionNames.contains(name)) return it }
            return null
        }

        @JvmStatic
        fun getRegionAt(location: Location): GameRegion? {
            val regionManager = DeadByDaylight.regionContainer.get(BukkitAdapter.adapt(location.world))
            return getRegionByRegionName(regionManager!!.regions.get(0)!!.id)
        }

    }


}