package net.ignissak.deadbydaylight.game.modules

import com.gmail.filoghost.holographicdisplays.api.Hologram
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI
import net.ignissak.deadbydaylight.DeadByDaylight
import net.ignissak.deadbydaylight.api.event.GeneratorPowerUpEvent
import net.ignissak.deadbydaylight.game.interfaces.GamePlayer
import net.ignissak.deadbydaylight.utils.TextComponentBuilder
import net.ignissak.deadbydaylight.utils.getGamePlayer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import java.util.*

class Generator(val location: Location) {

    // maximum 4
    var progress = 0
    var hologram: Hologram = HologramsAPI.createHologram(DeadByDaylight.instance, location.clone().add(.5, 2.0, .5))
    var contributors: MutableList<String> = arrayListOf()

    init {
        hologram.clearLines()
        hologram.appendTextLine("§c\u274C Generátor \u274C")
        hologram.appendTextLine(getPercentage())
    }

    fun increaseProgress(value: Int = 1, gamePlayer: GamePlayer): Boolean {
        if (this.progress < 4) {
            this.progress += value

            this.contributors.add(gamePlayer.player.name)
            gamePlayer.coins += 1
            gamePlayer.gameStats.survivor_fuels_filled += 1
            gamePlayer.player.inventory.setItem(0, null)
            location.world?.playSound(location, Sound.ENTITY_CHICKEN_EGG, .5F, .0F)

            gamePlayer.player.sendMessage("§e+1CC §8[Doplnění paliva do generátoru]")
            this.updateHologram()

            if (this.progress == 4)
                this.powerUp()
            return true
        }
        return false
    }

    private fun updateHologram() {
        hologram.clearLines()
        if (isActivated()) hologram.appendTextLine("§aGenerátor \u2713")
        else hologram.appendTextLine("§cGenerátor \u274C")
        hologram.appendTextLine(getPercentage())
    }

    fun isActivated(): Boolean = progress == 4;

    private fun powerUp() {
        // TODO
        Bukkit.getPluginManager().callEvent(GeneratorPowerUpEvent(this))

        location.world?.strikeLightningEffect(location)

        TextComponentBuilder("").broadcast()
        TextComponentBuilder("&a&lGenerátor opraven!", true).broadcast()
        TextComponentBuilder("&8[]") // TODO: Location
        TextComponentBuilder("").broadcast()
        TextComponentBuilder("&7Generátor opravili:", true).broadcast()
        TextComponentBuilder("&f${this.contributors.distinct().joinToString(", ") }", true).broadcast()
        TextComponentBuilder("").broadcast()

        this.contributors.distinct().forEach { it ->
            if (it.getGamePlayer() == null) return
            it.getGamePlayer()!!.gameStats.survivor_generators_powered += 1
            val coinsToAdd: Int = 5 * this.contributors.count { it == it.getGamePlayer()!!.player.name }
            it.getGamePlayer()!!.coins += coinsToAdd
            it.getGamePlayer()!!.player.sendMessage("§e+${coinsToAdd}CC §8[Opravení generátoru]")
        }
    }

    private fun getPercentage(): String {
        return when (progress) {
            0 -> "§4\u25A0§7${"\u25A0".repeat(8)}"
            1 -> "§6${"\u25A0".repeat(3)}§7${"\u25A0".repeat(6)}"
            2 -> "§e${"\u25A0".repeat(5)}§7${"\u25A0".repeat(4)}"
            3 -> "§a${"\u25A0".repeat(7)}§7${"\u25A0".repeat(2)}"
            4 -> "§2${"\u25A0".repeat(9)}"
            else -> "§4\u25A0§7${"\u25A0".repeat(8)}"
        }
    }

    override fun toString(): String {
        return "Generator(location=$location, progress=$progress)"
    }


}