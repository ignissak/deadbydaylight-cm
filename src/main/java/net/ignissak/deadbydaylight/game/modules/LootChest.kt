package net.ignissak.deadbydaylight.game.modules

import net.ignissak.deadbydaylight.DeadByDaylight
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
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector

class LootChest(val location: Location) {

    val loot: MutableSet<ItemStack> = mutableSetOf()
    var opened: Boolean = false
    var bukkitTask: BukkitTask? = null

    fun open() {
        location.world?.playSound(location, Sound.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, .5F, 1F)
        opened = true
        val pos = BlockPosition(this.location.block.x, this.location.block.y, this.location.block.z)

        for (itemStack in loot) {
            val dropItem = location.world?.dropItem(location.clone().add(.5, 1.0, .5), itemStack)
            dropItem?.velocity = Vector(.0, .3, .0)
            if (itemStack.isSimilar(ItemManager.battery)) {
                dropItem?.customName = "§eBaterie"
            } else if (itemStack.isSimilar(ItemManager.bandage)) {
                dropItem?.customName = "§cBandáž"
            } else if (itemStack.isSimilar(ItemManager.flash)) {
                dropItem?.customName = "§9Zapalovač"
            }
            dropItem?.isCustomNameVisible = true
        }

        println("Opening chest at $pos. Will recover in 5 minutes.")

        val packet = PacketPlayOutBlockAction(pos, Blocks.CHEST, 1, 1)
        Bukkit.getOnlinePlayers().forEach { (it as CraftPlayer).handle.playerConnection.sendPacket(packet) }

        bukkitTask = object : BukkitRunnable() {
            override fun run() {
                this@LootChest.close()
                this@LootChest.loot.add(ItemManager.battery)
                this@LootChest.location.world?.playSound(location, Sound.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, .5F, 1F)

                println("Recovered chest at $pos.")
            }
        }.runTaskLater(DeadByDaylight.instance, 5 * 60 * 20)
    }

    fun close() {
        opened = false

        val pos = BlockPosition(this.location.block.x, this.location.block.y, this.location.block.z)
        val packet = PacketPlayOutBlockAction(pos, Blocks.CHEST, 1, 0)
        Bukkit.getOnlinePlayers().forEach { (it as CraftPlayer).handle.playerConnection.sendPacket(packet) }

        try {
            bukkitTask?.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}