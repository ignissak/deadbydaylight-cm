package net.ignissak.deadbydaylight

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.regions.RegionContainer
import cz.craftmania.craftlibs.CraftLibs
import net.citizensnpcs.api.CitizensAPI
import net.ignissak.deadbydaylight.command.AdminCommands
import net.ignissak.deadbydaylight.command.PlayerCommands
import net.ignissak.deadbydaylight.event.*
import net.ignissak.deadbydaylight.game.BoardManager
import net.ignissak.deadbydaylight.game.GameManager
import net.ignissak.deadbydaylight.game.PlayerManager
import net.ignissak.deadbydaylight.game.interfaces.GameRegion
import net.ignissak.deadbydaylight.game.task.BoardUpdateTask
import net.ignissak.deadbydaylight.utils.Constants
import net.ignissak.deadbydaylight.utils.Log
import net.ignissak.deadbydaylight.utils.getSurvivor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.java.JavaPlugin
import java.lang.Exception

class DeadByDaylight : JavaPlugin() {

    override fun onEnable() {
        Log.info("Initializing plugin...")
        instance = this

        config.options().copyDefaults(true)
        saveDefaultConfig()

        regionContainer = WorldGuard.getInstance().platform.regionContainer

        gameManager = GameManager()
        playerManager = PlayerManager()
        boardManager = BoardManager()

        gameManager.setupWorld()

        CraftLibs.getSqlManager().query("create table if not exists minigames.dbd_players\n" +
                "(\n" +
                "    id       int auto_increment\n" +
                "        primary key,\n" +
                "    uuid     varchar(64)                                        not null,\n" +
                "    nickname varchar(32)                                        not null,\n" +
                "    stats    longtext collate utf8mb4_bin default json_object() not null,\n" +
                "    constraint stats\n" +
                "        check (json_valid(`stats`))\n" +
                ");\n" +
                "\n")

        this.registerListeners()

        val adminCommands = AdminCommands()
        getCommand("forcestart")?.setExecutor(adminCommands)
        getCommand("setlobby")?.setExecutor(adminCommands)
        getCommand("addkillerspawn")?.setExecutor(adminCommands)
        getCommand("addsurvivorspawn")?.setExecutor(adminCommands)
        getCommand("adddrop")?.setExecutor(adminCommands)
        getCommand("addgenerator")?.setExecutor(adminCommands)
        getCommand("addlootchest")?.setExecutor(adminCommands)

        getCommand("role")?.setExecutor(PlayerCommands())

        Log.info("Unregistering & destroying NPCs...")
        CitizensAPI.getNPCRegistry().forEach { it.destroy() }
        CitizensAPI.getNPCRegistry().deregisterAll()

        gameManager.survivorLocations.forEach {
            it.chunk.load()
        }

        gameManager.killerLocations.forEach {
            it.chunk.load()
        }
    }

    override fun onDisable() {
        Log.info("Turning off...")

        gameManager.gates.forEach { it.close() }
        gameManager.lootChests.forEach { it.close() }

        PlayerManager.survivorTeam.entries.forEach {
            it.getSurvivor()?.destroyNPC()
        }

        gameManager.clearEntities()

        try {
            boardUpdateTask.cancel()
        } catch (ignored: Exception) {}

        saveConfig()

        Bukkit.getOnlinePlayers().forEach { it.kickPlayer("§cTento server se uzavřel.") }
    }

    private fun registerListeners() {
        val pluginManager = Bukkit.getPluginManager()

        pluginManager.registerEvents(GeneralListener(), this)
        pluginManager.registerEvents(JoinListener(), this)
        pluginManager.registerEvents(WorldListener(), this)
        pluginManager.registerEvents(GameListener(), this)
        pluginManager.registerEvents(RegionListener(), this)
    }

    companion object {
        lateinit var instance: DeadByDaylight
        lateinit var gameManager: GameManager
        lateinit var playerManager: PlayerManager
        lateinit var boardManager: BoardManager
        val prefix: String = org.bukkit.ChatColor.translateAlternateColorCodes('&', "${Constants.halloweenColor}&lHalloween &8| &7 ")
        val boardUpdateTask: BoardUpdateTask = BoardUpdateTask()
        lateinit var regionContainer: RegionContainer
    }
}