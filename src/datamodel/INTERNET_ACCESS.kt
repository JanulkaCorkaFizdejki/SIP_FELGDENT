package datamodel

enum class INTERNET_ACCESS {
    INIT {
        override fun message() = "Łączenie"
    },

    NO_CONNECTION {
        override fun message() = "Brak Internetu"
    },

    CONNECTION {
        override fun message() = "Połączony"
    };


    abstract fun message(): String
}