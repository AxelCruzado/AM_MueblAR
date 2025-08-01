package com.example.mueblar.data.validator

object Validators {
    private val validDomains = listOf("gmail.com", "hotmail.com", "outlook.com", "yahoo.com","mueblar.com")

    fun isValidEmail(email: String): Boolean {
        val regex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})".toRegex()
        return email.matches(regex) && validDomains.any { email.endsWith(it) }
    }

    fun isValidPhone(phone: String): Boolean {
        return phone.length == 9 && phone.all { it.isDigit() }
    }

    fun isValidRuc(ruc: String): Boolean {
        return ruc.length == 11 && ruc.all { it.isDigit() }
    }
}
