package ch.streckeisen.mycv.backend.account.dto

import java.time.LocalDate

data class SignupRequestDto(
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val phone: String?,
    val birthday: LocalDate?,
    val street: String?,
    val houseNumber: String?,
    val postcode: String?,
    val city: String?,
    val country: String?,
    val hasPublicProfile: Boolean?,
    val password: String?
)