package net.ignissak.deadbydaylight.game.task

import cz.craftmania.craftcore.spigot.utils.effects.ParticleEffect
import net.ignissak.deadbydaylight.DeadByDaylight
import net.ignissak.deadbydaylight.api.inteface.StoppableRunnable
import net.ignissak.deadbydaylight.game.PlayerManager
import net.ignissak.deadbydaylight.game.modules.Generator
import net.ignissak.deadbydaylight.utils.Utils
import net.ignissak.deadbydaylight.utils.getPlayer
import net.minecraft.server.v1_16_R2.EntityShulker
import net.minecraft.server.v1_16_R2.EntityTypes
import net.minecraft.server.v1_16_R2.PacketPlayOutEntityDestroy
import net.minecraft.server.v1_16_R2.PacketPlayOutSpawnEntityLiving
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.craftbukkit.v1_16_R2.CraftWorld
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer
import org.bukkit.scheduler.BukkitRunnable

class RunningGeneratorTask : BukkitRunnable(), StoppableRunnable {

    var entities: MutableMap<Generator, EntityShulker> = mutableMapOf()

    override fun run() {
        if (DeadByDaylight.gameManager.generators.stream().noneMatch { it.isActivated() }) return

        // TEST
        DeadByDaylight.gameManager.generators.filter { it.isActivated() }.forEach {
            if (!entities.containsKey(it)) {
                // Shows glow for killer
                val connection = (PlayerManager.killerTeam.entries.first().getPlayer() as CraftPlayer).handle.playerConnection

                val shulker = EntityShulker(EntityTypes.SHULKER, (it.location.world!! as CraftWorld).handle)
                shulker.setLocation(it.location.x, it.location.y, it.location.z, 0F, 0F)
                shulker.setFlag(6, true)
                shulker.setFlag(5, true)

                val packet = PacketPlayOutSpawnEntityLiving(shulker)
                connection.sendPacket(packet)

                entities[it] = shulker
            }
            it.location.world?.playSound(it.location, Sound.ENTITY_MINECART_RIDING, SoundCategory.BLOCKS, .5F, .0F)
            ParticleEffect.SMOKE_NORMAL.display(.6F, .6F, .6F, 0F, 25, Utils.getCenter(it.location), 16.0)
        }
    }

    override fun stop() {
        // Removes all shulkers
        entities.values.forEach {
            Bukkit.getOnlinePlayers().forEach{ player ->
                val connection = (player as CraftPlayer).handle.playerConnection

                val packet = PacketPlayOutEntityDestroy(it.id)
                connection.sendPacket(packet)
            }
        }

        this.cancel()
    }
}