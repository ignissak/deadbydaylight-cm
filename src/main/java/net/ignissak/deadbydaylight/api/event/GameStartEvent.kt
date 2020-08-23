package net.ignissak.deadbydaylight.api.event

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class GameStartEvent : Event() {

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