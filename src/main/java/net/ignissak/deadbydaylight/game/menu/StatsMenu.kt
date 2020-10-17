package net.ignissak.deadbydaylight.game.menu

import cz.craftmania.craftcore.spigot.builders.items.ItemBuilder
import cz.craftmania.craftcore.spigot.inventory.builder.ClickableItem
import cz.craftmania.craftcore.spigot.inventory.builder.content.InventoryContents
import cz.craftmania.craftcore.spigot.inventory.builder.content.InventoryProvider
import net.ignissak.deadbydaylight.DeadByDaylight
import net.ignissak.deadbydaylight.utils.Constants
import org.apache.commons.lang3.time.DurationFormatUtils
import org.bukkit.Material
import org.bukkit.entity.Player

class StatsMenu : InventoryProvider {

    override fun update(player: Player?, contents: InventoryContents?) {}

    override fun init(player: Player, contents: InventoryContents) {
        val gamePlayer = DeadByDaylight.playerManager.getGamePlayer(player) ?: return

        val stats = gamePlayer.gameStats

        val killerStats = ItemBuilder(Material.IRON_AXE)
                .setName("${Constants.killerColor}Statistiky za killera")
                .setLore("§7Killy: §f${stats.killer_kills}",
                "§7Hity: §f${stats.killer_hits}",
                "§7Ssmrtelné zasažení: §f${stats.killer_downs}",
                "§7Výhry: §f${stats.killer_wins}")
                .hideAllFlags()
                .build()

        val survivorStats = ItemBuilder(Material.LEATHER_BOOTS)
                .setName("${Constants.survivorColor}Statistiky za survivora")
                .setLore("§7Spuštěné generátory: §f${stats.survivor_generators_powered}",
                "§7Vložené baterie: §f${stats.survivor_fuels_filled}",
                "§7Výlečení spoluhráčů: §f${stats.survivor_players_revived}",
                "§7Výher: §f${stats.survivor_wins}")
                .hideAllFlags()
                .build()
        
        val general = ItemBuilder(Material.PAPER)
                .setName("§9Všeobecné statistiky")
                .setLore("§7Playtime: §f${DurationFormatUtils.formatDuration(stats.playtime, "HH'h' mm'm' ss's'")}",
                "§7Odehráných her: §f${stats.games_played}")
                .hideAllFlags()
                .build()

        contents.set(0, 1, ClickableItem.empty(general))
        contents.set(0, 2, ClickableItem.empty(killerStats))
        contents.set(0, 3, ClickableItem.empty(survivorStats))
    }
}