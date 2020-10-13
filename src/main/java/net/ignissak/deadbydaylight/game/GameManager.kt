/*
 * Copyright (c) 2020.
 * Made by Jakub 'iGniSs' Bordáš.
 */

package net.ignissak.deadbydaylight.game


import cz.craftmania.craftcore.spigot.messages.Title
import net.ignissak.deadbydaylight.DeadByDaylight
import net.ignissak.deadbydaylight.api.event.GameStartEvent
import net.ignissak.deadbydaylight.game.interfaces.*
import net.ignissak.deadbydaylight.game.modules.Gate
import net.ignissak.deadbydaylight.game.modules.Generator
import net.ignissak.deadbydaylight.game.modules.Killer
import net.ignissak.deadbydaylight.game.modules.LootChest
import net.ignissak.deadbydaylight.game.task.FrequentTryEndTask
import net.ignissak.deadbydaylight.game.task.LocationTask
import net.ignissak.deadbydaylight.game.task.RunningGeneratorTask
import net.ignissak.deadbydaylight.game.task.SurvivorRevivingSurvivorTask
import net.ignissak.deadbydaylight.utils.*
import org.apache.commons.lang3.time.DurationFormatUtils
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.lang.IllegalStateException
import java.util.*
import java.util.stream.Collectors

class GameManager {

    var gameState: GameState = GameState.LOBBY
    private val minSurvivors = 3
    var isDisabledMoving: Boolean = false
    var startedAt: Long = 0
    private var endsAt: Long = 0
    private var endedAt: Long = 0
    var startingPlayers: Int = 0
    var countdown: Int = 30

    var generators: MutableList<Generator> = mutableListOf()
    // TODO: Place another chests
    var lootChests: MutableList<LootChest> = mutableListOf()
    var drops: MutableList<Location> = mutableListOf()

    var lobbyLocation: Location? = LocationUtils.parseLocation(DeadByDaylight.instance.config.getString("locations.lobby"))
    var survivorLocations: MutableList<Location> = mutableListOf()
    var killerLocations: MutableList<Location> = mutableListOf()
    var dumpLocation: Location? = LocationUtils.parseLocation(DeadByDaylight.instance.config.getString("locations.dumb"))

    val revivingTasks: MutableList<SurvivorRevivingSurvivorTask> = mutableListOf()
    val gates: MutableList<Gate> = mutableListOf()

    var locationTask: LocationTask = LocationTask()
    var checkTask: FrequentTryEndTask = FrequentTryEndTask()

    // Loading locations for loot chests, generators and drops
    init {
        this.clearEntities()

        DeadByDaylight.instance.config.getStringList("locations.survivor").forEach {
            LocationUtils.parseLocation(it, true)?.let { it1 -> survivorLocations.add(it1) }
        }
        Log.info("Registered ${survivorLocations.size} survivor spawn locations.")

        DeadByDaylight.instance.config.getStringList("locations.killer").forEach {
            LocationUtils.parseLocation(it, true)?.let { it1 -> killerLocations.add(it1) }
        }
        Log.info("Registered ${survivorLocations.size} killer spawn locations.")

        DeadByDaylight.instance.config.getStringList("locations.chests").forEach {
            LocationUtils.parseLocation(it, false)?.let { it1 -> LootChest(it1) }?.let { it2 -> lootChests.add(it2) }
        }
        Log.info("Registered ${lootChests.size} loot chests.")
        if (lootChests.size < 15) {
            Log.warning("There are less than 15 loot chests, plugin may misbehave.")
        }

        DeadByDaylight.instance.config.getStringList("locations.generators").forEach {
            LocationUtils.parseLocation(it, false)?.let { it1 -> Generator(it1) }?.let { it2 -> generators.add(it2) }
        }
        Log.info("Registered ${generators.size} generators.")
        if (generators.size < 10) {
            Log.warning("There are less than 10 generators, plugin may misbehave.")
        }

        DeadByDaylight.instance.config.getStringList("locations.drops").forEach {
            LocationUtils.parseLocation(it, false)?.let { it1 -> drops.add(it1) }
        }
        Log.info("Registered ${drops.size} drops.")

        gates.add(Gate(GameRegion.IRON_BARS_1, Material.IRON_BARS))
        gates.add(Gate(GameRegion.IRON_BARS_2, Material.IRON_BARS))
        Log.info("Registered ${gates.size} gates.")

    }

    private fun canStart(): Boolean = PlayerManager.players.size > this.minSurvivors && gameState == GameState.LOBBY

    fun tryStart() {
        if (!canStart()) return

        this.startCountdown()
    }

