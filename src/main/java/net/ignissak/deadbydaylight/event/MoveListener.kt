package net.ignissak.deadbydaylight.event

import net.ignissak.deadbydaylight.DeadByDaylight
import net.ignissak.deadbydaylight.game.interfaces.GameState
import net.ignissak.deadbydaylight.game.modules.Killer
import net.ignissak.deadbydaylight.utils.getGamePlayer
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
        if (DeadByDaylight.gameManager.gameState != GameState.INGAME) return
        val player = event.player
        val gamePlayer = player.getGamePlayer() ?: return

        if (gamePlayer !is Killer) return

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