package net.ignissak.deadbydaylight

import net.ignissak.deadbydaylight.database.Database
import net.ignissak.deadbydaylight.database.DatabaseData
import net.ignissak.deadbydaylight.database.SQLManager
import net.ignissak.deadbydaylight.database.types.MySQL
import net.ignissak.deadbydaylight.database.types.SQLite
import net.ignissak.deadbydaylight.locale.LocaleManager
import org.bukkit.plugin.java.JavaPlugin
import org.simpleyaml.configuration.file.YamlFile

class Main : JavaPlugin() {
    var databaseData: DatabaseData? = null
        private set
    var database: Database? = null
        private set

    override fun onEnable() {
        instance = this
        if (!configFile.exists()) {
            try {
                configFile.createNewFile(true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        configFile.options().copyDefaults(true)
        databaseData = DatabaseData()
        database = if (databaseData!!.type == "mysql") {
            MySQL()
        } else {
            SQLite()
        }
        database!!.setup()
        sQLManager = SQLManager()
        localeManager = LocaleManager()
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    companion object {
        @JvmStatic
        var instance: Main? = null
            private set
        var sQLManager: SQLManager? = null
            private set
        @JvmStatic
        val configFile = YamlFile("config.yml")
        @JvmStatic
        var localeManager: LocaleManager? = null
            private set

    }
}