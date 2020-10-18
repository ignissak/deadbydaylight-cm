package net.ignissak.deadbydaylight.game.task

import net.ignissak.deadbydaylight.DeadByDaylight
import net.ignissak.deadbydaylight.game.PlayerManager
import net.ignissak.deadbydaylight.game.interfaces.GameState
import net.ignissak.deadbydaylight.utils.getSurvivor
import org.bukkit.scheduler.BukkitRunnable

class FrequentTryEndTask : BukkitRunnable() {

    override fun run() {
        if (DeadByDaylight.gameManager.gates.all { it.isOpened } || DeadByDaylight.gameManager.gameState == GameState.ENDING) {
            PlayerManager.survivorTeam.entries.forEach { it.getSurvivor()?.removeBlindness() }
        }
        DeadByDaylight.gameManager.tryEnd()
    }
}