    private fun startCountdown() {
        this.gameState = GameState.STARTING

        DeadByDaylight.instance.let { DeadByDaylight.boardUpdateTask.runTaskTimerAsynchronously(it, 0L, 20L) }

        val run: BukkitRunnable = object : BukkitRunnable() {
            override fun run() {
                if (gameState == GameState.INGAME) this.cancel()

                if (!canStart()) {
                    Utils.broadcast(true, "Start hry přerušen §c(nedostatek hráčů)§7.")
                    gameState = GameState.LOBBY

                    Bukkit.getOnlinePlayers().forEach { it.level = 0 }

                    this.cancel()
                    return
                }

                Bukkit.getOnlinePlayers().forEach { it.level = 0 }

                if (countdown <= 0) {
                    startGame()
                    this.cancel()
                    return
                } else if (countdown <= 5) {
                    Utils.broadcast(true, "Teleportace do arény za §c$countdown §7${getFormattedSeconds(countdown)}.")
                    Utils.sendSoundGlobally(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, getPitch(countdown))
                }

                countdown--
            }
        }
        DeadByDaylight.instance.let { run.runTaskTimer(it, 0L, 20L) }
    }

    fun forceStart() {
        this.startGame()
    }

    private fun startGame() {
        // TEST: Start game

        startedAt = System.currentTimeMillis()
        endsAt = System.currentTimeMillis() + (15 * 60 * 1000)
        startingPlayers = PlayerManager.players.size
        gameState = GameState.INGAME
        isDisabledMoving = true
        this.clearEntities()

        // Increase statistics
        PlayerManager.players.values.forEach{ it.gameStats.games_played += 1 }

        countdown = 15

        this.createTeams()

        DeadByDaylight.boardUpdateTask.runTaskTimerAsynchronously(DeadByDaylight.instance, 0, 20)

        // Killer teleporting
        PlayerManager.killerTeam.entries.forEach {
            it.getPlayer()?.teleport(killerLocations.random())
        }

        // Survivor teleporting
        val queue: Queue<Location> = LinkedList(survivorLocations.shuffled())

        PlayerManager.survivorTeam.entries.forEach {
            val location = queue.remove()

            it.getPlayer()?.teleport(location)
            queue.add(location)
        }

        // Survivor's health & hunger
        PlayerManager.survivorTeam.entries.forEach {
            it.getPlayer()?.gameMode = GameMode.ADVENTURE
            it.getPlayer()?.foodLevel = 6
            it.getPlayer()?.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 4.0
            it.getPlayer()?.health = 4.0
        }

        PlayerManager.killerTeam.entries.forEach {
            it.getPlayer()?.gameMode = GameMode.ADVENTURE
            it.getPlayer()?.foodLevel = 0
            it.getPlayer()?.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 2.0
            it.getPlayer()?.health = 2.0

            it.getKiller()?.disguise()
        }

        // Game items
        PlayerManager.players.values.forEach {
            it.giveStartingItems()
        }

        // Adding loot to loot chests
        if (lootChests.size > 0) {
            lootChests.forEach { it.loot.add(ItemManager.fuel) }
            lootChests.shuffled().take(14).forEach { it.loot.add(ItemManager.bandage) }
            lootChests.shuffled().take(9).forEach { it.loot.add(ItemManager.flash) }

            Log.info("Loot in loot chests has been generated.")
        }

        // Adding loot to drops
        if (drops.size > 0) {
            drops.forEach {
                when (Random().nextInt(4)) {
                    2 -> {
                        val dropItem = it.world?.dropItem(it, ItemManager.flash)
                        dropItem?.customName = "§9Zapalovač"
                        dropItem?.isCustomNameVisible = true
                    }
                    3 -> {
                        val dropItem = it.world?.dropItem(it, ItemManager.bandage)
                        dropItem?.customName = "§cBandáž"
                        dropItem?.isCustomNameVisible = true
                    }
                    else -> {
                        val dropItem = it.world?.dropItem(it, ItemManager.fuel)
                        dropItem?.customName = "§eBaterie"
                        dropItem?.isCustomNameVisible = true
                    }
                }
            }

            Log.info("Drops have been spawned.")
        }

        // Killer messages
        TextComponentBuilder("").send(PlayerManager.killerTeam)
        TextComponentBuilder("${Constants.halloweenColor}&lHALLOWEEN", true).send(PlayerManager.killerTeam)
        TextComponentBuilder("").send(PlayerManager.killerTeam)
        TextComponentBuilder("&e&lJSI KILLER!", true).send(PlayerManager.killerTeam)
        TextComponentBuilder("").send(PlayerManager.killerTeam)
        TextComponentBuilder("&7Tvým úkolem je chytit a zabít", true).send(PlayerManager.killerTeam)
        TextComponentBuilder("&7survivory, než stihnou uniknout.", true).send(PlayerManager.killerTeam)
        TextComponentBuilder("").send(PlayerManager.killerTeam)

        // Survivor messages
        TextComponentBuilder("").send(PlayerManager.survivorTeam)
        TextComponentBuilder("${Constants.halloweenColor}&lHALLOWEEN", true).send(PlayerManager.survivorTeam)
        TextComponentBuilder("").send(PlayerManager.survivorTeam)
        TextComponentBuilder("&a&lJSI SURVIVOR!", true).send(PlayerManager.survivorTeam)
        TextComponentBuilder("").send(PlayerManager.survivorTeam)
        TextComponentBuilder("&7Tvým úkolem je spolu s ostatními", true).send(PlayerManager.survivorTeam)
        TextComponentBuilder("&7opravit aspoň 4 generátory a uniknout", true).send(PlayerManager.survivorTeam)
        TextComponentBuilder("&7před killerem, který se vás snaží zabít.", true).send(PlayerManager.survivorTeam)
        TextComponentBuilder("&7Po mapě se spawnují truhly s bateriemi,", true).send(PlayerManager.survivorTeam)
        TextComponentBuilder("&7bandážema nebo zapalovačem. Také jsou i", true).send(PlayerManager.survivorTeam)
        TextComponentBuilder("&7někde na zemi pohozené.", true).send(PlayerManager.survivorTeam)
        TextComponentBuilder("").send(PlayerManager.survivorTeam)

        val run: BukkitRunnable = object : BukkitRunnable() {
            override fun run() {
                if (countdown == 0) {
                    // TEST: Start
                    Bukkit.getPluginManager().callEvent(GameStartEvent())

                    isDisabledMoving = false
                    Title("§c§lHRA ZAČÍNÁ", "§7Hodně štěstí!", 0, 60, 20).broadcast()
                    Utils.sendSoundGlobally(Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1F, 1F)
                    Utils.broadcast(true, "Hra začíná!")

                    PlayerManager.survivorTeam.entries.forEach {
                        it.getGamePlayer()?.giveStartingPotionEffects()
                    }

                    Bukkit.getOnlinePlayers().forEach {
                        it.level = 0
                    }

                    locationTask.runTaskTimer(DeadByDaylight.instance, 0, 20)
                    checkTask.runTaskTimer(DeadByDaylight.instance, 0, 20)

                    this.cancel()
                    return
                }

                if (countdown <= 5 && countdown != 0) {
                    Title(getFormattedInt(countdown), "", 0, 25, 0).broadcast()
                    Utils.sendSoundGlobally(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, getPitch(countdown))
                }

                Bukkit.getOnlinePlayers().forEach { it.level = countdown }

                countdown--
            }
        }
        DeadByDaylight.instance.let { run.runTaskTimer(it, 0L, 20L) }
    }

