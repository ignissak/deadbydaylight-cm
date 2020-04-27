package net.ignissak.deadbydaylight.utils

import org.bukkit.Bukkit
import org.bukkit.Location

object LocationUtils {
    @JvmStatic
    fun parseLocation(unparsed: String, useYawPitch: Boolean): Location {
        val loc = unparsed.split(";").toTypedArray()
        val world = loc[0]
        val x = loc[1].toDouble()
        val y = loc[2].toDouble()
        val z = loc[3].toDouble()
        val yaw = if (useYawPitch) loc[4].toFloat() else 0f
        val pitch = if (useYawPitch) loc[5].toFloat() else 0f
        return Location(Bukkit.getWorld(world), x, y, z, yaw, pitch)
    }
}