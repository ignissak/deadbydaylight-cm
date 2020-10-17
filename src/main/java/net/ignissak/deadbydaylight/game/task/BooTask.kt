package net.ignissak.deadbydaylight.game.task

import net.ignissak.deadbydaylight.game.PlayerManager
import net.ignissak.deadbydaylight.utils.getPlayer
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

class BooTask : BukkitRunnable() {

    override fun run() {
        val random = Random()
        val bol = random.nextBoolean()

        if (bol) {
            PlayerManager.killerTeam.entries.forEach {
                it.getPlayer()?.location?.let { it1 -> it.getPlayer()?.playSound(it1, Sound.ENTITY_RAVAGER_STUNNED, SoundCategory.HOSTILE, 1F, 0F) }
            }
        } else {
            PlayerManager.players.values.forEach {
                it.player.playSound(it.player.location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.AMBIENT, 1F, 0F)
            }
        }
    }
}