package net.ignissak.deadbydaylight.game.task

import net.ignissak.deadbydaylight.game.interfaces.GameRegion
import net.ignissak.deadbydaylight.game.interfaces.SurvivalState
import net.ignissak.deadbydaylight.game.modules.Survivor
import net.ignissak.deadbydaylight.utils.getGamePlayer
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

class LocationTask : BukkitRunnable() {

    override fun run() {
        Bukkit.getOnlinePlayers().forEach {
            val gamePlayer = it.getGamePlayer() ?: return@forEach

            if (gamePlayer is Survivor && gamePlayer.survivalState != SurvivalState.PLAYING) return@forEach

            val region = GameRegion.getRegionAt(it.location) ?: return@forEach

            it.spigot().sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText(region.title))
        }
    }

}