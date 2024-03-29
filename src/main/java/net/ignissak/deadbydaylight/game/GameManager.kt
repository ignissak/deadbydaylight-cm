/*
 * Copyright (c) 2020.
 * Made by Jakub 'iGniSs' Bordáš.
 */

package net.ignissak.deadbydaylight.game


import cz.craftmania.craftcore.spigot.messages.BossBar
import cz.craftmania.craftcore.spigot.messages.Title
import cz.craftmania.craftlibs.CraftLibs
import net.ignissak.deadbydaylight.DeadByDaylight
import net.ignissak.deadbydaylight.game.interfaces.*
import net.ignissak.deadbydaylight.game.modules.*
import net.ignissak.deadbydaylight.game.task.*
import net.ignissak.deadbydaylight.utils.*
import org.apache.commons.lang3.time.DurationFormatUtils
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import java.util.stream.Collectors

class GameManager {

    var gameState: GameState = GameState.LOBBY
    private val minSurvivors = 4
    var isDisabledMoving: Boolean = false
    var startedAt: Long = 0
    var endsAt: Long = 0
    private var endedAt: Long = 0
    var startingPlayersInt: Int = 0
    var startingPlayers: MutableList<String> = mutableListOf()
    var countdown: Int = 30
    var gatesOpenedAt: Long = 0

    var generators: MutableList<Generator> = mutableListOf()
    var lootChests: MutableList<LootChest> = mutableListOf()
    var drops: MutableList<Location> = mutableListOf()

    var lobbyLocation: Location? = LocationUtils.parseLocation(DeadByDaylight.instance.config.getString("locations.lobby"))
    var survivorLocations: MutableList<Location> = mutableListOf()
    var killerLocations: MutableList<Location> = mutableListOf()
    var dumpLocation: Location? = LocationUtils.parseLocation(DeadByDaylight.instance.config.getString("locations.dumb"))
    var fireworkLocations: MutableList<Location> = mutableListOf()

    val revivingTasks: MutableList<SurvivorRevivingSurvivorTask> = mutableListOf()
    val gates: MutableList<Gate> = mutableListOf()

    var locationTask: LocationTask = LocationTask()
    var checkTask: FrequentTryEndTask = FrequentTryEndTask()
    private var booTask = BooTask()

    // Default: 5
    // If only 3 survivors: 4
    var neededGenerators = 5

    var bossBar: BossBar? = null
    var bossBarTask: BossBarTask? = null

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

