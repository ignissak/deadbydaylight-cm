package net.ignissak.deadbydaylight.game.modules

import net.ignissak.deadbydaylight.game.ItemManager
import net.minecraft.server.v1_16_R2.BlockPosition
import net.minecraft.server.v1_16_R2.Blocks
import net.minecraft.server.v1_16_R2.PacketPlayOutBlockAction
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class LootChest(val location: Location) {

    val loot: MutableSet<ItemStack> = mutableSetOf()
    var opened: Boolean = false

    fun open() {
        location.world?.playSound(location, Sound.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, .5F, 1F)
        opened = true
        val pos = BlockPosition(this.location.block.x, this.location.block.y, this.location.block.z)

        for (itemStack in loot) {
            val dropItem = location.world?.dropItem(location.clone().add(.5, 1.0, .5), itemStack)
            dropItem?.velocity = Vector(.0, .3, .0)
            if (itemStack.isSimilar(ItemManager.fuel)) {
                dropItem?.customName = "§eBaterie"
            } else if (itemStack.isSimilar(ItemManager.bandage)) {
                dropItem?.customName = "§cBandáž"
            } else if (itemStack.isSimilar(ItemManager.flash)) {
                dropItem?.customName = "§9Zapalovač"
            }
            dropItem?.isCustomNameVisible = true
        }

        println("Opening chest at ${pos}.")

        val packet = PacketPlayOutBlockAction(pos, Blocks.CHEST, 1, 1)
        Bukkit.getOnlinePlayers().forEach { (it as CraftPlayer).handle.playerConnection.sendPacket(packet) }
    }

    fun close() {
        opened = false

        val pos = BlockPosition(this.location.block.x, this.location.block.y, this.location.block.z)
        val packet = PacketPlayOutBlockAction(pos, Blocks.CHEST, 1, 0)
        Bukkit.getOnlinePlayers().forEach { (it as CraftPlayer).handle.playerConnection.sendPacket(packet) }
    }

}