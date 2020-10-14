package net.ignissak.deadbydaylight.event

import cz.craftmania.craftcore.spigot.messages.chat.ChatInfo
import net.ignissak.deadbydaylight.DeadByDaylight
import net.ignissak.deadbydaylight.game.ItemManager
import net.ignissak.deadbydaylight.game.PlayerManager
import net.ignissak.deadbydaylight.game.interfaces.GamePlayer
import net.ignissak.deadbydaylight.game.interfaces.GameState
import net.ignissak.deadbydaylight.game.interfaces.SurvivalState
import net.ignissak.deadbydaylight.game.modules.Killer
import net.ignissak.deadbydaylight.game.modules.Survivor
import net.ignissak.deadbydaylight.utils.getGamePlayer
import net.ignissak.deadbydaylight.utils.getPlayer
import org.bukkit.GameMode
import org.bukkit.entity.Animals
import org.bukkit.entity.Monster
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockFadeEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack

class GeneralListener : Listener {

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        val player = event.player
        val gamePlayer: GamePlayer = player.getGamePlayer() ?: return

        if (DeadByDaylight.gameManager.isDisabledMoving) {
            if (event.to?.blockX != event.from.blockX ||
                    event.to?.blockY != event.from.blockY ||
                    event.to?.blockZ != event.from.blockZ) {
                player.teleport(event.from)
                return
            }
        } else if (gamePlayer is Survivor) {
            if (gamePlayer.survivalState == SurvivalState.DYING) {
                player.teleport(event.from)
                return
            }
        }

        if (DeadByDaylight.gameManager.gameState == GameState.LOBBY && event.to?.y!! < 0) {
            DeadByDaylight.gameManager.lobbyLocation?.let { player.teleport(it) }
        }
    }

    @EventHandler
    fun onChat(event: AsyncPlayerChatEvent) {
        if (DeadByDaylight.gameManager.gameState != GameState.INGAME) {
            event.format = "§c${event.player.name}§8: §7%2\$s"
            return
        }

        val craftPlayer: GamePlayer = DeadByDaylight.playerManager.getGamePlayer(event.player) ?: return

        if (craftPlayer is Killer) {
            event.isCancelled = true
            ChatInfo.error(event.player, "Survivoři ti nerozumí.")
        } else if (craftPlayer is Survivor) {
            PlayerManager.survivorTeam.entries.forEach { it.getPlayer()?.sendMessage(String.format((if (craftPlayer.survivalState == SurvivalState.SPECTATING) "§7[Mrtvý] " else "") + "§c${event.player.name}§8: §7%2\$s", event.player, event.message)) }
            DeadByDaylight.instance.logger.info(String.format((if (craftPlayer.survivalState == SurvivalState.SPECTATING) "§7[Mrtvý] " else "") + "§c${event.player.name}§8: §7%2\$s", event.player, event.message))

            event.isCancelled = true
        }
    }

    @EventHandler
    fun onSwap(event: PlayerSwapHandItemsEvent) {
        if (!(event.player.isOp && event.player.gameMode == GameMode.CREATIVE)) event.isCancelled = true
    }

    @EventHandler
    fun onDrop(event: PlayerDropItemEvent) {
        if (!(event.player.isOp && event.player.gameMode == GameMode.CREATIVE)) event.isCancelled = true
    }

    @EventHandler
    fun onHungerChange(event: FoodLevelChangeEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun onBlockFade(event: BlockFadeEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun onEntitySpawn(event: EntitySpawnEvent) {
        if (event.entity is Animals || event.entity is Monster)
            event.isCancelled = true
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.clickedInventory?.type == InventoryType.PLAYER && !event.whoClicked.isOp) event.isCancelled = true
    }

    @EventHandler
    fun onClick(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK && event.action != Action.RIGHT_CLICK_AIR) return
        val item: ItemStack = event.item ?: return

        if (item.isSimilar(ItemManager.role)) {
            event.player.performCommand("role")

            event.isCancelled = true
        }
    }
}

