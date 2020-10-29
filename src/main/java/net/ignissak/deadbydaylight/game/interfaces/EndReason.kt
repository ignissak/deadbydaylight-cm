package net.ignissak.deadbydaylight.game.interfaces

enum class EndReason {

    // TEST: Add killer killed everyone, survivors escaped etc
    KILLER_WON,
    SURVIVORS_ESCAPED,
    TIME_RUN_OUT,
    KILLER_QUIT,
    SURVIVORS_QUIT

}