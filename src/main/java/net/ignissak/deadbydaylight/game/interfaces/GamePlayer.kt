package net.ignissak.deadbydaylight.game.interfaces

import com.google.gson.Gson
import cz.craftmania.crafteconomy.api.CraftCoinsAPI
import cz.craftmania.craftlibs.CraftLibs
import me.libraryaddict.disguise.DisguiseAPI
import net.citizensnpcs.api.CitizensAPI
import net.ignissak.deadbydaylight.DeadByDaylight
import net.ignissak.deadbydaylight.game.PlayerManager
import net.ignissak.deadbydaylight.game.modules.GameStats
import net.ignissak.deadbydaylight.game.modules.Killer
import net.ignissak.deadbydaylight.game.modules.Survivor
import net.ignissak.deadbydaylight.utils.Log
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffectType
import xyz.upperlevel.spigot.book.BookUtil

abstract class GamePlayer(val player: Player) {

    lateinit var gameStats: GameStats
    private val gson: Gson = Gson()
    var rolePreference: RolePreference = RolePreference.FILL
    var coins: Int = 0
    private var gotCoins: Boolean = false

    init {
        try {
            // Loading statistics
            CraftLibs.getSqlManager().query("SELECT * FROM dbd_players WHERE uuid = ?;", player.uniqueId.toString()).thenAccept { dbRows ->
                if (dbRows.isEmpty()) {
                    Log.info("Inserting data for ${player.name}")
                    CraftLibs.getSqlManager().query("INSERT INTO dbd_players (uuid, nickname) VALUES (?, ?);", player.uniqueId.toString(), player.name)
                    this.gameStats = GameStats()
                } else {
                    Log.info("Loading data for ${player.name}")
                    this.gameStats = gson.fromJson(dbRows[0].getString("stats"), GameStats::class.java)

                    println(gameStats)
                }
            }
        } catch (e: Exception) {
            Log.fatal("Could not fetch data for ${player.name}, player was disconnected.")
            player.kickPlayer("Nepodařilo se získat tvé data z databáze, byl jsi vyhozen.")
        }
    }

    fun onJoin() {
        this.unDisguise()

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
        this.giveBook()

        DeadByDaylight.gameManager.tryStart()
    }

    private fun giveBook() {
        val book = BookUtil.writtenBook()
                .author("CraftMania")
                .title("§9Manuál §7(klikni pravým)")
                .pages(
                        // First page
                        BookUtil.PageBuilder()
                                .add(
                                        BookUtil.TextBuilder.of("Základní informace")
                                                .color(ChatColor.GOLD)
                                                .build()
                                )
                                .newLine()
                                .newLine()
                                .add("Tato hra je 1 versus 4, kde se killer snaží zabít survivory a survivoři se snaží utéct z mapy. Pro více informací k rolím přejdi na další stránky.")
                                .build(),
                        // Second page
                        BookUtil.PageBuilder()
                                .add(
                                        BookUtil.TextBuilder.of("Hraní za killera")
                                                .color(ChatColor.RED)
                                                .build()
                                )
                                .newLine()
                                .newLine()
                                .add("Jako killer musíš zabít všechny survivory. Survivoři mají maximálně 2 životy, které si mohou doplnit bándáží. Tvoje sekyra dává 2 damage (1 srdíčko). Hráč neumře ihned, ale leží na zemi po dobu 20 sekund, během kterých ho můžou ostatní oživit.")
                                .build(),
                        BookUtil.PageBuilder()
                                .add("Také můžeš přitáhnout survivory k sobě pomocí hooku. Obě dvě tvoje zbraně mají po použití cooldown a po zasažení survivora sekyrou mají speed boost.")
                                .build(),
                        // Third page
                        BookUtil.PageBuilder()
                                .add(
                                        BookUtil.TextBuilder.of("Hraní za survivora")
                                                .color(ChatColor.DARK_GREEN)
                                                .build()
                                )
                                .newLine()
                                .newLine()
                                .add("Jako survivor musíš spolu s ostatními opravit aspoň 5 generátorů a utéct skrz jednu ze 2 brán na mapě. Baterie do generátorů jsou schované v truhlách nebo se spawnují po zemi.")
                                .build(),
                        BookUtil.PageBuilder()
                                .add("V těchto truhlách můžeš získat bandáž na vyléčení a zapalovač, abys mohl lépe vidět, protože budeš mít blindness. Pokud nějaký spoluhráč bude zraněn, tak dostaneš kompas, který tě k němu bude navigovat a můžeš ho shiftováním oživit.")
                                .build(),
                )
                .build()
        player.inventory.setItem(1, book)
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
        println(gameStats)
        CraftLibs.getSqlManager().query("UPDATE dbd_players SET stats = ? WHERE uuid = ?;", gson.toJson(gameStats), player.uniqueId.toString()).whenComplete { _, _ -> Log.info("Updated statistics for ${player.name}.") }
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

    private fun unDisguise() {
        DisguiseAPI.undisguiseToAll(this.player)
    }

    fun removeBlindness() {
        player.removePotionEffect(PotionEffectType.BLINDNESS)
    }

    override fun toString(): String {
        return "GamePlayer(player=$player, gameStats=$gameStats, rolePreference=$rolePreference, coins=$coins)"
    }

}