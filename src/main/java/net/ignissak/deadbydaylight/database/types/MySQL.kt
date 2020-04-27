package net.ignissak.deadbydaylight.database.types

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.ignissak.deadbydaylight.Main.Companion.instance
import net.ignissak.deadbydaylight.database.Database
import java.sql.Connection
import java.sql.SQLException

class MySQL : Database() {
    private var hikariDataSource: HikariDataSource? = null

    @Throws(SQLException::class)
    public override fun connection(): Connection? {
        return hikariDataSource!!.connection
    }

    override fun hikariDataSource(): HikariDataSource? {
        return hikariDataSource
    }

    override fun setup() {
        val databaseData = instance!!.databaseData
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:mysql://" + databaseData!!.hostname + ":" + databaseData.port + "/" + databaseData.database + "?characterEncoding=UTF-8"
        config.driverClassName = "com.mysql.jdbc.Driver"
        config.username = databaseData.username
        config.password = databaseData.password
        config.connectionTimeout = 15000
        config.maximumPoolSize = 10
        config.minimumIdle = 2
        hikariDataSource = HikariDataSource(config)
        instance!!.logger.info("Set up MySQL connection.")
    }
}