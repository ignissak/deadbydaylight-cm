package net.ignissak.deadbydaylight.utils

import net.citizensnpcs.api.npc.NPC
import net.ignissak.deadbydaylight.DeadByDaylight
import net.ignissak.deadbydaylight.game.interfaces.GamePlayer
import net.ignissak.deadbydaylight.game.modules.Killer
import net.ignissak.deadbydaylight.game.modules.Survivor
import net.minecraft.server.v1_16_R2.NBTTagCompound
import net.minecraft.server.v1_16_R2.NBTTagList
import org.bukkit.*
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.scoreboard.Team
import java.util.*

class Utils {

    companion object {
        fun createHead(name: String?, uuid: String?, textureData: String?): ItemStack? {
            try {
                val sHead = CraftItemStack.asNMSCopy(ItemStack(Material.PLAYER_HEAD, 1))
                val tag = NBTTagCompound()
                val skullOwnerTag = NBTTagCompound()
                val displayTag = NBTTagCompound()
                val propertiesTag = NBTTagCompound()
                val tagList = NBTTagList()
                val valueTag = NBTTagCompound()
                valueTag.setString("Value", textureData)
                tagList.add(valueTag)
                propertiesTag["textures"] = tagList
                skullOwnerTag.setString("Id", uuid)
                skullOwnerTag.setString("Name", name)
                skullOwnerTag["Properties"] = propertiesTag
                displayTag.setString("Name", name)
                tag["SkullOwner"] = skullOwnerTag
                tag["display"] = displayTag
                sHead.tag = tag
                return CraftItemStack.asBukkitCopy(sHead)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        fun createHead(name: String?, uuid: String?, textureData: String?, displayName: String?, vararg lore: String): ItemStack? {
            try {
                val sHead = CraftItemStack.asNMSCopy(ItemStack(Material.PLAYER_HEAD, 1))
                val tag = NBTTagCompound()
                val skullOwnerTag = NBTTagCompound()
                val displayTag = NBTTagCompound()
                val propertiesTag = NBTTagCompound()
                val tagList = NBTTagList()
                val valueTag = NBTTagCompound()
                valueTag.setString("Value", textureData)
                tagList.add(valueTag)
                propertiesTag["textures"] = tagList
                skullOwnerTag.setString("Id", uuid)
                skullOwnerTag.setString("Name", name)
                skullOwnerTag["Properties"] = propertiesTag
                displayTag.setString("Name", name)
                tag["SkullOwner"] = skullOwnerTag
                tag["display"] = displayTag
                sHead.tag = tag
                val item = CraftItemStack.asBukkitCopy(sHead)
                val itemMeta = item.itemMeta
                itemMeta!!.setDisplayName(displayName)
                val finalLore: MutableList<String> = ArrayList()
                for (s in lore) finalLore.add(s)
                itemMeta.lore = finalLore
                itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                item.itemMeta = itemMeta
                return item
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        @JvmStatic
        fun sendSoundGlobally(sound: Sound, volume: Float, pitch: Float) {
            Bukkit.getOnlinePlayers().forEach { it.playSound(it.location, sound, volume, pitch) }
        }

        @JvmStatic
        fun broadcast(withPrefix: Boolean = true, message: String) {
            Bukkit.getOnlinePlayers().forEach { it.sendMessage(ChatColor.translateAlternateColorCodes('&', if (withPrefix) "&c&lHALLOWEEN &8| &7$message" else "&7$message")) }
            println(ChatColor.translateAlternateColorCodes('&', if (withPrefix) "&c&lHALLOWEEN &8| &7$message" else "&7$message"))
        }

        @JvmStatic
        fun broadcastTip(message: String) {
            Bukkit.getOnlinePlayers().forEach { it.sendMessage("§c§lTIP §8| §7$message") }
        }

        @JvmStatic
        fun getCenter(loc: Location): Location? {
            return loc.clone().add(.5, .5, .5)
        }
    }
}

fun String?.getPlayer(): Player? {
    return this?.let { Bukkit.getPlayer(it) }
}

fun String?.getGamePlayer(): GamePlayer? {
    return this?.let { DeadByDaylight.playerManager.getGamePlayer(it) }
}

fun String?.getSurvivor(): Survivor? {
    return this?.let { getGamePlayer() as Survivor }
}

fun String?.getKiller(): Killer? {
    return this?.let { getGamePlayer() as Killer }
}

fun Player?.getGamePlayer(): GamePlayer? {
    return this?.player?.let { DeadByDaylight.playerManager.getGamePlayer(it) }
}

fun Team.isFull(): Boolean = this.name == "killer" && this.entries.size == 1 || this.name == "survivors" && this.entries.size == 4

fun Int.remainingTo(int: Int): Int {
    if (int < this) return 0
    return int - this
}

fun NPC.hide() {
    this.teleport(DeadByDaylight.gameManager.dumpLocation, PlayerTeleportEvent.TeleportCause.PLUGIN)
    Bukkit.getOnlinePlayers().forEach { DeadByDaylight.instance.let { it1 -> it.hidePlayer(it1, this.entity as Player) } }
}

fun NPC.show(location: Location) {
    this.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN)
    Bukkit.getOnlinePlayers().forEach { DeadByDaylight.instance.let { it1 -> it.showPlayer(it1, this.entity as Player) } }
}