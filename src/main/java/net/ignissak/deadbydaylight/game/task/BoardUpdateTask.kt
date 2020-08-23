package net.ignissak.deadbydaylight.game.task

import net.ignissak.deadbydaylight.DeadByDaylight
import org.bukkit.scheduler.BukkitRunnable

class BoardUpdateTask : BukkitRunnable() {

    override fun run() {
        DeadByDaylight.boardManager.updateAllPlayers()
    }

}