    // TEST
    private fun createTeams() {
        // DEBUG: println(PlayerManager.players.values.stream().map { it.toString() }.toArray().joinToString(", ", "[", "]"))
        // 1: Randomly choose killer, remaining players will fill survivors team
        if (PlayerManager.players.values.stream().anyMatch { it.rolePreference == RolePreference.KILLER }) {
            DeadByDaylight.playerManager.registerKiller(PlayerManager.players.values.filter { it.rolePreference == RolePreference.KILLER }.random())
        }

        // 2: Assign 4 players with survivor preference
        if (PlayerManager.players.values.stream().filter { it.rolePreference == RolePreference.SURVIVOR && !it.isAssignedToTeam() }.count().toInt() == 5) {
            // All 5 players want to be survivors, choose randomly
            val collect = PlayerManager.players.values.stream().filter { it.rolePreference == RolePreference.SURVIVOR }.collect(Collectors.toList())
            val iterator: MutableIterator<GamePlayer> = collect.shuffled().iterator() as MutableIterator<GamePlayer>
            for (i in 1..4) {
                if (!iterator.hasNext()) continue
                val gamePlayer: GamePlayer = iterator.next()

                DeadByDaylight.playerManager.registerSurvivor(gamePlayer)
                iterator.remove()
            }
        } else {
            for (gamePlayer in PlayerManager.players.values.stream().filter { it.rolePreference == RolePreference.SURVIVOR && !it.isAssignedToTeam() }) {
                DeadByDaylight.playerManager.registerSurvivor(gamePlayer)
            }
        }

        if (DeadByDaylight.playerManager.areTeamsFilled()) return

        // 3: Fill teams with other players
        if (PlayerManager.players.values.stream().filter { it.rolePreference == RolePreference.FILL && !it.isAssignedToTeam() }.count().toInt() > 0) {
            // There are players that want to fill some role
            for (gamePlayer in PlayerManager.players.values.stream().filter { it.rolePreference == RolePreference.FILL && !it.isAssignedToTeam() }) {
                if (!PlayerManager.killerTeam.isFull()) {
                    DeadByDaylight.playerManager.registerKiller(gamePlayer)
                } else {
                    DeadByDaylight.playerManager.registerSurvivor(gamePlayer)
                }
            }
        }

        // 4: Check for remaining players
        if (PlayerManager.players.values.stream().anyMatch { !it.isAssignedToTeam() }) {
            // Some players are not assigned to a team
            for (gamePlayer in PlayerManager.players.values.filter { !it.isAssignedToTeam() }) {
                if (!PlayerManager.killerTeam.isFull()) {
                    DeadByDaylight.playerManager.registerKiller(gamePlayer)
                } else {
                    DeadByDaylight.playerManager.registerSurvivor(gamePlayer)
                }
            }
        }
    }

