package net.ignissak.deadbydaylight.utils

import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import java.util.*

/**
 * Additional library for [TextComponentBuilder]. With this library you can easily
 * merge several [BaseComponent] together.
 * @version 1.0.0
 * @author jacobbordas / igniss
 */
class BaseComponentMerger {
    private val textComponentList: MutableList<TextComponent>

    /**
     * Creates empty BaseComponentMerger.
     */
    constructor() {
        textComponentList = ArrayList()
    }

    /**
     * @param components Array of [BaseComponent] to be merged together
     */
    constructor(vararg components: BaseComponent?) {
        textComponentList = ArrayList()
        for (textComponent in components) {
            textComponentList.add(TextComponent(textComponent))
        }
    }

    /**
     * @param withSpaces Whether output [TextComponent] should be with spaces between each [TextComponent] merged.
     * @return Final merged [TextComponent]
     */
    fun output(withSpaces: Boolean): TextComponent {
        val iterator: ListIterator<TextComponent> = textComponentList.listIterator()
        val textComponent = iterator.next()
        while (iterator.hasNext()) {
            if (withSpaces) textComponent.addExtra(" ")
            textComponent.addExtra(iterator.next())
        }
        return textComponent
    }

    /**
     * Adds [BaseComponent] to list.
     * @param components BaseComponents to add
     */
    fun addComponent(vararg components: BaseComponent?) {
        for (textComponent in components) {
            textComponentList.add(TextComponent(textComponent))
        }
    }
}