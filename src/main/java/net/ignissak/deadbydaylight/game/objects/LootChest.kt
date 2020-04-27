package net.ignissak.deadbydaylight.game.objects

import org.bukkit.Location
import org.bukkit.block.Chest
import org.bukkit.inventory.ItemStack

class LootChest(val location: Location) {
    val chest: Chest
    private val loot: List<ItemStack>? = null

    init {
        chest = location.world!!.getBlockAt(location) as Chest
    }
}