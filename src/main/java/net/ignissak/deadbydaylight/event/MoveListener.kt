package net.ignissak.deadbydaylight.event

import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class MoveListener : Listener {

    private val soundMap: MutableMap<Player, Long> = mutableMapOf()

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        if (event.from.block == event.to?.block) return
        val player = event.player

        if (!soundMap.containsKey(player)) {
            soundMap[player] = System.currentTimeMillis() + 2500
            return
        }

        if (soundMap[player]!! > System.currentTimeMillis()) {
            // Dupity dup
            soundMap[player] = System.currentTimeMillis() + 2500

            player.world.playSound(player.location, Sound.ENTITY_IRON_GOLEM_STEP, SoundCategory.HOSTILE, 1F, 0F)
        }
    }
}