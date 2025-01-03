package ch.streckeisen.mycv.backend.account.auth

import ch.streckeisen.mycv.backend.account.ApplicantAccountService
import ch.streckeisen.mycv.backend.account.dto.LoginRequestDto
import ch.streckeisen.mycv.backend.account.dto.SignupRequestDto
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import ch.streckeisen.mycv.backend.security.JwtService
import io.jsonwebtoken.JwtException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

private const val LOGIN_VALIDATION_ERROR_KEY = "${MYCV_KEY_PREFIX}.auth.login.error"

@Service
class AuthenticationService(
    private val applicantAccountService: ApplicantAccountService,
    private val userDetailsService: UserDetailsService,
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val messagesService: MessagesService
) {
    fun signUp(signupRequest: SignupRequestDto): Result<AuthData> {
        return applicantAccountService.create(signupRequest)
            .fold(
                onSuccess = {
                    authenticate(LoginRequestDto(signupRequest.email, signupRequest.password))
                },
                onFailure = {
                    Result.failure(it)
                }
            )
    }

    fun authenticate(loginRequest: LoginRequestDto): Result<AuthData> {
        return validateLoginRequest(loginRequest)
            .fold(
                onSuccess = {
                    try {
                        authenticationManager.authenticate(
                            UsernamePasswordAuthenticationToken(
                                loginRequest.username,
                                loginRequest.password
                            )
                        )

                        val userDetails = userDetailsService.loadUserByUsername(loginRequest.username)
                        val accessToken = jwtService.generateAccessToken(userDetails)
                        val refreshToken = jwtService.generateRefreshToken(userDetails)
                        val accessTokenExpirationTime = jwtService.getAccessTokenExpirationTime()
                        val refreshTokenExpirationTime = jwtService.getRefreshTokenExpirationTime()
                        Result.success(
                            AuthData(
                                accessToken,
                                accessTokenExpirationTime,
                                refreshToken,
                                refreshTokenExpirationTime
                            )
                        )
                    } catch (ex: AuthenticationException) {
                        Result.failure(ex)
                    }
                },
                onFailure = {
                    Result.failure(it)
                }
            )
    }

    fun refreshAccessToken(oldRefreshToken: String): Result<AuthData> {
        val username = jwtService.extractUsername(oldRefreshToken)
        val userDetails = userDetailsService.loadUserByUsername(username)
        if (userDetails == null || !jwtService.isTokenValid(oldRefreshToken, userDetails)) {
            return Result.failure(JwtException("Invalid refresh token"))
        }

        val accessToken = jwtService.generateAccessToken(userDetails)
        val refreshToken = jwtService.generateRefreshToken(userDetails)
        return Result.success(
            AuthData(
                accessToken,
                jwtService.getAccessTokenExpirationTime(),
                refreshToken,
                jwtService.getRefreshTokenExpirationTime()
            )
        )
    }

    private fun validateLoginRequest(loginRequest: LoginRequestDto): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        if (loginRequest.username.isNullOrBlank()) {
            val error = messagesService.getMessage("username")
            validationErrorBuilder.addError("username", error)
        }

        if (loginRequest.password.isNullOrBlank()) {
            val error = messagesService.getMessage("password")
            validationErrorBuilder.addError("password", error)
        }

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build(messagesService.getMessage(LOGIN_VALIDATION_ERROR_KEY)))
        }
        return Result.success(Unit)
    }
}