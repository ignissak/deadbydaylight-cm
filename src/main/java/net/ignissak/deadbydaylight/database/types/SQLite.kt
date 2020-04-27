package net.ignissak.deadbydaylight.database.types

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.ignissak.deadbydaylight.Main.Companion.instance
import net.ignissak.deadbydaylight.database.Database
import java.io.File
import java.io.IOException
import java.sql.Connection
import java.sql.SQLException

class SQLite : Database() {
    private var hikariDataSource: HikariDataSource? = null
    private val databaseFile = File(instance!!.dataFolder, "local.db")

    @Throws(SQLException::class)
    override fun connection(): Connection? {
        return hikariDataSource!!.connection
    }

    override fun hikariDataSource(): HikariDataSource? {
        return hikariDataSource
    }

    override fun setup() {
        if (!databaseFile.exists()) {
            try {
                databaseFile.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        val databaseData = instance!!.databaseData
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:sqlite:$databaseFile"
        hikariDataSource = HikariDataSource(config)
        instance!!.logger.info("Set up SQLite connection.")
    }
}