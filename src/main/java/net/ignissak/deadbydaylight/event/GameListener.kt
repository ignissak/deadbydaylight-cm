package net.ignissak.deadbydaylight.event

import cz.craftmania.craftcore.spigot.messages.Title
import cz.craftmania.craftcore.spigot.messages.chat.ChatInfo
import net.ignissak.deadbydaylight.DeadByDaylight
import net.ignissak.deadbydaylight.api.event.GeneratorPowerUpEvent
import net.ignissak.deadbydaylight.game.GameManager
import net.ignissak.deadbydaylight.game.ItemManager
import net.ignissak.deadbydaylight.game.PlayerManager
import net.ignissak.deadbydaylight.game.interfaces.GamePlayer
import net.ignissak.deadbydaylight.game.interfaces.GameState
import net.ignissak.deadbydaylight.game.interfaces.SurvivalState
import net.ignissak.deadbydaylight.game.modules.Generator
import net.ignissak.deadbydaylight.game.modules.Killer
import net.ignissak.deadbydaylight.game.modules.LootChest
import net.ignissak.deadbydaylight.game.modules.Survivor
import net.ignissak.deadbydaylight.game.task.BossBarTask
import net.ignissak.deadbydaylight.game.task.SurvivorRevivingSurvivorTask
import net.ignissak.deadbydaylight.utils.TextComponentBuilder
import net.ignissak.deadbydaylight.utils.Utils
import net.ignissak.deadbydaylight.utils.getGamePlayer
import net.ignissak.deadbydaylight.utils.getSurvivor
import net.minecraft.server.v1_16_R2.PacketPlayOutCollect
import org.apache.commons.lang3.time.DurationFormatUtils
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.*
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.meta.Damageable
import org.bukkit.potion.PotionEffectType


class GameListener : Listener {

