package net.ignissak.deadbydaylight.game.task

import cz.craftmania.craftcore.spigot.messages.Title
import cz.craftmania.craftcore.spigot.utils.effects.ParticleEffect
import net.ignissak.deadbydaylight.game.PlayerManager
import net.ignissak.deadbydaylight.game.interfaces.SurvivalState
import net.ignissak.deadbydaylight.game.modules.Survivor
import net.ignissak.deadbydaylight.utils.getKiller
import org.bukkit.Color
import org.bukkit.scheduler.BukkitRunnable

class SurvivorDyingTask(private val survivor: Survivor) : BukkitRunnable() {

    private var remainingTime: Int = 20

    override fun run() {
        if (survivor.survivalState != SurvivalState.DYING) this.cancel()
        /*if (!DeadByDaylight.playerManager.isAnySurvivorAlive()) {
            survivor.die()
            this.cancel()
            return
        }*/
        // TEST: Particles
        // public void display(ParticleEffect.ParticleData data, Location center, Color color, double range, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
        ParticleEffect.REDSTONE.display(null, survivor.npc.entity.location.clone().add(.0, 1.5, .0), Color.RED, 16.0, .1F, .1F, .1F, .3F, 10)
        //ParticleEffect.BLOCK_CRACK.display(ParticleEffect.BlockData(Material.REDSTONE_BLOCK, 0), .25F, .25F, .25F, .5F, 50, survivor.npc.entity.location.clone().add(.0, -.5, .0), ArrayList<Player>(Bukkit.getOnlinePlayers()))
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