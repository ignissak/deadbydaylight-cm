package net.ignissak.deadbydaylight.game.menu

import cz.craftmania.craftcore.spigot.builders.items.ItemBuilder
import cz.craftmania.craftcore.spigot.inventory.builder.ClickableItem
import cz.craftmania.craftcore.spigot.inventory.builder.content.InventoryContents
import cz.craftmania.craftcore.spigot.inventory.builder.content.InventoryProvider
import cz.craftmania.craftcore.spigot.messages.chat.ChatInfo
import net.ignissak.deadbydaylight.DeadByDaylight
import net.ignissak.deadbydaylight.game.interfaces.RolePreference
import net.ignissak.deadbydaylight.utils.Constants
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class RolePreferenceMenu : InventoryProvider {

    override fun update(player: Player, contents: InventoryContents) {}


    // TEST
    override fun init(player: Player, contents: InventoryContents) {
        val gamePlayer = DeadByDaylight.playerManager.getGamePlayer(player) ?: return

        val killerNotSelected: ItemStack = ItemBuilder(Material.IRON_AXE, 1)
                .setName("${Constants.killerColor}Killer")
                .setLore("", "§7Zvolit preferenci na roli killera.", "", "§eZvolit.")
                .hideAllFlags()
                .build()

        val killerSelected: ItemStack = ItemBuilder(Material.IRON_AXE, 1)
                .setName("${Constants.killerColor}Killer")
                .setLore("", "§7Zvolit preferenci na roli killera.", "", "§aZvoleno.")
                .setGlowing()
                .hideAllFlags()
                .build()

        val survivorNotSelected: ItemStack = ItemBuilder(Material.LEATHER_BOOTS, 1)
                .setName("${Constants.survivorColor}Survivor")
                .setLore("", "§7Zvolit preferenci na roli survivora.", "", "§eZvolit.")
                .hideAllFlags()
                .build()

        val survivorSelected: ItemStack = ItemBuilder(Material.LEATHER_BOOTS, 1)
                .setName("${Constants.survivorColor}Survivor")
                .setLore("", "§7Zvolit preferenci na roli survivora.", "", "§aZvoleno.")
                .setGlowing()
                .hideAllFlags()
                .build()

        val fillNotSelected: ItemStack = ItemBuilder(Material.HOPPER, 1)
                .setName("${Constants.fillColor}Fill")
                .setLore("", "§7Zvolit preferenci na roli, kde", "§7budou chybět hráči.", "", "§eZvolit.")
                .hideAllFlags()
                .build()

        val fillSelected: ItemStack = ItemBuilder(Material.HOPPER, 1)
                .setName("${Constants.fillColor}Fill")
                .setLore("", "§7Zvolit preferenci na roli, kde", "§7budou chybět hráči.", "", "§aZvoleno.")
                .setGlowing()
                .hideAllFlags()
                .build()

        when (gamePlayer.rolePreference) {
            RolePreference.KILLER -> {
                contents.set(0, 0, ClickableItem.empty(killerSelected))

                contents.set(0, 2, ClickableItem.of(survivorNotSelected) {
                    gamePlayer.rolePreference = RolePreference.SURVIVOR
                    player.closeInventory()

                    ChatInfo.success(player, "Preference změněna na survivora.")
                })

                contents.set(0, 4, ClickableItem.of(fillNotSelected) {
                    gamePlayer.rolePreference = RolePreference.FILL
                    player.closeInventory()

                    ChatInfo.success(player, "Preference změněna na doplnění týmů.")
                })
                return
            }
            RolePreference.SURVIVOR -> {
                contents.set(0, 0, ClickableItem.of(killerNotSelected) {
                    gamePlayer.rolePreference = RolePreference.KILLER
                    player.closeInventory()

                    ChatInfo.success(player, "Preference změněna na killera.")
                })

                contents.set(0, 2, ClickableItem.empty(survivorSelected))

                contents.set(0, 4, ClickableItem.of(fillNotSelected) {
                    gamePlayer.rolePreference = RolePreference.FILL
                    player.closeInventory()

                    ChatInfo.success(player, "Preference změněna na doplnění týmů.")
                })
                return
            }
            RolePreference.FILL -> {
                contents.set(0, 0, ClickableItem.of(killerNotSelected) {
                    gamePlayer.rolePreference = RolePreference.KILLER
                    player.closeInventory()

                    ChatInfo.success(player, "Preference změněna na killera.")
                })

                contents.set(0, 2, ClickableItem.of(survivorNotSelected) {
                    gamePlayer.rolePreference = RolePreference.SURVIVOR
                    player.closeInventory()

                    ChatInfo.success(player, "Preference změněna na survivora.")
                })

                contents.set(0, 4, ClickableItem.empty(fillSelected))
                return
            }
        }
    }
}