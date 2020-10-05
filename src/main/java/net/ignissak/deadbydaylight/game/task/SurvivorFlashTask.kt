package net.ignissak.deadbydaylight.game.task

import cz.craftmania.craftcore.spigot.messages.chat.ChatInfo
import net.ignissak.deadbydaylight.game.modules.Survivor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.scheduler.BukkitRunnable

class SurvivorFlashTask(private val survivor: Survivor): BukkitRunnable() {

    override fun run() {
        if (!this.survivor.player.inventory.contains(Material.FLINT_AND_STEEL) || this.survivor.player.inventory.itemInMainHand.type != Material.FLINT_AND_STEEL) {
            this.survivor.giveBlindness()

            this.cancel()
            return
        }

        this.survivor.light()
        val flashlight: Damageable = this.survivor.player.inventory.itemInMainHand.itemMeta as Damageable

        if (flashlight.damage >= 64) {
            ChatInfo.warning(this.survivor.player, "Vyprchal ti tvůj zapalovač.")

            this.survivor.player.playSound(this.survivor.player.location, Sound.ENTITY_ITEM_BREAK, SoundCategory.AMBIENT, .5F, 1.5F)
            this.survivor.player.inventory.setItem(2, null)
            this.survivor.giveBlindness()

            this.cancel()
            return
        }

        flashlight.damage += 1
        this.survivor.player.inventory.itemInMainHand.itemMeta = flashlight as ItemMeta
    }
}