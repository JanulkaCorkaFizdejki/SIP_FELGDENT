package datamodel

enum class DATABASE_TYPE {
    LOCAL {
        override fun dbName() = DATABASE_LOCAL.name
    };

    abstract fun dbName(): String
}