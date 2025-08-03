package com.example.mueblar.data.validator

object Validators {
    private val validDomains = listOf("gmail.com", "hotmail.com", "outlook.com", "yahoo.com", "mueblar.com")

    fun isValidEmail(email: String): Boolean {
        val regex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})".toRegex()
        return email.matches(regex) && validDomains.any { email.endsWith(it) }
    }

    fun isValidPhone(phone: String): Boolean {
        // Aceptar números con prefijo de país (ej. +51987654321) o sin él (987654321)
        val cleanPhone = phone.replace("[^0-9+]".toRegex(), "")
        return cleanPhone.matches("^(\\+[0-9]{1,3})?[0-9]{9}$".toRegex()) && cleanPhone.length <= 12
    }

    fun isValidPassword(password: String): Boolean {
        return password.length in 6..16
    }

    fun isValidRuc(ruc: String): Boolean {
        return ruc.length == 11 && ruc.all { it.isDigit() }
    }
}