package net.ignissak.deadbydaylight.database

import net.ignissak.deadbydaylight.Main

class DatabaseData {
    val type: String
    val hostname: String
    val username: String
    val database: String
    val password: String
    val tableName: String
    val port: Int
    val isDefault: Boolean
        get() = hostname == "localhost" && username == "username" && database == "database" && password == "password"

    init {
        val sec = Main.configFile.getConfigurationSection("database")
        type = sec.getString("type", "SQLite")
        hostname = sec.getString("connection.hostname", "localhost")
        port = sec.getInt("connection.port", 3306)
        username = sec.getString("connection.username", "username")
        database = sec.getString("connection.database", "database")
        password = sec.getString("connection.password", "password")
        tableName = sec.getString("table_name", "dbd_players")
    }
}