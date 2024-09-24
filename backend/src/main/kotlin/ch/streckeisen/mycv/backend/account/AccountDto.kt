package ch.streckeisen.mycv.backend.account

import java.time.LocalDate

data class AccountDto(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val birthday: LocalDate,
    val street: String,
    val houseNumber: String?,
    val postcode: String,
    val city: String,
    val country: String,
    val hasPublicProfile: Boolean,
)