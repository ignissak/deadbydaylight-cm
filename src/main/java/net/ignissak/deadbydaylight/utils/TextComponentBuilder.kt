package net.ignissak.deadbydaylight.utils

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Team
import java.util.*
import java.util.function.Consumer

/**
 * This library simplifies creating [TextComponent].
 * All strings are automatically parsed with [ChatColor.translateAlternateColorCodes] and [TextComponent.fromLegacyText].
 * This object is [Cloneable].
 * @version 1.0.0
 * @author jacobbordas / igniss
 */
class TextComponentBuilder : Cloneable {
    /**
     * @return [TextComponent]
     */
    val component: TextComponent

    /**
     * Creates TextComponentBuilder with desired message. This message is parsed with [ChatColor.translateAlternateColorCodes]
     * and with [TextComponent.fromLegacyText] so all formats and colours are parsed automatically to [TextComponent].
     * @param message Message to parse to TextComponent
     */
    constructor(message: String?) {
        component = BaseComponentMerger(*TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message!!))).output(false)
    }

    /**
     * Creates TextComponentBuilder with desired message. This message is parsed with [ChatColor.translateAlternateColorCodes]
     * and with [TextComponent.fromLegacyText] so all formats and colours are parsed automatically to [TextComponent].
     * @param message Message to parse to TextComponent
     * @param centered Whether this message should be centered in [TextComponent]
     */
    constructor(message: String?, centered: Boolean) {
        component = BaseComponentMerger(*TextComponent.fromLegacyText(if (centered) StringUtils.getCenteredMessage(ChatColor.translateAlternateColorCodes('&', message!!)) else ChatColor.translateAlternateColorCodes('&', message!!))).output(false)
    }

    /**
     * Sets performed command to the TextComponent.
     * @param command Command you want to se to be performed on click.
     */
    fun setPerformedCommand(command: String): TextComponentBuilder {
        var command = command
        if (!command.startsWith("/")) command = "/$command"
        component.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, command)
        return this
    }

    /**
     * Sets tooltip to the TextComponent.
     * @param tooltip Tooltip you want to set to the TextComponent on hover.
     */
    fun setTooltip(tooltip: String?): TextComponentBuilder {
        component.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(tooltip))
        return this
    }

    /**
     * Sets suggested command to the TextComponent.
     * @param command Command you want to be suggested on click.
     */
    fun suggestCommand(command: String): TextComponentBuilder {
        var command = command
        if (!command.startsWith("/")) command = "/$command"
        if (!command.endsWith(" ")) command = "$command "
        component.clickEvent = ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command)
        return this
    }

    /**
     * Sends this TextComponent to player.
     * @param player Target player.
     */
    fun send(player: Player) {
        player.spigot().sendMessage(component)
    }

    /**
     * Sends this TextComponent to multiple players.
     * @param players Target players.
     */
    fun send(vararg players: Player) {
        for (player in players) {
            player.spigot().sendMessage(component)
        }
    }

    /**
     * Broadcasts this TextComponent to all players.
     */
    fun broadcast() {
        Bukkit.getOnlinePlayers().forEach { player: Player -> player.spigot().sendMessage(component) }
    }

    /**
     * Sends this TextComponent to Scoreboard team.
     * @param team Target team.
     */
    fun send(team: Team) {
        team.entries.forEach(Consumer { entry: String ->
            Bukkit.getPlayer(entry)?.let { send(it) }
        })
    }

    @Throws(CloneNotSupportedException::class)
    override fun clone(): Any {
        return super.clone()
    }
}