    fun tryEnd(): Boolean {
        // TEST
        if (gameState != GameState.INGAME) return false
        if (PlayerManager.survivorTeam.entries.stream().noneMatch{ it.getSurvivor()?.survivalState == SurvivalState.PLAYING}
                || PlayerManager.survivorTeam.entries.size == 0
                || PlayerManager.killerTeam.entries.size == 0
                || endsAt < System.currentTimeMillis()) {
            this.endGame()
            return true
        }
        return false
    }

    private fun endGame(endReason: EndReason? = null) {
        if (gameState == GameState.ENDING) return
        endedAt = System.currentTimeMillis()
        gameState = GameState.ENDING

        try {
            runningGeneratorTask.cancel()

        } catch (ignored: IllegalStateException) {}

        try {
            checkTask.cancel()
        } catch (ignored: IllegalStateException) {}

        Bukkit.getScheduler().cancelTask(DeadByDaylight.boardUpdateTask.taskId)
        DeadByDaylight.boardManager.updateAllPlayers()

        this.clearEntities()

        lootChests.forEach { it.close() }

        PlayerManager.players.values.forEach {
            if (it is Killer) {
                it.gameStats.playtime += System.currentTimeMillis() - startedAt
            }
            it.giveCoins()

            it.player.gameMode = GameMode.SPECTATOR
        }

        TextComponentBuilder("").broadcast()
        TextComponentBuilder("§c§lKONEC HRY", true).broadcast()
        if (endReason == EndReason.TIME_RUN_OUT)
             TextComponentBuilder("§8[Vypršel čas]", true).broadcast()
        TextComponentBuilder("").broadcast()
        TextComponentBuilder("§fDěkujeme za zahrání naší", true).broadcast()
        TextComponentBuilder("§fHalloween minihry.", true).broadcast()
        TextComponentBuilder("").broadcast()
        TextComponentBuilder("§8Made with <3 by iGniSsak", true).broadcast()
        TextComponentBuilder("").broadcast()

        Utils.broadcast(true, "Za 15 sekund se restartuje server.")

        DeadByDaylight.instance.let { Bukkit.getScheduler().runTaskLater(it, this::shutDown, 15 * 20) }
    }

    private fun shutDown() {
        Bukkit.shutdown()
    }

    /**
     * Sets gamerules and settings for arena.
     */
    fun setupWorld() {
        if (lobbyLocation == null) return
        val world = lobbyLocation!!.world

        world?.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        world?.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        world?.setGameRule(GameRule.DO_ENTITY_DROPS, false)
        world?.setGameRule(GameRule.DO_TILE_DROPS, false)
        world?.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false)
        world?.setGameRule(GameRule.DO_MOB_SPAWNING, false)
        world?.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
        world?.setGameRule(GameRule.MAX_ENTITY_CRAMMING, 100)
        world?.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false)
        world?.difficulty = Difficulty.HARD
        world?.time = 15000L
    }

    private fun getFormattedInt(i: Int): String? {
        when (i) {
            5 -> return "§c❺"
            4 -> return "§6❹"
            3 -> return "§e❸"
            2 -> return "§2❷"
            1 -> return "§a❶"
        }
        return i.toString()
    }


    private fun getFormattedSeconds(i: Int): String? {
        if (i >= 5) {
            return "vteřin"
        } else if (i > 1) {
            return "vteřiny"
        }
        return "vteřinu"
    }

    private fun getPitch(i: Int): Float {
        return when (i) {
            5 -> 0.2f
            4 -> 0.4f
            3 -> 0.6f
            2 -> 0.8f
            else -> 1.0f
        }
    }

    fun getGameTimeFormatted(): String = DurationFormatUtils.formatDuration((endsAt - System.currentTimeMillis()), "mm:ss")

    fun getLootChestAt(location: Location): LootChest? = lootChests.find { it.location.block.location == location }

    fun getGeneratorAt(location: Location): Generator? = generators.find { it.location.block.location == location }

    fun clearEntities() {
        lobbyLocation?.world?.entities?.forEach { if (it !is Player) it.remove() }
    }

    companion object {
        val runningGeneratorTask: RunningGeneratorTask = RunningGeneratorTask()
    }

}