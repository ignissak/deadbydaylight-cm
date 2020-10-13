package net.ignissak.deadbydaylight.game.task

import net.ignissak.deadbydaylight.DeadByDaylight
import org.bukkit.scheduler.BukkitRunnable

class FrequentTryEndTask : BukkitRunnable() {

    override fun run() {
        DeadByDaylight.gameManager.tryEnd()
    }
}