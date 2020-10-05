package net.ignissak.deadbydaylight.game.interfaces

import com.google.gson.Gson
import cz.craftmania.crafteconomy.api.CraftCoinsAPI
import cz.craftmania.craftlibs.CraftLibs
import net.citizensnpcs.api.CitizensAPI
import net.ignissak.deadbydaylight.DeadByDaylight
import net.ignissak.deadbydaylight.game.PlayerManager
import net.ignissak.deadbydaylight.game.modules.GameStats
import net.ignissak.deadbydaylight.game.modules.Killer
import net.ignissak.deadbydaylight.game.modules.Survivor
import net.ignissak.deadbydaylight.utils.Log
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player

abstract class GamePlayer(val player: Player) {

    var gameStats: GameStats = GameStats()
    private val gson: Gson = Gson()
    var rolePreference: RolePreference = RolePreference.FILL
    var coins: Int = 0
    private var gotCoins: Boolean = false

    fun onJoin() {
        // Loading statistics
        CraftLibs.getSqlManager().query("SELECT * FROM dbd_players WHERE uuid = ?;", player.uniqueId.toString()).whenComplete{ dbRows, _ ->
            if (dbRows.isEmpty()) {
                Log.info("Inserting data for ${player.name}")
                CraftLibs.getSqlManager().query("INSERT INTO dbd_players (uuid, nickname) VALUES (?, ?);", player.uniqueId.toString(), player.name)
            } else {
                Log.info("Loading data for ${player.name}")
                gameStats = gson.fromJson(dbRows[0].getString("stats"), GameStats::class.java)
            }
        }

        player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 20.0
        player.health = 20.0
        player.foodLevel = 20
        player.exp = 0F
        player.level = 0
        player.gameMode = GameMode.SURVIVAL
        player.inventory.clear()
        player.equipment?.armorContents = emptyArray()

        player.playSound(player.location, Sound.AMBIENT_CAVE, 1F, 1F)
        player.activePotionEffects.forEach { player.removePotionEffect(it.type) }
        DeadByDaylight.gameManager.lobbyLocation?.let { player.teleport(it) }
        this.showToOthers()

        DeadByDaylight.gameManager.tryStart()
    }

    fun onQuit() {

        if (this is Survivor) {
            this.npc.destroy()
            CitizensAPI.getNPCRegistry().deregister(this.npc)
        }

        if (DeadByDaylight.gameManager.gameState == GameState.INGAME)
            DeadByDaylight.gameManager.tryEnd()

    }

    fun updateStats() {
        CraftLibs.getSqlManager().query("UPDATE dbd_players SET stats = ? WHERE uuid = ?;", gson.toJson(gameStats), player.uniqueId.toString()).thenAcceptAsync { Log.info("Updated statistics for ${player.name}.") }
    }

    abstract fun giveStartingItems()
    abstract fun giveStartingPotionEffects()

    @Deprecated("Directly check instance with 'is' keyword.", ReplaceWith("this is Survivor", "net.ignissak.deadbydaylight.game.modules.Survivor"), DeprecationLevel.ERROR)
    fun isSurvivor(): Boolean = this is Survivor
    @Deprecated("Directly check instance with 'is' keyword.", ReplaceWith("this is Killer", "net.ignissak.deadbydaylight.game.modules.Killer"), DeprecationLevel.ERROR)
    fun isKiller(): Boolean = this is Killer
    fun isAssignedToTeam(): Boolean = PlayerManager.killerTeam.entries.contains(player.name) || PlayerManager.survivorTeam.entries.contains(player.name)

    fun hideFromOthers() {
        Bukkit.getOnlinePlayers().forEach { DeadByDaylight.instance.let { it1 -> it.hidePlayer(it1, this.player) } }
    }

    fun showToOthers() {
        Bukkit.getOnlinePlayers().forEach { DeadByDaylight.instance.let { it1 -> it.showPlayer(it1, this.player) } }
    }

    fun giveCoins() {
        if (coins > 0 && !gotCoins) {
            gotCoins = true
            CraftCoinsAPI.giveCoins(player, coins.toLong())
        }
    }

    override fun toString(): String {
        return "GamePlayer(player=$player, gameStats=$gameStats, rolePreference=$rolePreference, coins=$coins)"
    }

}