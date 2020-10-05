package net.ignissak.deadbydaylight.game

import net.ignissak.deadbydaylight.DeadByDaylight
import net.ignissak.deadbydaylight.game.interfaces.GamePlayer
import net.ignissak.deadbydaylight.game.interfaces.SurvivalState
import net.ignissak.deadbydaylight.game.modules.Killer
import net.ignissak.deadbydaylight.game.modules.Survivor
import net.ignissak.deadbydaylight.utils.Log
import net.ignissak.deadbydaylight.utils.getSurvivor
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Team
import java.util.stream.Collectors

class PlayerManager {

    init {
        killerTeam.color = ChatColor.RED
        survivorTeam.color = ChatColor.GREEN

        survivorTeam.setAllowFriendlyFire(false)
        survivorTeam.setCanSeeFriendlyInvisibles(false)
        survivorTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER)
        survivorTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER)

        killerTeam.setAllowFriendlyFire(false)
        killerTeam.setCanSeeFriendlyInvisibles(false)
        killerTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER)
        killerTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER)

        Log.success("Teams loaded.")
    }

    fun registerPlayer(player: Player): GamePlayer {
        val gamePlayer = object: GamePlayer(player) {

            override fun giveStartingItems() {}

            override fun giveStartingPotionEffects() {}

        }

        gamePlayer.onJoin()

        players[player.name] = gamePlayer
        DeadByDaylight.boardManager.setupSidebar(gamePlayer)

        return gamePlayer
    }

    fun unregisterPlayer(gamePlayer: GamePlayer) {
        gamePlayer.onQuit()

        DeadByDaylight.boardManager.removeSidebar(gamePlayer)

        players.remove(gamePlayer.player.name)
        killerTeam.removeEntry(gamePlayer.player.name)
        survivorTeam.removeEntry(gamePlayer.player.name)
    }

    fun registerSurvivor(gamePlayer: GamePlayer): Survivor {
        survivorTeam.addEntry(gamePlayer.player.name)

        val survivor = Survivor(gamePlayer.player)
        players[gamePlayer.player.name] = survivor

        return survivor
    }

    fun registerKiller(gamePlayer: GamePlayer): Killer {
        killerTeam.addEntry(gamePlayer.player.name)

        val killer = Killer(gamePlayer.player)
        players[gamePlayer.player.name] = killer

        return killer
    }

    fun getGamePlayer(player: Player): GamePlayer? {
        return players.getOrDefault(player.name, null)
    }

    fun getGamePlayer(name: String): GamePlayer? {
        return players.getOrDefault(name, null)
    }

    fun areTeamsFilled(): Boolean = killerTeam.entries.size == 1 && survivorTeam.entries.size == 4

    fun isAnySurvivorAlive(): Boolean = survivorTeam.entries.stream().anyMatch { it.getSurvivor()?.survivalState == SurvivalState.PLAYING }

    fun isAnySurvivorDying(): Boolean = survivorTeam.entries.stream().anyMatch { it.getSurvivor()?.survivalState == SurvivalState.DYING }

    fun getSurvivorsDying(): MutableList<Survivor>? = survivorTeam.entries.stream().filter{ it.getSurvivor()?.survivalState == SurvivalState.DYING }.map { it.getSurvivor() }.collect(Collectors.toList())

    companion object {
        val players: LinkedHashMap<String, GamePlayer> = linkedMapOf()
        val killerTeam: Team = BoardManager.xoreBoard.scoreboard.registerNewTeam("killer")
        val survivorTeam: Team = BoardManager.xoreBoard.scoreboard.registerNewTeam("survivors")
    }
}