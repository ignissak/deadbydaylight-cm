package net.ignissak.deadbydaylight.command

import cz.craftmania.craftcore.spigot.messages.chat.ChatInfo
import cz.craftmania.craftlibs.utils.actions.ConfirmAction
import net.ignissak.deadbydaylight.DeadByDaylight
import net.ignissak.deadbydaylight.game.modules.Generator
import net.ignissak.deadbydaylight.game.modules.LootChest
import net.ignissak.deadbydaylight.utils.LocationUtils
import net.ignissak.deadbydaylight.utils.TextComponentBuilder
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.stream.Collectors

class AdminCommands : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true
        DeadByDaylight.playerManager.getGamePlayer(sender) ?: return true

        if (!sender.hasPermission("halloweenminigame.admin")) return true

        when (command.name.toLowerCase()) {
            "forcestart" -> {
                if (Bukkit.getOnlinePlayers().size < 2) {
                    ChatInfo.error(sender, "Musí být aspoň 2 hráči připojeni.")
                    return true
                }
                val action = ConfirmAction.Builder()
                        .setPlayer(sender)
                        .setDelay(15L)
                        .generateIdentifier()
                        .addComponent { action -> TextComponentBuilder("&aKlikni zde pokud chceš force startnout hru.").setTooltip("Force startnout hru").setPerformedCommand(action.confirmationCommand).component }
                        .setRunnable { DeadByDaylight.gameManager.forceStart() }
                        .setExpireRunnable { p -> ChatInfo.warning(p, "Force startnutí hry vypršelo.") }
                        .build()
                action.sendTextComponents()
                return true
            }
            "setlobby" -> {
                DeadByDaylight.gameManager.lobbyLocation = sender.location
                DeadByDaylight.instance.config.set("locations.lobby", LocationUtils.formatLocation(sender.location, true))
                ChatInfo.success(sender, "Byla změnena poloha lobby.")
            }
            "addkillerspawn" -> {
                DeadByDaylight.gameManager.killerLocations.add(sender.location)
                DeadByDaylight.instance.config.set("locations.killer", DeadByDaylight.gameManager.killerLocations.stream().map { LocationUtils.formatLocation(it) }.collect(Collectors.toList()))
                ChatInfo.success(sender, "Byla přidána spawn lokace pro killera.")
            }
            "addsurvivorspawn" -> {
                DeadByDaylight.gameManager.survivorLocations.add(sender.location)
                DeadByDaylight.instance.config.set("locations.survivor", DeadByDaylight.gameManager.survivorLocations.stream().map { LocationUtils.formatLocation(it) }.collect(Collectors.toList()))
                ChatInfo.success(sender, "Byla přidána spawn lokace pro survivora.")
            }
            "adddrop" -> {
                DeadByDaylight.gameManager.drops.add(sender.location)
                DeadByDaylight.instance.config.set("locations.drops", DeadByDaylight.gameManager.drops.stream().map { LocationUtils.formatLocation(it, false) }.collect(Collectors.toList()))
                ChatInfo.success(sender, "Byla přidána spawn lokace pro drop.")
            }
            "addgenerator" -> {
                val targetBlock: Block = sender.getTargetBlock(null, 5)
                println(targetBlock.toString())
                DeadByDaylight.gameManager.generators.add(Generator(targetBlock.location))
                DeadByDaylight.instance.config.set("locations.generators", DeadByDaylight.gameManager.generators.stream().map { LocationUtils.formatLocation(it.location, false) }.collect(Collectors.toList()))
                ChatInfo.success(sender, "Byla přidána lokace generátoru.")
            }
            "addlootchest" -> {
                val targetBlock: Block = sender.getTargetBlock(null, 5)
                println(targetBlock.toString())
                DeadByDaylight.gameManager.lootChests.add(LootChest(targetBlock.location))
                DeadByDaylight.instance.config.set("locations.chests", DeadByDaylight.gameManager.lootChests.stream().map { LocationUtils.formatLocation(it.location, false) }.collect(Collectors.toList()))
                ChatInfo.success(sender, "Byla přidána lokace loot chestky.")
            }
            "addfireworkspawn" -> {
                DeadByDaylight.gameManager.fireworkLocations.add(sender.location)
                DeadByDaylight.instance.config.set("locations.fireworks", DeadByDaylight.gameManager.survivorLocations.stream().map { LocationUtils.formatLocation(it) }.collect(Collectors.toList()))
                ChatInfo.success(sender, "Byla přidána spawn lokace pro firework.")
            }
        }
        return true
    }
}