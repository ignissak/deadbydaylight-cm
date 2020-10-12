package net.ignissak.deadbydaylight.game.modules

import net.ignissak.deadbydaylight.DeadByDaylight
import net.ignissak.deadbydaylight.game.interfaces.GameRegion
import net.ignissak.deadbydaylight.utils.Log
import org.bukkit.Location
import org.bukkit.Material

class Gate(val region: GameRegion, private val material: Material) {

    var isOpened = false
    private val deletedBlocks: MutableList<Location> = mutableListOf()

    fun open() {
        if (isOpened) return

        Log.debug("Opening gate ${region.name}")
        isOpened = true

        val protectedRegion = this.region.getApplicableRegion(region.regionNames[0]) ?: return

        val blocks = mutableListOf<Location>()
        for (x in protectedRegion.minimumPoint.x .. protectedRegion.maximumPoint.x) {
            for (y in protectedRegion.minimumPoint.y .. protectedRegion.maximumPoint.y) {
                for (z in protectedRegion.minimumPoint.z .. protectedRegion.maximumPoint.z) {
                    blocks.add(Location(DeadByDaylight.gameManager.lobbyLocation!!.world,
                    x.toDouble(), y.toDouble(), z.toDouble(), 0F, 0F))
                }
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
                deletedBlock.block.setType(material, true)
            }
        }

        deletedBlocks.clear()
    }
}