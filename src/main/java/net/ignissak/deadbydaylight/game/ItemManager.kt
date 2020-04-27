package net.ignissak.deadbydaylight.game

import net.ignissak.deadbydaylight.Main.Companion.localeManager
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.stream.Collectors

object ItemManager {
    val battery: ItemStack
        get() {
            val itemStack = ItemStack(Material.REDSTONE, 1)
            val itemMeta = itemStack.itemMeta
            itemMeta!!.setDisplayName(ChatColor.translateAlternateColorCodes('&', localeManager!!.localeFile.getString("items.battery.name")))
            itemMeta.lore = localeManager!!.localeFile.getStringList("items.battery.lore").stream().map { s: String? -> ChatColor.translateAlternateColorCodes('&', s!!) }.collect(Collectors.toList())
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
            itemStack.itemMeta = itemMeta
            return itemStack
        }

    val flash: ItemStack
        get() {
            val itemStack = ItemStack(Material.FLINT_AND_STEEL, 1)
            val itemMeta = itemStack.itemMeta
            itemMeta!!.setDisplayName(ChatColor.translateAlternateColorCodes('&', localeManager!!.localeFile.getString("items.flash.name")))
            itemMeta.lore = localeManager!!.localeFile.getStringList("items.flash.lore").stream().map { s: String? -> ChatColor.translateAlternateColorCodes('&', s!!) }.collect(Collectors.toList())
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
            itemStack.itemMeta = itemMeta
            return itemStack
        }

    val bandage: ItemStack
        get() {
            val itemStack = ItemStack(Material.PAPER, 1)
            val itemMeta = itemStack.itemMeta
            itemMeta!!.setDisplayName(ChatColor.translateAlternateColorCodes('&', localeManager!!.localeFile.getString("items.bandage.name")))
            itemMeta.lore = localeManager!!.localeFile.getStringList("items.bandage.lore").stream().map { s: String? -> ChatColor.translateAlternateColorCodes('&', s!!) }.collect(Collectors.toList())
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
            itemStack.itemMeta = itemMeta
            return itemStack
        }
}