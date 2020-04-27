package net.ignissak.deadbydaylight.game.objects

import net.ignissak.deadbydaylight.utils.LocationUtils.parseLocation
import org.bukkit.Location

class Gate(val id: String, val arena: Arena) {

    //todo remove
    val leftUpperCorner: Location
    val rightDownerCorner: Location

    init {
        leftUpperCorner = parseLocation(arena.arenaFile.getString("locations.gate.$id.left_upper_corner"), false)
        rightDownerCorner = parseLocation(arena.arenaFile.getString("locations.gate.$id.right_downer_corner"), false)
    }
}