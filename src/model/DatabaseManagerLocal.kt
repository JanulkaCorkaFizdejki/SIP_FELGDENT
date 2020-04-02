package model

import datamodel.DATABASE_LOCAL
import java.lang.Exception
import java.sql.*

class DatabaseManagerLocal: DatabaseManager {

    private var connection: Connection? = null

    init {
        try {
            Class.forName("org.sqlite.JDBC")
            this.connection = DriverManager.getConnection("jdbc:sqlite:${DATABASE_LOCAL.name}")

        } catch (e: Exception) {
            println(e)
        }
    }

    override fun select(query: String): ResultSet {
        val stmt: Statement = this.connection!!.createStatement()
        return stmt.executeQuery(query)

    }

    override fun insert(query: String) {
        val pstmt: PreparedStatement = this.connection!!.prepareStatement(query)
        pstmt.executeUpdate()
        pstmt.close()

    }

    override fun updaate(query: String) {
        val pstmt:  PreparedStatement = this.connection!!.prepareStatement(query)
        pstmt.executeUpdate()
        pstmt.close()

    }

    override fun delete(query: String) {
        val pstmt:  PreparedStatement = this.connection!!.prepareStatement(query)
        pstmt.executeUpdate()
        pstmt.close()

    }

    override fun close() {
        this.connection?.close()
    }
}