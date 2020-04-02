package model

import java.sql.ResultSet

interface DatabaseManager {
    fun select (query: String) : ResultSet
    fun insert (query: String)
    fun updaate (query: String)
    fun delete(query: String)
    fun close ()
}