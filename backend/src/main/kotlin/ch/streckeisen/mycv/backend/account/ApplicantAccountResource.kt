package ch.streckeisen.mycv.backend.account

import ch.streckeisen.mycv.backend.account.dto.AccountDto
import ch.streckeisen.mycv.backend.security.getMyCvPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/account")
class ApplicantAccountResource(private val applicantAccountService: ApplicantAccountService) {
    @GetMapping
    fun getAccount(): ResponseEntity<AccountDto> {
        val principal = SecurityContextHolder.getContext().authentication.getMyCvPrincipal()
        return applicantAccountService.findById(principal.id)
            .fold(
                onSuccess = { account ->
                    ResponseEntity.ok(account.toAccountDto())
                },
                onFailure = {
                    ResponseEntity.internalServerError().build()
                }
            )
    }
}