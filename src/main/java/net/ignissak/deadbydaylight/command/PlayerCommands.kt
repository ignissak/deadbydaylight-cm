package net.ignissak.deadbydaylight.command

import cz.craftmania.craftcore.spigot.inventory.builder.SmartInventory
import cz.craftmania.craftcore.spigot.messages.chat.ChatInfo
import net.ignissak.deadbydaylight.DeadByDaylight
import net.ignissak.deadbydaylight.game.interfaces.GameState
import net.ignissak.deadbydaylight.game.menu.RolePreferenceMenu
import net.ignissak.deadbydaylight.game.menu.StatsMenu
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType

class PlayerCommands : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true
        val gamePlayer = DeadByDaylight.playerManager.getGamePlayer(sender) ?: return true

        when (command.name.toLowerCase()) {
            "role" -> {
                if (DeadByDaylight.gameManager.gameState == GameState.INGAME || DeadByDaylight.gameManager.gameState == GameState.ENDING) {
                    ChatInfo.error(sender, "Preference na roli si můžeš vybrát jenom v Lobby.")
                    return true
                }
                SmartInventory.builder().provider(RolePreferenceMenu()).title("Preference").type(InventoryType.HOPPER).build().open(sender)
                return true
            }
            "stats" -> {
                if (DeadByDaylight.gameManager.gameState == GameState.INGAME || DeadByDaylight.gameManager.gameState == GameState.ENDING) {
                    ChatInfo.error(sender, "Statistiky můžeš přehlížet jenom v Lobby.")
                    return true
                }
                SmartInventory.builder().provider(StatsMenu()).title("Statistiky").type(InventoryType.HOPPER).build().open(sender)
                return true
            }
        }
        return true
    }
}