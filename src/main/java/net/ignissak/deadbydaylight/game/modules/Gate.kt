package net.ignissak.deadbydaylight.game.modules

import net.ignissak.deadbydaylight.DeadByDaylight
import net.ignissak.deadbydaylight.game.interfaces.GameRegion
import net.ignissak.deadbydaylight.utils.Log
import org.bukkit.Location
import org.bukkit.Material

class Gate(private val region: GameRegion, private val material: Material) {

    var isOpened = false
    private val deletedBlocks: MutableList<Location> = mutableListOf()

    fun open() {
        if (isOpened) return

        Log.debug("Opening gate ${region.name}")
        isOpened = true

        val protectedRegion = this.region.getApplicableRegion(region.regionNames[0]) ?: return

        val points = protectedRegion.points
        val blocks = mutableListOf<Location>()
        for (point in points) {
            for (y in protectedRegion.minimumPoint.blockY..protectedRegion.maximumPoint.blockY) {
                blocks.add(Location(DeadByDaylight.gameManager.lobbyLocation!!.world,
                        point.blockX.toDouble(), y.toDouble(), point.blockZ.toDouble()))
            }
        }

        for (loc in blocks) {
            if (loc.block.type == material) {
                deletedBlocks.add(loc.clone())

                loc.block.setType(Material.AIR, false)
            }
        }

        // TODO
    }

    fun close() {
        if (!isOpened) return

        Log.debug("Closing gate ${region.name}")
        isOpened = false

        for (deletedBlock in deletedBlocks) {
            if (deletedBlock.block.type == Material.AIR) {
                deletedBlock.block.setType(material, false)
            }
        }

        deletedBlocks.clear()
    }
}