    /**
     * General [PlayerInteractEvent] that handles clicking outside [GameState.INGAME]
     * These clicks are cancelled if player is not OP and not in Creative.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    fun onClick(event: PlayerInteractEvent) {
        if (DeadByDaylight.gameManager.gameState != GameState.INGAME) {
            if (event.item?.type == Material.WRITTEN_BOOK) return
            if (event.player.isOp && event.player.gameMode == GameMode.CREATIVE) return
            event.isCancelled = true
            return
        }
    }

    @EventHandler
    fun onBandageUse(event: PlayerInteractEvent) {
        val player = event.player
        val gamePlayer = DeadByDaylight.playerManager.getGamePlayer(player) ?: return

        if (event.action == Action.RIGHT_CLICK_BLOCK || event.action == Action.RIGHT_CLICK_AIR) {
            // Bandage
            if (gamePlayer !is Survivor) return
            if (gamePlayer.survivalState != SurvivalState.PLAYING) return
            if (!player.inventory.itemInMainHand.isSimilar(ItemManager.bandage)) return
            // Ignore full health survivor
            if (player.health >= 4.0) return

            // Survivor is already healing
            if (gamePlayer.healing) return

            gamePlayer.showHealthTitle()
            gamePlayer.healing = true

            DeadByDaylight.instance.let {
                Bukkit.getScheduler().runTaskLater(it, Runnable {
                    player.health += 1.0
                    player.inventory.setItem(1, null)

                    gamePlayer.showHealthTitle()
                    gamePlayer.healing = false
                }, 10)
            }

        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onBlockClick(event: PlayerInteractEvent) {
        if (DeadByDaylight.gameManager.gameState != GameState.INGAME) return

        //println(event.action)
        //println(event.clickedBlock)

        val player = event.player
        val gamePlayer = DeadByDaylight.playerManager.getGamePlayer(player) ?: return

        // Accept only right block clicks
        if (event.action != Action.RIGHT_CLICK_BLOCK) return

        if (event.hand != EquipmentSlot.HAND) return
        if (event.action == Action.RIGHT_CLICK_BLOCK) {
            if (event.clickedBlock == null) return
            if (gamePlayer !is Survivor) {
                if (gamePlayer.player.inventory.itemInMainHand.type == Material.FISHING_ROD) return
                event.isCancelled = true
                return
            }
            // Ignore spectators
            if (gamePlayer.survivalState != SurvivalState.PLAYING) {
                event.isCancelled = true
                return
            }

            val clickedBlock: Block = event.clickedBlock!!
            when (clickedBlock.type) {
                // Click on loot chest
                Material.CHEST -> {
                    val lootChest: LootChest? = DeadByDaylight.gameManager.getLootChestAt(clickedBlock.location)

                    if (lootChest == null) {
                        event.isCancelled = true
                        return
                    }

                    if (!lootChest.opened) lootChest.open()
                    event.isCancelled = true
                }
                // Click on generator
                Material.BLAST_FURNACE -> {
                    if (player.inventory.itemInMainHand.type != Material.PLAYER_HEAD) {
                        event.isCancelled = true
                        return
                    }
                    val generator: Generator? = DeadByDaylight.gameManager.getGeneratorAt(clickedBlock.location)

                    if (generator == null) {
                        event.isCancelled = true
                        return
                    }

                    if (generator.increaseProgress(1, gamePlayer)) {
                        Title("§a+25%", "", 5, 10, 5).send(player)
                    } else {
                        ChatInfo.error(player, "Tento generátor již běží!")
                    }
                    event.isCancelled = true
                }
                else -> {
                    event.isCancelled = true
                }
            }
        }
    }

    @EventHandler
    fun onHookHit(event: ProjectileHitEvent) {
        if (DeadByDaylight.gameManager.gameState != GameState.INGAME) return
        if (event.hitEntity == null) return
        if (event.entity.type != EntityType.FISHING_HOOK) return
        if (event.hitEntity !is Player) return

        val projectile = event.entity
        if (projectile.shooter == null) return

        val player: Player = projectile.shooter as Player
        val gamePlayer = DeadByDaylight.playerManager.getGamePlayer(player) ?: return

        if (gamePlayer !is Killer) return

        val caughtPlayer: Player = event.hitEntity as Player
        val caughtGamePlayer = DeadByDaylight.playerManager.getGamePlayer(caughtPlayer) ?: return

        if (caughtGamePlayer !is Survivor) return
        val survivor: Survivor = caughtGamePlayer
        // Player caught

        gamePlayer.player.inventory.setItem(1, null)
        Title("§c§lCooldown", "§f6 sekund", 10, 20, 10).send(gamePlayer.player)

        DeadByDaylight.instance.let {
            Bukkit.getScheduler().runTaskLater(it, Runnable {
                gamePlayer.player.inventory.setItem(1, ItemManager.hook)
            }, 120)
        }

        survivor.hook(gamePlayer)
    }

    @EventHandler(ignoreCancelled = true)
    fun onPickupLast(event: EntityPickupItemEvent) {
        if (event.entity !is Player) return
        val player = event.entity as Player

        player.inventory.contents.forEach {
            if (it == null) return
            if (it.type == Material.AIR) return
            if (it.amount > 1) {
                it.amount = 1
            }
        }
    }

    @EventHandler
    fun onPickup(event: EntityPickupItemEvent) {
        if (event.entity !is Player) {
            event.isCancelled = true
            return
        }
        if (DeadByDaylight.gameManager.gameState != GameState.INGAME || DeadByDaylight.gameManager.isDisabledMoving) {
            event.isCancelled = true
            return
        }
        val player: Player = event.entity as Player
        val gamePlayer: GamePlayer = DeadByDaylight.playerManager.getGamePlayer(player) ?: return
        if (gamePlayer !is Survivor) {
            event.isCancelled = true
            return
        }
        if (gamePlayer.survivalState != SurvivalState.PLAYING) return
        val item = event.item
        val itemStack = item.itemStack.clone()

        event.isCancelled = true

        if (itemStack.type == Material.PLAYER_HEAD) {
            if (!player.inventory.contains(Material.PLAYER_HEAD)) {
                val packet = PacketPlayOutCollect(item.entityId, player.entityId, 1)
                (player as CraftPlayer).handle.playerConnection.sendPacket(packet)
                item.remove()
                player.inventory.setItem(0, itemStack)

                //gamePlayer.coins += 1
                //player.sendMessage("§e+1CC §8[Nalezení baterie]")
                return
            } else event.isCancelled = true
        } else if (itemStack.isSimilar(ItemManager.bandage)) {
            if (!player.inventory.contains(Material.PAPER)) {
                val packet = PacketPlayOutCollect(item.entityId, player.entityId, 1)
                (player as CraftPlayer).handle.playerConnection.sendPacket(packet)
                item.remove()
                player.inventory.setItem(1, itemStack)

                //gamePlayer.coins += 1
                //player.sendMessage("§e+1CC §8[Nalezení bandáže]")
                return
            } else event.isCancelled = true
        } else if (itemStack.type == Material.FLINT_AND_STEEL) {
            if (!player.inventory.contains(Material.FLINT_AND_STEEL)) {
                val packet = PacketPlayOutCollect(item.entityId, player.entityId, 1)
                (player as CraftPlayer).handle.playerConnection.sendPacket(packet)
                item.remove()
                player.inventory.setItem(2, itemStack)

                //gamePlayer.coins += 1
                //player.sendMessage("§e+1CC §8[Nalezení zapalovače]")
                return
            } else if (player.inventory.contains(Material.FLINT_AND_STEEL)) {
                val flintAndSteel = player.inventory.getItem(2) ?: return
                val damageable = flintAndSteel.itemMeta as Damageable

                if (damageable.hasDamage()) {
                    val packet = PacketPlayOutCollect(item.entityId, player.entityId, 1)
                    (player as CraftPlayer).handle.playerConnection.sendPacket(packet)
                    item.remove()
                    player.inventory.setItem(2, itemStack)

                    //gamePlayer.coins += 1
                    //player.sendMessage("§e+1CC §8[Nalezení zapalovače]")
                    return
                }
            } else event.isCancelled = true
        } else event.isCancelled = true
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        if (event.entity !is Player) return
        if (DeadByDaylight.gameManager.gameState != GameState.INGAME || DeadByDaylight.gameManager.isDisabledMoving) {
            event.isCancelled = true
            event.damage = .0
        }
        if (event.cause == EntityDamageEvent.DamageCause.FALL
                || event.cause == EntityDamageEvent.DamageCause.STARVATION
                || event.cause == EntityDamageEvent.DamageCause.VOID) {
            event.damage = .0
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onDamageByPlayer(event: EntityDamageByEntityEvent) {
        if (event.entity !is Player) return
        if (event.damager !is Player) return

        val entityGamePlayer: GamePlayer = DeadByDaylight.playerManager.getGamePlayer(event.entity as Player) ?: return
        val damagerGamePlayer: GamePlayer = DeadByDaylight.playerManager.getGamePlayer(event.damager as Player)
                ?: return

        if (entityGamePlayer is Killer) {
            event.isCancelled = true
            return
        }

        if (entityGamePlayer is Survivor && damagerGamePlayer is Killer && DeadByDaylight.gameManager.gameState == GameState.INGAME) {
            val survivor: Survivor = entityGamePlayer
            val killer: Killer = damagerGamePlayer

            if (!damagerGamePlayer.player.inventory.itemInMainHand.isSimilar(ItemManager.axe)) {
                event.isCancelled = true
                return
            }
            if (survivor.survivalState != SurvivalState.PLAYING) {
                event.damage = .0
                event.isCancelled = true
                return
            }

            event.damage = .0
            val down: Boolean = survivor.hit(killer)

            if (down) {
                killer.coins += 3
                killer.player.sendMessage("§e+3 CC §8[Smrtelné zranění survivora]")
            } else {
                killer.coins += 3
                killer.player.sendMessage("§e+3 CC §8[Zranení survivora]")
            }

            killer.player.inventory.setItem(0, null)
            Title("§c§lCooldown", "§f5 sekund", 10, 20, 10).send(killer.player)

            DeadByDaylight.instance.let {
                Bukkit.getScheduler().runTaskLater(it, Runnable {
                    killer.player.inventory.setItem(0, ItemManager.axe)
                }, 100)
            }
        }
    }

    @EventHandler
    fun onDespawn(event: ItemDespawnEvent) {
        if (DeadByDaylight.gameManager.gameState == GameState.INGAME) event.isCancelled = true
    }

    @EventHandler
    fun onRegenerateHealth(event: EntityRegainHealthEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun onRegenerateFood(event: FoodLevelChangeEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun onSneak(event: PlayerToggleSneakEvent) {
        val player: Player = event.player
        val gamePlayer: GamePlayer = player.getGamePlayer() ?: return
        if (gamePlayer !is Survivor) return

        if (gamePlayer.survivalState != SurvivalState.PLAYING) return
        if (!DeadByDaylight.playerManager.isAnySurvivorDying()) return
        val survivorsDying = DeadByDaylight.playerManager.getSurvivorsDying()

        println(survivorsDying)

        if (survivorsDying.stream().anyMatch { it?.previousLocation?.distance(gamePlayer.player.location)!! < 2 }) {
            val optionalSurvivorToRevive = survivorsDying.stream().filter { it?.player?.location?.distance(gamePlayer.player.location)!! < 2 }.findFirst()
            if (!optionalSurvivorToRevive.isPresent) return

            val survivorToRevive = optionalSurvivorToRevive.get()

            val survivorRevivingSurvivorTask = SurvivorRevivingSurvivorTask(gamePlayer, survivorToRevive)
            DeadByDaylight.gameManager.revivingTasks.add(survivorRevivingSurvivorTask)
            DeadByDaylight.instance.let { survivorRevivingSurvivorTask.runTaskTimer(it, 0, 10) }
        }
    }

    @EventHandler
    fun onGeneratorPowerUp(ignored: GeneratorPowerUpEvent) {
        try {
            DeadByDaylight.instance.let { GameManager.runningGeneratorTask.runTaskTimer(it, 0L, 40L) }
        } catch (ignored: IllegalStateException) {
        }
        if (DeadByDaylight.gameManager.generators.count { it.isActivated() } == DeadByDaylight.gameManager.neededGenerators) {

            TextComponentBuilder("").broadcast()
            TextComponentBuilder("§7Potřebný počet generátorů bylo opraveno,", true).broadcast()
            TextComponentBuilder("§ebrány se otevřou za 30 vteřin!", true).broadcast()
            TextComponentBuilder("").broadcast()

            PlayerManager.survivorTeam.entries.forEach { it.getSurvivor()?.removeBlindness() }

            Bukkit.getScheduler().runTaskLater(DeadByDaylight.instance, Runnable {
                DeadByDaylight.gameManager.gates.forEach { it1 -> it1.open() }
                DeadByDaylight.gameManager.gatesOpenedAt = System.currentTimeMillis()

                // TEST: Decrease game time to 2 minutes
                // TEST: After 2 minutes shut down doors and end game with time out
                TextComponentBuilder("").broadcast()
                TextComponentBuilder("§a§lBrány se otevřely!", true).broadcast()
                TextComponentBuilder("").broadcast()

                Bukkit.getOnlinePlayers().forEach {
                    it.playSound(it.location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.AMBIENT, .5F, .5F)
                }

                if (DeadByDaylight.gameManager.endsAt - System.currentTimeMillis() > 1000 * 120)
                    // Decrease time
                    DeadByDaylight.gameManager.endsAt = System.currentTimeMillis() + 1000 * 120

                Utils.broadcast(true, "Za ${DurationFormatUtils.formatDuration(DeadByDaylight.gameManager.endsAt - System.currentTimeMillis(), "mm'm' 'a' ss's'")} se brány zamknou a hra skončí.")

                DeadByDaylight.gameManager.bossBarTask = BossBarTask()
                DeadByDaylight.gameManager.bossBarTask?.runTaskTimer(DeadByDaylight.instance, 0, 20)
            }, 30 * 20)
        }
    }

    @EventHandler
    fun onLightUseEvent(event: PlayerItemHeldEvent) {
        val player = event.player
        val gamePlayer = player.getGamePlayer() ?: return

        if (DeadByDaylight.gameManager.gameState != GameState.INGAME || DeadByDaylight.gameManager.isDisabledMoving) return
        if (DeadByDaylight.gameManager.gates.all { it.isOpened }) return
        if (DeadByDaylight.gameManager.generators.count { it.isActivated() } >= DeadByDaylight.gameManager.neededGenerators) return

        if (gamePlayer !is Survivor) return
        if (gamePlayer.survivalState != SurvivalState.PLAYING) return
        val newItem = player.inventory.getItem(event.newSlot)
        if (newItem == null) {
            try {
                gamePlayer.giveBlindness()
                gamePlayer.survivorFlashTask.cancel()
            } catch (e: Exception) {
                if (e is java.lang.IllegalStateException) return
                e.printStackTrace()
            }
        } else {
            val itemMeta = player.inventory.getItem(event.newSlot)?.itemMeta ?: return
            if (itemMeta.displayName == "§9Zapalovač §7(podrž pro použití)") {
                if (DeadByDaylight.gameManager.areGatesOpened()) return
                if (player.hasPotionEffect(PotionEffectType.SPEED)) return
                gamePlayer.holdingFlash()
            }
        }
    }

    @EventHandler
    fun onLightPickUp(event: EntityPickupItemEvent) {
        if (event.entity !is Player) return

        val gamePlayer = (event.entity as Player).getGamePlayer() ?: return

        if (gamePlayer !is Survivor) {
            event.isCancelled = true
            return
        }

        if (gamePlayer.player.inventory.itemInMainHand == event.item.itemStack) {
            if (event.item.itemStack.isSimilar(ItemManager.flash)) {
                try {
                    gamePlayer.giveBlindness()
                    gamePlayer.survivorFlashTask.cancel()
                } catch (e: Exception) {
                    if (e is java.lang.IllegalStateException) return
                    e.printStackTrace()
                }
            }
        }
    }

    @EventHandler
    fun onInteract(event: InventoryClickEvent) {
        if (!event.whoClicked.isOp) event.isCancelled = true
    }
}