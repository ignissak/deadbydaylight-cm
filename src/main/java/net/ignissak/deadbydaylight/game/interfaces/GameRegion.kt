package net.ignissak.deadbydaylight.game.interfaces

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldguard.protection.ApplicableRegionSet
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import net.ignissak.deadbydaylight.DeadByDaylight
import org.bukkit.Location

enum class GameRegion(val regionNames: Array<String>, val title: String) {

    BAMBOO_FARM(arrayOf("bamboo"), "Bambusová farma"),
    DEEP_SPRUCE_FOREST(arrayOf("deepspruceforest"), "Hustý smrkový les"),
    ESCAPE_1(arrayOf("door1"), "Východ"),
    ESCAPE_2(arrayOf("door2"), "Východ"),
    IRON_BARS_1(arrayOf("ironbars1"), "Východ"),
    IRON_BARS_2(arrayOf("ironbars2"), "Východ"),
    EAST_GATE(arrayOf("eastgate"), "Východní brána"),
    EAST_PATH(arrayOf("eastpath"), "Východní stezka"),
    GARDENS(arrayOf("gardens"), "Záhrady"),
    HOUSE_WITH_FARM(arrayOf("house1", "house2"), "Chalupa"),
    NORTH_DOOR(arrayOf("northdoor"), "Severní brána"),
    NORTH_RIVER(arrayOf("northriver"), "Severní řeka")
    ;


    fun getApplicableRegion(name: String): ProtectedCuboidRegion? = DeadByDaylight.regionContainer.get(BukkitAdapter.adapt(DeadByDaylight.gameManager.lobbyLocation!!.world!!))!!.getRegion(name) as ProtectedCuboidRegion

    companion object {

        @JvmStatic
        fun getRegionByRegionName(name: String?): GameRegion? {
            if (name == null) return null
            values().forEach { if (it.regionNames.contains(name)) return it }
            return null
        }

        @JvmStatic
        fun getRegionAt(location: Location): GameRegion? {
            val regionManager = DeadByDaylight.regionContainer.get(BukkitAdapter.adapt(location.world))
            val regions = regionManager!!.getApplicableRegions(BukkitAdapter.asBlockVector(location)).regions
            if (regions.size == 0) return null
            return getRegionByRegionName(regions.first().id)
        }

    }


}