package net.ignissak.deadbydaylight.game.objects

import org.bukkit.Location
import org.simpleyaml.configuration.file.YamlFile

class Arena(val name: String) {

    val arenaFile: YamlFile
    val maxSurvivors: Int
    val minSurvivors: Int
    val neededGenerators: Int
    val batteryLoots: List<Location>? = null
    val bandageLoots: List<Location>? = null
    val lightLoots: List<Location>? = null
    val killerSpawns: List<Location>? = null
    val survivorSpawns: List<Location>? = null
    val generators: List<Generator>? = null
    val lootChests: List<LootChest>? = null

    init {
        arenaFile = YamlFile("/arenas/$name.yml")
        maxSurvivors = arenaFile.getInt("players.survivors", 4)
        minSurvivors = arenaFile.getInt("players.min_survivors", 3)
        neededGenerators = arenaFile.getInt("settings.needed_generators")
        for (chests in arenaFile.getStringList("locations.chests")) {
        }
    }
}