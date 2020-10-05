package net.ignissak.deadbydaylight.game

import net.ignissak.deadbydaylight.DeadByDaylight
import net.ignissak.deadbydaylight.game.interfaces.GamePlayer
import net.ignissak.deadbydaylight.game.interfaces.GameState
import net.ignissak.deadbydaylight.game.interfaces.SurvivalState
import net.ignissak.deadbydaylight.game.modules.Killer
import net.ignissak.deadbydaylight.game.modules.Survivor
import net.ignissak.deadbydaylight.utils.getSurvivor
import net.minecord.xoreboardutil.bukkit.XoreBoard
import net.minecord.xoreboardutil.bukkit.XoreBoardUtil

class BoardManager {

    fun setupSidebar(gamePlayer: GamePlayer) {
        xoreBoard.addPlayer(gamePlayer.player)

        val privateSidebar = xoreBoard.getPrivateSidebar(gamePlayer.player)

        privateSidebar.displayName = "§c§lHALLOWEEN"
        privateSidebar.clearLines()
        privateSidebar.lines = getLines(gamePlayer)

        privateSidebar.showSidebar()
    }

    fun removeSidebar(gamePlayer: GamePlayer) {
        xoreBoard.removePlayer(gamePlayer.player)

        val privateSidebar = xoreBoard.getPrivateSidebar(gamePlayer.player)

        privateSidebar.hideSidebar()
    }

    private fun update(gamePlayer: GamePlayer) {
        val privateSidebar = xoreBoard.getPrivateSidebar(gamePlayer.player)

        privateSidebar.rewriteLines(getLines(gamePlayer))
    }

    fun updateAllPlayers() {
        for (player in PlayerManager.players.values) {
            this.update(player)
        }
    }

    private fun getLines(gamePlayer: GamePlayer): HashMap<String, Int> {
        val hashMap: HashMap<String, Int> = hashMapOf()
        when (DeadByDaylight.gameManager.gameState) {
            GameState.LOBBY -> {
                hashMap["§5"] = 5
                hashMap["§fHráči: §c${PlayerManager.players.size}§7/§c5"] = 4
                hashMap["§fMin. hráčů: §c4"] = 3
                hashMap["§2"] = 2
                hashMap["§7mc.craftmania.cz"] = 1
            }
            GameState.STARTING -> {
                hashMap["§5"] = 5
                hashMap["§fHráči: §c${PlayerManager.players.size}§7/§c5"] = 4
                hashMap["§fStart za: §c${DeadByDaylight.gameManager.countdown}s"] = 3 // TEST
                hashMap["§2"] = 2
                hashMap["§7mc.craftmania.cz"] = 1
            }
            GameState.INGAME -> {
                hashMap["§8"] = 8
                hashMap["§fČas: §c${DeadByDaylight.gameManager.getGameTimeFormatted()}"] = 7
                hashMap["§6"] = 6
                hashMap["§fSurvivoři: §c${PlayerManager.survivorTeam.entries.stream().filter { it.getSurvivor()?.survivalState != SurvivalState.SPECTATING }.count().toInt()}/${DeadByDaylight.gameManager.startingPlayers - 1}"] = 5
                hashMap["§fGenerátory:"] = 4
                hashMap[getActivatedGenerators()] = 3
                hashMap["§2"] = 2
                hashMap["§7mc.craftmania.cz"] = 1
            }
            GameState.ENDING -> {
                if (gamePlayer is Survivor) {
                    hashMap["§1§0"] = 10
                    hashMap["§fOživil jsi"] = 9
                    hashMap["§c${getNumberOfRevives(gamePlayer.revivedPlayers)}§f."] = 8
                    hashMap["§6"] = 7
                    hashMap["§fPomohol jsi dokončit"] = 6
                    hashMap["§c${getNumberOfGenerators(DeadByDaylight.gameManager.generators.count { it.contributors.contains(gamePlayer.player.name) })}§f."] = 5
                } else if (gamePlayer is Killer) {
                    hashMap["§8"] = 8
                    hashMap["§fZasažení: §c" + gamePlayer.playerHits] = 7
                    hashMap["§fSmrtelných zasažení: §c" + gamePlayer.playerDowns] = 6
                    hashMap["§fKillů: §c" + gamePlayer.playerKills] = 5
                }
                hashMap["§4"] = 4
                hashMap["§fCoinů: §c" + gamePlayer.coins] = 3
                hashMap["§2"] = 2
                hashMap["§7mc.craftmania.cz"] = 1
            }
        }
        return hashMap
    }

    // "\u25A0"
    private fun getActivatedGenerators(): String {
        val stringBuilder = StringBuilder()
        if (DeadByDaylight.gameManager.generators.stream().anyMatch { it.isActivated() }) {
            stringBuilder.append("§a\u25A0".repeat(DeadByDaylight.gameManager.generators.count { it.isActivated() }))
        }
        if (DeadByDaylight.gameManager.generators.stream().anyMatch { it.isActivated() } && DeadByDaylight.gameManager.generators.stream().anyMatch { !it.isActivated() }) {
            stringBuilder.append("§7|")
        }
        if (DeadByDaylight.gameManager.generators.stream().anyMatch { !it.isActivated() }) {
            stringBuilder.append("§c\u25A0".repeat(DeadByDaylight.gameManager.generators.count { !it.isActivated() }))
        }
        return stringBuilder.toString()
    }

    private fun getNumberOfRevives(int: Int): String {
        if (int == 0 || int >= 5) {
            return "$int spoluhráčů"
        } else if (int in 1..4) {
            return "$int spoluhráče"
        }
        return "$int spoluháčů"
    }

    private fun getNumberOfGenerators(int: Int): String {
        if (int == 0 || int >= 5) {
            return "$int generátorů"
        } else if (int == 1) {
            return "$int generátor"
        } else if (int in 2..4) {
            return "$int generátory"
        }
        return "$int generátorů"
    }

    companion object {
        val xoreBoard: XoreBoard = XoreBoardUtil.createXoreBoard("deadbydaylight")
    }
}