package net.ignissak.deadbydaylight.game

import cz.craftmania.craftcore.spigot.builders.items.ItemBuilder
import net.ignissak.deadbydaylight.utils.Utils
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

object ItemManager {

    val battery: ItemStack
        get() = Utils.createHead("fuel", "8847dbf6-6648-47f2-bb4f-667903125a9e", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGQyYzE5YjQ0MjU0MTM1MWE2YjgxZWViNmNiZWY0MTk2NmZmYjdkYmU0YzEzNmI4N2Y1YmFmOWQxNGEifX19",
                "§eBaterie §7(klikni pravym)", "", "§7Kliknutím pravym s baterii", "§7na generátor pro vložení.", "")!!

    val flash: ItemStack
        get() = ItemBuilder(Material.FLINT_AND_STEEL, 1)
                .setName("§9Zapalovač §7(podrž pro použití)")
                .setLore("", "§7Podržením v ruce", "§7rozvítíš zapalovač.", "")
                .hideAllFlags()
                .build()

    val bandage: ItemStack
        get() = ItemBuilder(Material.PAPER, 1)
                .setName("§cBandáž §7(klikni pravym)")
                .setLore("", "§7Kliknutím pravym se", "§7vyléčíš.", "")
                .hideAllFlags()
                .build()

    val axe: ItemStack
        get() = ItemBuilder(Material.IRON_AXE, 1)
                .setName("§cSekera §7(klikni levym)")
                .setLore("", "§7Kliknutím levym na", "§7survivora ho zasáhneš.", "")
                .hideAllFlags()
                .build()

    val hook: ItemStack
        get() {
            val itemStack = ItemBuilder(Material.FISHING_ROD, 1)
                    .setName("§aPrut §7(klikni pravym)")
                    .setLore("", "§7Kliknutím pravym vyhodíš", "§7prut, při zasažení survivora", "§7ho přitáhneš k sobě.", "")
                    .hideAllFlags()
                    .build()
            val itemMeta = itemStack.itemMeta
            itemMeta?.isUnbreakable = true
            itemMeta?.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
            itemStack.itemMeta = itemMeta
            return itemStack
        }

    val role: ItemStack
        get() = ItemBuilder(Material.FLINT, 1)
                .setName("§ePreference §7(klikni pravym)")
                .setLore("", "§7Klikni s itemem pro zobrazení", "§7menu na výběru preference role.", "")
                .hideAllFlags()
                .build()

    val stats: ItemStack
        get() = ItemBuilder(Material.BOOK, 1)
                .setName("§9Statistiky §7(klikni pravym)")
                .setLore("", "§7Klikni s itemem pro zobrazení", "§7menu s tvými statistikami.", "")
                .hideAllFlags()
                .build()
}