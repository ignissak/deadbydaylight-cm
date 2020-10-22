package net.ignissak.deadbydaylight.game.modules

// BUG: For some reason, the stats will reset after each new game.
data class GameStats(var games_played: Int = 0,
                     var killer_kills: Int = 0,
                     var killer_downs: Int = 0,
                     var killer_wins: Int = 0,
                     var killer_hits: Int = 0,
                     var survivor_wins: Int = 0,
                     var survivor_generators_powered: Int = 0,
                     var survivor_fuels_filled: Int = 0,
                     var survivor_players_revived: Int = 0,
                     var playtime: Long = 0L
)