package net.ignissak.deadbydaylight.game.task

import cz.craftmania.craftcore.spigot.messages.Title
import cz.craftmania.craftcore.spigot.utils.effects.ParticleEffect
import net.ignissak.deadbydaylight.DeadByDaylight
import net.ignissak.deadbydaylight.game.PlayerManager
import net.ignissak.deadbydaylight.game.interfaces.SurvivalState
import net.ignissak.deadbydaylight.game.modules.Survivor
import net.ignissak.deadbydaylight.utils.getKiller
import org.bukkit.Color
import org.bukkit.scheduler.BukkitRunnable

class SurvivorDyingTask(private val survivor: Survivor) : BukkitRunnable() {

    private var remainingTime: Int = 30

    override fun run() {
        if (survivor.survivalState != SurvivalState.DYING) {
            this.cancel()
            return
        }

        if (!DeadByDaylight.playerManager.isAnySurvivorAlive()) {
            remainingTime = 1
        }

        if (PlayerManager.killerTeam.size == 0) {
            this.cancel()
            return
        }

        ParticleEffect.REDSTONE.display(null, survivor.npc.entity.location.clone().add(.0, 1.5, .0), Color.RED, 16.0, .1F, .1F, .1F, .3F, 10)
        Title("§c§lUMÍRÁŠ!", "§fOstatní tě musí zachránit.", 0, 40, 0).send(survivor.player)

        survivor.player.level = remainingTime
        survivor.player.isFlying = true

        if (remainingTime == 1) {
            PlayerManager.killerTeam.entries.first().getKiller()?.let { survivor.die(it) }
            this.cancel()
            return
        }

        if (!survivor.isBeingRevived())
            remainingTime -= 1
    }
}