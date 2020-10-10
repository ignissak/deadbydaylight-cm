package net.ignissak.deadbydaylight.game.task

import cz.craftmania.craftcore.spigot.utils.effects.ParticleEffect
import net.ignissak.deadbydaylight.DeadByDaylight
import net.ignissak.deadbydaylight.utils.Utils
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.scheduler.BukkitRunnable

class RunningGeneratorTask : BukkitRunnable() {

    override fun run() {
        if (DeadByDaylight.gameManager.generators.stream().noneMatch { it.isActivated() }) return

        // BUG: Particles are not in center of the block
        DeadByDaylight.gameManager.generators.filter { it.isActivated() }.forEach {
            it.location.world?.playSound(it.location, Sound.ENTITY_MINECART_RIDING, SoundCategory.BLOCKS, .5F, .0F)
            ParticleEffect.SMOKE_NORMAL.display(.6F, .6F, .6F, 0F, 25, Utils.getCenter(it.location), 16.0)
        }
    }

}