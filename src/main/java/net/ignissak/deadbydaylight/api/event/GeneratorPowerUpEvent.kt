package net.ignissak.deadbydaylight.api.event

import net.ignissak.deadbydaylight.game.modules.Generator
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class GeneratorPowerUpEvent(val generator: Generator): Event() {

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }

}