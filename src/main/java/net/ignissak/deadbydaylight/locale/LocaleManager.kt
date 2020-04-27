package net.ignissak.deadbydaylight.locale

import net.ignissak.deadbydaylight.Main
import net.ignissak.deadbydaylight.Main.Companion.instance
import net.ignissak.deadbydaylight.utils.Log.error
import net.ignissak.deadbydaylight.utils.Log.fatal
import net.ignissak.deadbydaylight.utils.Log.info
import net.ignissak.deadbydaylight.utils.Log.success
import org.bukkit.Bukkit
import org.simpleyaml.configuration.file.YamlFile

class LocaleManager {

    var localeFile: YamlFile

    init {
        val submittedLocale = Main.configFile.getString("locale")
        info("Loading locale: /lang/$submittedLocale.yml")
        localeFile = YamlFile("/lang/$submittedLocale")
        if (!localeFile.exists()) {
            error("Could not load /lang/$submittedLocale.yml.")
            info("Loading default locale...")
            localeFile = YamlFile("/lang/en_GB.yml")
            if (!localeFile.exists()) {
                fatal("Could not load default locale. Please reinstall plugin.")
                Bukkit.getPluginManager().disablePlugin(instance!!)
            }
        }
        success("Loaded locale: " + localeFile.name)
    }
}