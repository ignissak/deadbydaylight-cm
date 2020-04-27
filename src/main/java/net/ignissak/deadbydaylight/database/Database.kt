package net.ignissak.deadbydaylight.database

import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

abstract class Database {
    @Throws(SQLException::class)
    abstract fun connection(): Connection?
    abstract fun hikariDataSource(): HikariDataSource?
    abstract fun setup()
    fun closePool() {
        if (hikariDataSource() != null && !hikariDataSource()!!.isClosed) {
            hikariDataSource()!!.close()
        }
    }

    fun close(connection: Connection?, preparedStatement: PreparedStatement?, resultSet: ResultSet?) {
        if (connection != null) try {
            connection.close()
        } catch (ignored: SQLException) {
        }
        if (preparedStatement != null) try {
            preparedStatement.close()
        } catch (ignored: SQLException) {
        }
        if (resultSet != null) try {
            resultSet.close()
        } catch (ignored: SQLException) {
        }
    }
}