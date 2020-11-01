package net.ignissak.deadbydaylight.game.task

import cz.craftmania.craftcore.spigot.messages.BossBar
import net.ignissak.deadbydaylight.DeadByDaylight
import net.ignissak.deadbydaylight.game.interfaces.GameState
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

class BossBarTask : BukkitRunnable() {

    override fun run() {
        if (DeadByDaylight.gameManager.areGatesOpened() && DeadByDaylight.gameManager.gameState == GameState.INGAME) {
            if (DeadByDaylight.gameManager.bossBar == null) {
                DeadByDaylight.gameManager.bossBar = BossBar("§fBrány se uzavřou za §c${DeadByDaylight.gameManager.getGameTimeFormatted()}", "RED", "SOLID", 1.0)
                Bukkit.getOnlinePlayers().forEach { DeadByDaylight.gameManager.bossBar?.addPlayer(it) }
                DeadByDaylight.gameManager.bossBar?.setVisible(true)
            }

            val now = System.currentTimeMillis()
            val ends = DeadByDaylight.gameManager.endsAt
            val opened = DeadByDaylight.gameManager.gatesOpenedAt
            val remaining = ends - now
            val diff = ends - opened

            // Remaining sú 2500 (2.5s)
            // Otvárajú as po 8000 (8s)
            if (remaining <= 0)
                DeadByDaylight.gameManager.bossBar?.hide()
            else
                DeadByDaylight.gameManager.bossBar?.setTitle("§fBrány se uzavřou za §c${DeadByDaylight.gameManager.getGameTimeFormatted()}")
                DeadByDaylight.gameManager.bossBar?.bossBar?.progress = remaining.toDouble() / diff.toDouble()
        } else {
            DeadByDaylight.gameManager.bossBar?.hide()
            DeadByDaylight.gameManager.bossBar = null
            this.cancel()
        }
    }
}