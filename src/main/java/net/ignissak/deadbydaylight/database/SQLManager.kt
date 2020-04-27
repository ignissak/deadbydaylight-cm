package net.ignissak.deadbydaylight.database

import net.ignissak.deadbydaylight.Main.Companion.instance
import java.sql.Connection
import java.sql.PreparedStatement

class SQLManager {
    private var database = instance!!.database

    //todo
    fun createTables() {
        var conn: Connection? = null
        var ps: PreparedStatement? = null
        try {
            conn = database!!.connection()
            // todo
            ps = conn!!.prepareStatement("")
            ps.executeUpdate()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            database!!.close(conn, ps, null)
        }
    }
}