package ch.streckeisen.mycv.backend.cv.profile.theme

import ch.streckeisen.mycv.backend.cv.profile.ProfileService
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProfileThemeService(
    private val profileThemeRepository: ProfileThemeRepository,
    private val profileService: ProfileService,
    private val profileThemeValidationService: ProfileThemeValidationService
) {
    @Transactional
    fun save(accountId: Long, themeUpdate: ProfileThemeUpdateDto): Result<ProfileThemeEntity> {
        val profile = profileService.findByAccountId(accountId)
            .getOrElse { return Result.failure(it) }

        if (profile.account.id != accountId) {
            return Result.failure(AccessDeniedException("You don't have permission to edit this profile"))
        }

        profileThemeValidationService.validateThemeUpdate(themeUpdate)
            .onFailure { return Result.failure(it) }

        val theme = ProfileThemeEntity(
            themeUpdate.backgroundColor!!,
            themeUpdate.surfaceColor!!,
            profile,
            profile.profileTheme?.id
        )

        return Result.success(profileThemeRepository.save(theme))
    }
}