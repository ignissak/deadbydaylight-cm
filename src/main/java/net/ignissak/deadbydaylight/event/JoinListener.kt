package net.ignissak.deadbydaylight.event

import net.ignissak.deadbydaylight.DeadByDaylight
import net.ignissak.deadbydaylight.game.ItemManager
import net.ignissak.deadbydaylight.game.interfaces.GamePlayer
import net.ignissak.deadbydaylight.game.interfaces.GameState
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.server.ServerListPingEvent

class JoinListener : Listener {

    @EventHandler
    fun onLogin(event: PlayerLoginEvent) {
        if (DeadByDaylight.gameManager.gameState == GameState.INGAME)
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "§cNa tomto serveru již hra běží.")
        else if (DeadByDaylight.gameManager.gameState == GameState.ENDING)
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "§cTento server se restartuje, chvíli počkej.")
        else if (Bukkit.getOnlinePlayers().size == 5)
            event.disallow(PlayerLoginEvent.Result.KICK_FULL, "§cServer je plný.")
    }

    @EventHandler
    fun onPing(event: ServerListPingEvent) {
        event.motd = DeadByDaylight.gameManager.gameState.toString()
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        DeadByDaylight.playerManager.registerPlayer(player)

        event.joinMessage = "${DeadByDaylight.prefix}${player.name} se §apřipojil §7(${Bukkit.getOnlinePlayers().size}/5)"
        player.inventory.setItem(0, ItemManager.role)
        player.inventory.setItem(8, ItemManager.stats)

        DeadByDaylight.boardManager.updateAllPlayers()

        DeadByDaylight.gameManager.tryStart()
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        val craftPlayer: GamePlayer = DeadByDaylight.playerManager.getGamePlayer(player) ?: return

        if (DeadByDaylight.gameManager.gameState == GameState.INGAME) {
            DeadByDaylight.gameManager.tryEnd()
        }

        event.quitMessage = "${DeadByDaylight.prefix}${player.name} se §codpojil §7(${Bukkit.getOnlinePlayers().size - 1}/5)"
        DeadByDaylight.playerManager.unregisterPlayer(craftPlayer)

        DeadByDaylight.boardManager.updateAllPlayers()
    }
}