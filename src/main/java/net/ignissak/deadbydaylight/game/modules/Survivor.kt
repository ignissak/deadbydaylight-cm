package net.ignissak.deadbydaylight.game.modules

import cz.craftmania.craftcore.spigot.builders.items.ItemBuilder
import cz.craftmania.craftcore.spigot.messages.Title
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.npc.NPC
import net.citizensnpcs.npc.skin.SkinnableEntity
import net.citizensnpcs.util.PlayerAnimation
import net.ignissak.deadbydaylight.DeadByDaylight
import net.ignissak.deadbydaylight.game.ItemManager
import net.ignissak.deadbydaylight.game.PlayerManager
import net.ignissak.deadbydaylight.game.interfaces.GamePlayer
import net.ignissak.deadbydaylight.game.interfaces.GameRegion
import net.ignissak.deadbydaylight.game.interfaces.GameState
import net.ignissak.deadbydaylight.game.interfaces.SurvivalState
import net.ignissak.deadbydaylight.game.task.SurvivorDyingTask
import net.ignissak.deadbydaylight.game.task.SurvivorFlashTask
import net.ignissak.deadbydaylight.utils.*
import org.bukkit.*
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class Survivor(player: Player) : GamePlayer(player) {

    var survivalState: SurvivalState = SurvivalState.PLAYING
    var survivorDyingTask: SurvivorDyingTask = SurvivorDyingTask(this)
    var survivorFlashTask: SurvivorFlashTask = SurvivorFlashTask(this)

    var previousLocation: Location? = null
    var endedAt: Long? = null
    var revivedPlayers = 0
    var healing = false
    var escaped = false

    val npc: NPC = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "corpse-${player.name}")

    init {
        npc.data().remove(NPC.PLAYER_SKIN_UUID_METADATA)
        npc.data().setPersistent(NPC.PLAYER_SKIN_UUID_METADATA, player.name)
        npc.data().setPersistent(NPC.PLAYER_SKIN_USE_LATEST, false)
        npc.data().setPersistent(NPC.NAMEPLATE_VISIBLE_METADATA, false)

        npc.spawn(DeadByDaylight.gameManager.dumpLocation)
        (npc.entity as SkinnableEntity).setSkinName(player.name, true)
        npc.hide()
    }

    override fun giveStartingItems() {
        player.inventory.clear()
        player.inventory.setItem(2, ItemManager.flash)
        player.inventory.setItem(1, ItemManager.bandage)
    }

    override fun giveStartingPotionEffects() {
        player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0, false, false, false))
    }

    fun isBeingRevived(): Boolean = DeadByDaylight.gameManager.revivingTasks.stream().anyMatch { it.survivorToBeRevived.player == this.player }

    /**
     * @return True if player was downed
     */
    fun hit(killer: Killer): Boolean {
        if (player.health <= 2.0) {
            down(killer)
            return true
        }

        player.damage(2.0)
        this.giveSpeed()

        killer.gameStats.killer_hits += 1
        killer.playerHits += 1

        return false
    }

    fun hook(killer: Killer) {
        val vector = killer.player.location.toVector().subtract(player.location.toVector())
        vector.multiply(.6)
        vector.y += .25

        player.velocity = vector
        this.giveSpeed(0)
    }

    private fun down(killer: Killer) {
        killer.gameStats.killer_downs += 1
        killer.playerDowns += 1

        this.survivalState = SurvivalState.DYING

        val ending = DeadByDaylight.gameManager.tryEnd()

        if (ending) {
            this.die(killer)
        } else {
            player.allowFlight = true
            player.isFlying = true

            previousLocation = player.location.clone()
            player.removePotionEffect(PotionEffectType.BLINDNESS)
            player.health = .5

            TextComponentBuilder("").send(PlayerManager.survivorTeam)
            TextComponentBuilder("§c${player.name} byl smrtelně zraněn!", true).send(PlayerManager.survivorTeam)
            TextComponentBuilder("§8[${GameRegion.getRegionAt(previousLocation!!)?.title}]", true).send(PlayerManager.survivorTeam)
            TextComponentBuilder("").send(PlayerManager.survivorTeam)

            val npcSpawnLocation = previousLocation!!.clone()
            npcSpawnLocation.yaw = 0F
            npcSpawnLocation.pitch = 0F

            npc.show(npcSpawnLocation)
            PlayerAnimation.SLEEP.play(npc.entity as Player)

            this.hideFromOthers()

            this.survivorDyingTask = SurvivorDyingTask(this)
            DeadByDaylight.instance.let { this.survivorDyingTask.runTaskTimer(it, 0, 20) }

            // Teleport above corpse
            val locToTeleport = player.location.clone().add(.0, 1.0, .0)
            locToTeleport.yaw = 0F
            locToTeleport.pitch = 90F
            player.teleport(locToTeleport)
        }

        PlayerManager.survivorTeam.entries.forEach { it.getSurvivor()?.compassLeadingToDyingSurvivor() }
    }

    fun revive(revivedBy: Survivor) {
        this.survivalState = SurvivalState.PLAYING
        this.showToOthers()

        player.health = 3.0
        player.level = 0
        player.allowFlight = false
        player.isFlying = false

        this.giveBlindness()

        Utils.broadcast(true, "${revivedBy.player.name} oživil ${player.name}.")

        npc.hide()

        player.playSound(player.location, Sound.ENTITY_CAT_AMBIENT, SoundCategory.AMBIENT, .5F, 1F)
        previousLocation?.let { player.teleport(it) }

        Title("§a§lOŽIVEN", "§fOživil tě §7" + revivedBy.player.name, 5, 20, 5).send(player)

        PlayerManager.survivorTeam.entries.forEach { it.getSurvivor()?.compassLeadingToDyingSurvivor() }
    }

    fun die(killer: Killer) {
        killer.gameStats.killer_kills += 1
        killer.playerKills += 1
        this.survivalState = SurvivalState.SPECTATING

        TextComponentBuilder(DeadByDaylight.prefix + "${player.name} zemřel.").broadcast()
        Title("§c§lGAME OVER", "Tady pro tebe hra končí.", 10, 60, 10).send(player)

        PlayerManager.killerTeam.entries.forEach {
            it.getKiller()?.coins = it.getKiller()?.coins?.plus(1)!!
            it.getPlayer()?.sendMessage("§e+1 CC §8[Zabití survivora]")
            it.getPlayer()?.location?.let { it1 -> it.getPlayer()?.playSound(it1, Sound.ENTITY_CAT_AMBIENT, SoundCategory.AMBIENT, 1F, 1F) }
        }

        player.gameMode = GameMode.SPECTATOR
        player.teleport(player.location.clone().add(.0, 3.0, .0))
        endedAt = System.currentTimeMillis()

        gameStats.playtime += endedAt!! - DeadByDaylight.gameManager.startedAt

        this.destroyNPC()

        this.giveCoins()
        this.updateStats()
        this.removeBlindness()
        this.showToOthers()

        val ending = DeadByDaylight.gameManager.tryEnd()

        if (!ending) {
            player.playSound(player.location, Sound.BLOCK_ANVIL_PLACE, SoundCategory.AMBIENT, .5F, 0F)

            player.inventory.contents.forEach {
                if (it != null) {
                    if (it.type == Material.AIR) return@forEach
                    if (it.type == Material.COMPASS) return@forEach
                    val dropItem = previousLocation?.world?.dropItem(previousLocation!!, it)

                    dropItem?.customName = it.itemMeta!!.displayName.split(" ")[0]
                    dropItem?.isCustomNameVisible = true
                }
            }
        }

        PlayerManager.survivorTeam.entries.forEach { it.getSurvivor()?.compassLeadingToDyingSurvivor() }
    }

    fun win() {
        this.survivalState = SurvivalState.SPECTATING
        this.endedAt = System.currentTimeMillis()
        this.escaped = true

        this.gameStats.survivor_wins += 1
        this.gameStats.playtime += endedAt!! - DeadByDaylight.gameManager.startedAt

        TextComponentBuilder(DeadByDaylight.prefix + "${player.name} utekl.").broadcast()
        Title("§a§lUTEKL JSI", "Skvělá práce.", 10, 60, 10).send(player)

        this.coins += 5
        this.player.sendMessage("§e+5 CC §8[Útěk]")
        this.player.gameMode = GameMode.SPECTATOR
        this.player.inventory.clear()

        this.removeBlindness()

        this.npc.destroy()
        CitizensAPI.getNPCRegistry().deregister(this.npc)

        this.giveCoins()
        this.updateStats()

        DeadByDaylight.gameManager.tryEnd()
    }

    private fun compassLeadingToDyingSurvivor() {
        if (survivalState != SurvivalState.PLAYING) return
        if (!DeadByDaylight.playerManager.isAnySurvivorDying() || DeadByDaylight.gameManager.gameState != GameState.INGAME) {
            // No survivor is dying, remove compass
            player.inventory.remove(Material.COMPASS)
            return
        }

        val survivor = DeadByDaylight.playerManager.getSurvivorsDying().first() ?: return

        val compassItem = ItemBuilder(Material.COMPASS, 1)
                .setName("§9Kompas §7(${survivor.player.name})")
                .setLore("", "§7Tento kompas tě zavede", "§7za přeživším, který umíra.", "")
                .hideAllFlags()
                .build()

        /*val compassMeta = compassItem.itemMeta!! as CompassMeta

        compassMeta.lodestone = survivor.previousLocation
        compassMeta.isLodestoneTracked = true

        compassItem.itemMeta = compassMeta*/

        player.compassTarget = survivor.previousLocation!!
        player.inventory.setItem(8, compassItem)
    }

    fun showHealthTitle() {
        val title: String = if (player.health <= 1.0) {
            "§c\u2665§7\u2665"
        } else if (player.health > 1.0 && player.health <= 2.0) {
            "§4\u2665§7\u2665"
        } else if (player.health > 2.0 && player.health <= 3.0) {
            "§4\u2665§c\u2665"
        } else {
            "§4\u2665§4\u2665"
        }

        Title(title, "", 0, 20, 5).send(player)
    }

    fun giveBlindness() {
        if (DeadByDaylight.gameManager.areGatesOpened()) return
        if (survivalState != SurvivalState.PLAYING) return
        if (player.hasPotionEffect(PotionEffectType.SPEED)) return
        player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0, false, false, false))
    }

    fun giveSpeed(amplifier: Int = 4) {
        player.removePotionEffect(PotionEffectType.BLINDNESS)
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 80, amplifier, false, false, false))

        Bukkit.getScheduler().runTaskLater(DeadByDaylight.instance, Runnable {
            this.giveBlindness()
        }, 81)
    }

    fun light() {
        player.removePotionEffect(PotionEffectType.BLINDNESS)
    }

    fun holdingFlash() {
        if (DeadByDaylight.gameManager.gates.all { it.isOpened }) return
        this.survivorFlashTask = SurvivorFlashTask(this)
        this.survivorFlashTask.runTaskTimer(DeadByDaylight.instance, 0, 6)
    }

    fun destroyNPC() {
        this.npc.destroy()
        CitizensAPI.getNPCRegistry().deregister(this.npc)
    }

    override fun toString(): String {
        return "Survivor(nick=${player.name}, survivalState=$survivalState)"
    }


}