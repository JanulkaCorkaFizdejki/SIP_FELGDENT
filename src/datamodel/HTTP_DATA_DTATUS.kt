package datamodel

enum class HTTP_DATA_STATUS {
    SERVER_DATA_OK {
      override fun message() = "Server - OK Data - OK"
    },
    SERVER_OK {
        override fun message() = "Server - OK Data - OK"
    },
    SERVER_ERROR {
        override fun message() = "Błąd serwera aplikacji!"
    },
    DATA_ERROR {
        override fun message() = "Nie można pobrać danych z serwera!"
    },
    LOGIN_PASSWORD_ERROR {
        override fun message() = "Nieprawidłowy login lub hasło!"
    };

    abstract fun message(): String
}