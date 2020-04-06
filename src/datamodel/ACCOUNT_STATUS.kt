package datamodel

enum class ACCOUNT_STATUS {
    ACCCOUNTS_OK {
        override fun message(): String = "FELG - zalogowany | Konto SIP - dodane"
    },

    NO_ACCOUNT_SIP {
        override fun message(): String = "Brak konta SIP"
    };

    abstract fun message(): String
}