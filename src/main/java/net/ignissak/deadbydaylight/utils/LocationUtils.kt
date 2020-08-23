package net.ignissak.deadbydaylight.utils

import net.ignissak.deadbydaylight.api.exception.CouldNotParseLocation
import org.bukkit.Bukkit
import org.bukkit.Location
import java.lang.Exception

class LocationUtils {

    companion object {

        @Throws(CouldNotParseLocation::class)
        fun parseLocation(unparsed: String?, useYawPitch: Boolean = false): Location? {
            if (unparsed == null) return null
            try {
                val loc = unparsed.split(";").toTypedArray()
                val world = loc[0]
                val x = loc[1].toDouble()
                val y = loc[2].toDouble()
                val z = loc[3].toDouble()
                val yaw = if (useYawPitch) loc[4].toFloat() else 0f
                val pitch = if (useYawPitch) loc[5].toFloat() else 0f
                return Location(Bukkit.getWorld(world), x, y, z, yaw, pitch)
            } catch (e: Exception) {
                throw CouldNotParseLocation("Error while parsing '$unparsed'.")
            }
        }

        fun formatLocation(location: Location, useYawPitch: Boolean = true): String {
            if (useYawPitch) return "${location.world?.name};${location.x};${location.y};${location.z};${location.yaw};${location.pitch}"
            return "${location.world?.name};${location.x};${location.y};${location.z}"
        }
    }

}