        DeadByDaylight.instance.config.getStringList("locations.fireworks").forEach {
            LocationUtils.parseLocation(it, false)?.let { it1 -> fireworkLocations.add(it1) }
        }
        Log.info("Registered ${fireworkLocations.size} firework locations.")

    }

    private fun canStart(): Boolean = PlayerManager.players.size >= this.minSurvivors

    fun tryStart() {
        if (!canStart()) return

        this.startCountdown()
    }

    private fun startCountdown() {
        if (this.gameState == GameState.STARTING) return
        this.gameState = GameState.STARTING

        DeadByDaylight.boardUpdateTask = BoardUpdateTask()
        DeadByDaylight.boardUpdateTask.runTaskTimerAsynchronously(DeadByDaylight.instance, 0L, 20L)

        countdown = 30

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

                if (Bukkit.getOnlinePlayers().size > 5) {
                    // How this happens? Who knows.
                    Bukkit.getOnlinePlayers().random().kickPlayer("§cTento server je plný.")
                }

                Bukkit.getOnlinePlayers().forEach { it.level = countdown }

                if (countdown <= 0) {
                    startGame()
                    Bukkit.getOnlinePlayers().forEach { it.level = 0 }

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
        this.createTeams()

        startedAt = System.currentTimeMillis()
        endsAt = System.currentTimeMillis() + (10 * 60 * 1000) + (15 * 1000)
        startingPlayersInt = PlayerManager.players.size
        startingPlayers = PlayerManager.players.keys.toMutableList()
        gameState = GameState.INGAME
        isDisabledMoving = true
        this.clearEntities()

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
            it.getPlayer()?.setPlayerListName("§c§k${it.getPlayer()?.name}")

            it.getKiller()?.disguise()
        }

        // Game items
        PlayerManager.players.values.forEach {
            it.giveStartingItems()

            it.player.playSound(it.player.location, Sound.AMBIENT_CAVE, 1F, 0F)
        }

        // Adding loot to loot chests
        if (lootChests.size > 0) {
            lootChests.forEach { it.loot.add(ItemManager.battery) }
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
                        val dropItem = it.world?.dropItem(it, ItemManager.battery)
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

        Log.info("Game is starting in 15 seconds..")

        this.countdown = 15

        val run: BukkitRunnable = object : BukkitRunnable() {
            override fun run() {
                if (countdown == 0) {
                    isDisabledMoving = false

                    Title("§c§lHRA ZAČÍNÁ", "§7Hodně štěstí!", 0, 60, 20).broadcast()
                    Utils.sendSoundGlobally(Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1F, 1F)
                    Utils.broadcast(true, "Hra začíná!")

                    // TEST: Even when player leaves in 1 minute interval and does nothing - decrease to min of 3
                    if (startingPlayersInt - 1 == 3) {
                        neededGenerators -= 1
                        Utils.broadcast(true, "Počet potřebných generátorů je snížen na $neededGenerators, protože hrají jenom 3 survivoři.")
                    }

                    PlayerManager.survivorTeam.entries.forEach {
                        it.getGamePlayer()?.giveStartingPotionEffects()
                    }

                    Bukkit.getOnlinePlayers().forEach {
                        it.level = 0
                    }

                    // Increase statistics
                    PlayerManager.players.values.forEach { it.gameStats.games_played += 1 }

                    locationTask.runTaskTimer(DeadByDaylight.instance, 0, 20)
                    checkTask.runTaskTimer(DeadByDaylight.instance, 0, 20)

                    Log.success("Game started!")
                    Log.success("Starting players: $startingPlayersInt")

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
        DeadByDaylight.boardUpdateTask = BoardUpdateTask()
        DeadByDaylight.boardUpdateTask.runTaskTimer(DeadByDaylight.instance, 20, 20)
        DeadByDaylight.instance.let { run.runTaskTimer(it, 0L, 20L) }

        booTask.runTaskTimer(DeadByDaylight.instance, 100L, 200L)
    }

    private fun createTeams() {
        // DEBUG: println(PlayerManager.players.values.stream().map { it.toString() }.toArray().joinToString(", ", "[", "]"))
        // 1: Checks if someone has preference for a killer, if so choose randomly from these players
        // If not, choose random player
        val players = PlayerManager.players.values
        if (players.stream().anyMatch { it.gameStats.role_preference == RolePreference.KILLER }) {
            val killer = players.filter { it.gameStats.role_preference == RolePreference.KILLER }.random()
            DeadByDaylight.playerManager.registerKiller(killer)
            players.remove(killer)
        } else if (players.stream().anyMatch { it.gameStats.role_preference == RolePreference.FILL }) {
            val killer = players.filter { it.gameStats.role_preference == RolePreference.FILL }.random()
            DeadByDaylight.playerManager.registerKiller(killer)
            players.remove(killer)
        } else {
            val killer = players.random()
            DeadByDaylight.playerManager.registerKiller(killer)
            players.remove(killer)
        }

        // 2: Assign 4 players with survivor preference
        if (players.stream().filter { it.gameStats.role_preference == RolePreference.SURVIVOR && !it.isAssignedToTeam() }.count().toInt() == 5) {
            // All 5 players want to be survivors, choose randomly
            val collect = players.stream().filter { it.gameStats.role_preference == RolePreference.SURVIVOR }.collect(Collectors.toList())
            val iterator: MutableIterator<GamePlayer> = collect.shuffled().iterator() as MutableIterator<GamePlayer>
            for (i in 1..4) {
                if (!iterator.hasNext()) continue
                val gamePlayer: GamePlayer = iterator.next()

                DeadByDaylight.playerManager.registerSurvivor(gamePlayer)
                iterator.remove()
                players.remove(gamePlayer)
            }
        } else {
            for (gamePlayer in players.stream().filter { it.gameStats.role_preference == RolePreference.SURVIVOR && !it.isAssignedToTeam() }) {
                DeadByDaylight.playerManager.registerSurvivor(gamePlayer)
                players.remove(gamePlayer)
            }
        }

        if (DeadByDaylight.playerManager.areTeamsFilled()) return

        // 3: Fill teams with other players
        if (players.stream().filter { it.gameStats.role_preference == RolePreference.FILL && !it.isAssignedToTeam() }.count().toInt() > 0) {
            // There are players that want to fill some role
            for (gamePlayer in players.stream().filter { it.gameStats.role_preference == RolePreference.FILL && !it.isAssignedToTeam() }) {
                if (!PlayerManager.killerTeam.isFull()) {
                    DeadByDaylight.playerManager.registerKiller(gamePlayer)
                    players.remove(gamePlayer)
                } else {
                    DeadByDaylight.playerManager.registerSurvivor(gamePlayer)
                    players.remove(gamePlayer)
                }
            }
        }

        // 4: Check for remaining players
        if (players.stream().anyMatch { !it.isAssignedToTeam() }) {
            // Some players are not assigned to a team
            for (gamePlayer in players.filter { !it.isAssignedToTeam() }) {
                if (!PlayerManager.killerTeam.isFull()) {
                    DeadByDaylight.playerManager.registerKiller(gamePlayer)
                    players.remove(gamePlayer)
                } else {
                    DeadByDaylight.playerManager.registerSurvivor(gamePlayer)
                    players.remove(gamePlayer)
                }
            }
        }
    }

    fun tryEnd(): Boolean {
        if (gameState != GameState.INGAME) return false
        var endReason: EndReason? = null

        if (PlayerManager.survivorTeam.entries.stream().noneMatch { it.getSurvivor()?.survivalState == SurvivalState.PLAYING } && PlayerManager.survivorTeam.entries.stream().anyMatch { it.getSurvivor()?.escaped!! })
            endReason = EndReason.SURVIVORS_ESCAPED
        else if (PlayerManager.killerTeam.entries.size == 0)
            endReason = EndReason.KILLER_QUIT
        else if (PlayerManager.survivorTeam.entries.size == 0)
            endReason = EndReason.SURVIVORS_QUIT
        else if (endsAt < System.currentTimeMillis())
            endReason = if (this.areGatesOpened())
                EndReason.GATES_CLOSED
            else
                EndReason.TIME_RUN_OUT
        else if ((PlayerManager.killerTeam.entries.any { it.getKiller()?.playerKills!! >= startingPlayersInt - 2 }
                && PlayerManager.survivorTeam.entries.none { it.getSurvivor()?.survivalState == SurvivalState.PLAYING })
                        || PlayerManager.survivorTeam.entries.none { it.getSurvivor()?.survivalState == SurvivalState.PLAYING })
            endReason = EndReason.KILLER_WON

        if (endReason == null)
            return false

        this.endGame(endReason)
        return true
    }

    private fun endGame(endReason: EndReason? = null) {
        if (gameState == GameState.ENDING) return
        endedAt = System.currentTimeMillis()
        gameState = GameState.ENDING

        try {
            runningGeneratorTask.cancel()

        } catch (ignored: IllegalStateException) {
        }

        try {
            checkTask.cancel()
        } catch (ignored: IllegalStateException) {
        }

        try {
            booTask.cancel()
        } catch (ignored: IllegalStateException) {
        }

        CraftLibs.getSqlManager().query("INSERT INTO minigames.dbd_games (players, endReason) VALUES (?, ?);", startingPlayers.joinToString(separator = ",", prefix = "[", postfix = "]"), endReason?.name)

        Bukkit.getScheduler().cancelTask(DeadByDaylight.boardUpdateTask.taskId)
        DeadByDaylight.boardManager.updateAllPlayers()

        this.clearEntities()

        lootChests.forEach { it.close() }

        PlayerManager.players.values.forEach {
            if (it is Killer) {
                it.gameStats.playtime += System.currentTimeMillis() - startedAt
                if (it.playerKills >= 2 || (endReason == EndReason.TIME_RUN_OUT && DeadByDaylight.gameManager.gates.all { it1 -> !it1.isOpened })) {
                    it.gameStats.killer_wins += 1

                    it.player.sendMessage("§e+30 CC §8[Výhra]")
                    it.coins += 30
                }
                it.player.setPlayerListName(null)
            } else if (it is Survivor) {
                if (it.survivalState == SurvivalState.PLAYING) {
                    it.gameStats.playtime += System.currentTimeMillis() - startedAt
                }
                it.removeBlindness()
            }
            it.giveCoins()

            it.player.gameMode = GameMode.SPECTATOR
            it.player.playSound(it.player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.AMBIENT, .5F, 0F)

            it.updateStats()
        }

        TextComponentBuilder("").broadcast()
        TextComponentBuilder("§c§lKONEC HRY", true).broadcast()
        when (endReason) {
            EndReason.GATES_CLOSED -> TextComponentBuilder("§8[Brány se zavřeli]", true).broadcast()
            EndReason.TIME_RUN_OUT -> TextComponentBuilder("§8[Vypršel čas]", true).broadcast()
            EndReason.SURVIVORS_ESCAPED -> TextComponentBuilder("§8[Survivoři utekli]", true).broadcast()
            EndReason.KILLER_WON -> TextComponentBuilder("§8[Killer všechny zabil]", true).broadcast()
            EndReason.KILLER_QUIT -> TextComponentBuilder("§8[Killer opustil hru]", true).broadcast()
            EndReason.SURVIVORS_QUIT -> TextComponentBuilder("§8[Survivoři opustili hru]", true).broadcast()
        }
        TextComponentBuilder("").broadcast()
        TextComponentBuilder("§fDěkujeme za zahrání naší", true).broadcast()
        TextComponentBuilder("§fHalloween minihry.", true).broadcast()
        TextComponentBuilder("").broadcast()
        TextComponentBuilder("§8Made with <3 by iGniSsak", true).broadcast()
        TextComponentBuilder("").broadcast()

        Log.info("Game ends (endReason = $endReason).")

        Utils.broadcast(true, "Za 15 sekund se restartuje server.")

        gates.forEach { it.close() }

        var i = 10

        object : BukkitRunnable() {
            override fun run() {
                if (i == 0) {
                    this.cancel()
                    return
                }

                this@GameManager.fireworkLocations.forEach {
                    this@GameManager.spawnRandomFirework(it)
                }
                i--
            }

        }.runTaskTimer(DeadByDaylight.instance, 0, 20)

        Bukkit.getScheduler().runTaskLater(DeadByDaylight.instance, Runnable {
            Utils.broadcast(true, "Prosíme tě o vyplnení feedbacku v dotazníku: https://igniss.typeform.com/to/f4cMsMo2")
        }, 40)

        DeadByDaylight.instance.let { Bukkit.getScheduler().runTaskLater(it, this::shutDown, 15 * 20) }
    }

    private fun shutDown() {
        Bukkit.getOnlinePlayers().forEach { it.kickPlayer("Tento server se restartuje.") }

        Bukkit.getScheduler().runTaskLater(DeadByDaylight.instance, Bukkit::shutdown, 200)
    }

    fun playerLeftIngame(survivor: Survivor) {
        // This is run before player is unregistered from team
        if (PlayerManager.survivorTeam.size in 3..4) { // If there were 3-4 survivors
            if (System.currentTimeMillis() - startedAt < 60000) {
                if (this.neededGenerators > 3 && generators.none { it.contributors.contains(survivor.player.name) }) {
                    this.neededGenerators -= 1
                    Utils.broadcast(true, "AFK hráč se odpojil, počet potřebných generátorů byl znížen na ${this.neededGenerators}.")
                }
            }
        }
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

    fun getGameTimeFormatted(): String {
        if (endsAt - System.currentTimeMillis() < 0)
            return "00:00"
        return DurationFormatUtils.formatDuration((endsAt - System.currentTimeMillis()), "mm:ss")
    }

    fun getLootChestAt(location: Location): LootChest? = lootChests.find { it.location.block.location == location }

    fun getGeneratorAt(location: Location): Generator? = generators.find { it.location.block.location == location }

    fun clearEntities() {
        lobbyLocation?.world?.entities?.forEach { if (it !is Player) it.remove() }
    }

    fun areGatesOpened(): Boolean = gates.all { it.isOpened }

    fun areEnoughGeneratorsPowered(): Boolean = generators.count { it.isActivated() } >= this.neededGenerators

    private fun spawnRandomFirework(loc: Location) {
        val colors: Array<Color> = arrayOf(Color.AQUA, Color.BLUE, Color.FUCHSIA, Color.GRAY, Color.GREEN, Color.LIME, Color.MAROON, Color.NAVY, Color.OLIVE, Color.ORANGE, Color.PURPLE, Color.RED, Color.SILVER, Color.TEAL, Color.WHITE, Color.YELLOW)
        val firework = loc.world!!.spawnEntity(loc, EntityType.FIREWORK) as Firework
        val fireworkMeta = firework.fireworkMeta
        val random = Random()

        val effect = FireworkEffect.builder()
                .flicker(random.nextBoolean())
                .withColor(colors.random())
                .withFade(colors.random())
                .with(FireworkEffect.Type.values().random())
                .trail(true).build()

        fireworkMeta.addEffect(effect)
        fireworkMeta.power = random.nextInt(2) + 1

        firework.fireworkMeta = fireworkMeta
    }

    companion object {
        val runningGeneratorTask: RunningGeneratorTask = RunningGeneratorTask()
    }

}