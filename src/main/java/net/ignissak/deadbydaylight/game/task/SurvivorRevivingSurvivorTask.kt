package net.ignissak.deadbydaylight.game.task

import cz.craftmania.craftcore.spigot.messages.Title
import net.ignissak.deadbydaylight.game.modules.Survivor
import net.ignissak.deadbydaylight.utils.remainingTo
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.scheduler.BukkitRunnable

class SurvivorRevivingSurvivorTask(private val survivorReviving: Survivor, val survivorToBeRevived: Survivor): BukkitRunnable() {

    var remainingHalfSeconds = 8

    override fun run() {
        if (!survivorReviving.player.isSneaking) {
            Title("§c§lPŘERUŠENO", "", 0, 10, 5).send(survivorReviving.player)

            survivorReviving.player.playSound(survivorReviving.player.location, Sound.ENTITY_ITEM_BREAK, SoundCategory.AMBIENT, .5F, 1F)
            this.cancel()
            return
        }

        if (remainingHalfSeconds == 1) {
            Title("§a§lOŽIVEN", "§fOživil jsi §7" + survivorToBeRevived.player.name, 5, 20, 5).send(survivorReviving.player)
            survivorReviving.player.playSound(survivorReviving.player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.AMBIENT, .5F, 1F)
            survivorToBeRevived.revive(survivorReviving)

            survivorReviving.gameStats.survivor_players_revived += 1
            survivorReviving.revivedPlayers += 1

            survivorReviving.coins += 3
            survivorReviving.player.sendMessage("§e+3CC §8[Oživení spoluhráče]")
            this.cancel()
            return
        }

        Title("§e§lOŽIVOVÁNÍ", getDiagram(), 0, 25, 0).send(survivorReviving.player)

        remainingHalfSeconds -= 1
    }

    private fun getDiagram(): String = "§a\u25A0".repeat(remainingHalfSeconds.remainingTo(8)) + "§7\u25A0".repeat(remainingHalfSeconds)
}