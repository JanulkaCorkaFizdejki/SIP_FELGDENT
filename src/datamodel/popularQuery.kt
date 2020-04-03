package datamodel

object SQLITEPopularQuery {
    const val select_count_sip_user: String = "SELECT COUNT(uid) AS isset FROM ${DATABASE_LOCAL.tables.sip_user} LIMIT 1"
}