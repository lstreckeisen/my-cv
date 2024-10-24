<template>
  <v-container class="profile-editor">
    <v-row>
      <v-col cols="12" class="back-button">
        <router-link :to="{ name: 'account' }">
          <v-btn icon="mdi-arrow-left" />
          <span>Back to account</span>
        </router-link>
      </v-col>
      <v-col cols="12">
        <h2>Profile</h2>
      </v-col>
    </v-row>
    <v-tabs v-model="activeTab" color="secondary">
      <v-tab value="general">General</v-tab>
      <template v-if="isCreated">
        <v-tab value="work">Work Experience</v-tab>
        <v-tab value="edu">Education</v-tab>
        <v-tab value="skills">Skills</v-tab>
      </template>
    </v-tabs>

    <v-tabs-window v-model="activeTab">
      <v-tabs-window-item value="general">
        <v-row justify="center">
          <v-sheet class="form-sheet" rounded>
            <v-form @submit.prevent>
              <v-row class="form-flex">
                <v-col cols="12" md="4">
                  <v-img
                    :src="profilePicture"
                    :lazy-src="defaultProfilePicture"
                    class="profile-picture"
                  >
                    <template #placeholder>
                      <v-row class="fill-height" justify="center" align="center">
                        <v-progress-circular indeterminate />
                      </v-row>
                    </template>
                  </v-img>
                  <v-file-input
                    v-model="formState.profilePicture"
                    label="Profile Picture"
                    hint="Your profile picture must not exceed 2MB."
                    prepend-icon="mdi-camera"
                    accept=".png, .jpeg, .jpg"
                    :error-messages="profilePictureErrors"
                  />
                </v-col>

                <v-col cols="12" md="8">
                  <v-text-field
                    v-model="formState.alias"
                    label="Alias"
                    hint="Name that will be used in the URL of your profile"
                    :error-messages="aliasErrors"
                  />
                  <v-text-field
                    v-model="formState.jobTitle"
                    label="Job Title"
                    :error-messages="jobTitleErrors"
                  />
                  <v-textarea v-model="formState.bio" label="Bio" :error-messages="bioErrors" />
                  <v-switch
                    v-model="formState.isProfilePublic"
                    label="Public Profile Access"
                    hint="If enabled, your profile will be publicly accessible"
                    color="primary"
                    :error-messages="isProfilePublicErrors"
                  />
                  <v-switch
                    v-model="formState.isEmailPublic"
                    :disabled="!formState.isProfilePublic"
                    label="Show E-Mail in public profile"
                    hint="If enabled, your E-Mail address will be shown in your profile"
                    color="primary"
                    :error-messages="isEmailPublicErrors"
                  />
                  <v-switch
                    v-model="formState.isPhonePublic"
                    :disabled="!formState.isProfilePublic"
                    label="Show phone in public profile"
                    hint="If enabled, your phone number will be shown in your profile"
                    color="primary"
                    :error-messages="isPhonePublicErrors"
                  />
                  <v-switch
                    v-model="formState.isAddressPublic"
                    :disabled="!formState.isProfilePublic"
                    label="Show address in public profile"
                    hint="If enabled, your address will be shown in your profile"
                    color="primary"
                    :error-messages="isAddressPublicErrors"
                  />
                  <v-switch
                    v-model="formState.hideDescriptions"
                    :disabled="!formState.isProfilePublic"
                    label="Hide descriptions from public profile"
                    hint="If enabled, detailed descriptions of your CV entries won't be shown in your profile"
                    color="primary"
                    :error-messages="hideDescriptionsErrors"
                  />
                </v-col>
              </v-row>

              <v-btn type="submit" text="Save" color="primary" @click="saveGeneralInformation" />
            </v-form>
          </v-sheet>
        </v-row>
      </v-tabs-window-item>
      <template v-if="isCreated">
        <v-tabs-window-item value="work">
          <work-experiences-editor v-model="workExperiences" />
        </v-tabs-window-item>
        <v-tabs-window-item value="edu">
          <education-editor v-model="education" />
        </v-tabs-window-item>
        <v-tabs-window-item value="skills">
          <skills-editor v-model="skills" />
        </v-tabs-window-item>
      </template>
    </v-tabs-window>
  </v-container>
  <notification
    v-if="savingError"
    title="Failed to save profile"
    :message="`An error occurred while trying to save your profile. ${savingError}`"
  />
</template>

<script setup lang="ts">
import { computed, type ComputedRef, reactive, ref } from 'vue'
import type { ProfileDto } from '@/dto/ProfileDto'
import profileApi from '@/api/ProfileApi'
import ProfileApi from '@/api/ProfileApi'
import WorkExperiencesEditor from '@/views/profile/components/work-experience/WorkExperiencesEditor.vue'
import EducationEditor from '@/views/profile/components/education/EducationEditor.vue'
import SkillsEditor from '@/views/profile/components/skill/SkillsEditor.vue'
import { helpers, required } from '@vuelidate/validators'
import useVuelidate from '@vuelidate/core'
import { type ErrorMessages, getErrorMessages } from '@/services/FormHelper'
import type { ErrorDto } from '@/dto/ErrorDto'
import router from '@/router'
import Notification from '@/components/Notification.vue'
import round from 'lodash/round'
import type { ProfileUpdateRequestDto } from '@/dto/ProfileUpdateRequestDto'

const props = defineProps<{
  profile: ProfileDto
  exists: boolean
}>()

const profilePictureMaxSize = 2097152
const activeTab = ref('general')
const isCreated = ref(props.exists)
const workExperiences = ref(props.profile.workExperiences)
const education = ref(props.profile.education)
const skills = ref(props.profile.skills)
const savingError = ref<string>()
const profilePictureUrl = ref(props.profile.profilePicture)

const defaultProfilePicture = ProfileApi.getDefaultProfilePicture()
const profilePicture = computed(() => {
  return profilePictureUrl.value || defaultProfilePicture
})

type FormState = {
  profilePicture?: File
  alias?: string
  jobTitle?: string
  bio?: string
  isProfilePublic?: boolean
  isEmailPublic?: boolean
  isPhonePublic?: boolean
  isAddressPublic?: boolean
  hideDescriptions?: boolean
}

const formState = reactive<FormState>({
  profilePicture: undefined,
  alias: props.profile.alias,
  jobTitle: props.profile.jobTitle,
  bio: props.profile.bio,
  isProfilePublic: props.profile.isProfilePublic,
  isEmailPublic: props.profile.isEmailPublic,
  isPhonePublic: props.profile.isPhonePublic,
  isAddressPublic: props.profile.isAddressPublic,
  hideDescriptions: props.profile.hideDescriptions
})

const profilePictureSizeValidator = () =>
  formState.profilePicture == undefined || formState.profilePicture?.size <= profilePictureMaxSize

const rules = {
  profilePicture: isCreated.value
    ? {
        profilePictureSizeValidator: helpers.withMessage(
          `Profile Picture must be less than ${round(profilePictureMaxSize * Math.pow(2, -20), 1)} MB`,
          profilePictureSizeValidator
        )
      }
    : {
        required: helpers.withMessage('Profile Picture is required', required),
        profilePictureSizeValidator: helpers.withMessage(
          `Profile Picture must be less than ${round(profilePictureMaxSize * Math.pow(2, -20), 1)} MB`,
          profilePictureSizeValidator
        )
      },
  alias: {
    required: helpers.withMessage('Alias is required', required)
  },
  jobTitle: {
    required: helpers.withMessage('Job Title is required', required)
  },
  bio: {},
  isProfilePublic: {},
  isEmailPublic: {},
  isPhonePublic: {},
  isAddressPublic: {},
  hideDescriptions: {}
}

const form = useVuelidate<FormState>(rules, formState)
const errorMessages = ref<ErrorMessages>({})

function getErrors(attributeName: string): ComputedRef {
  return getErrorMessages(errorMessages, form, attributeName)
}

const profilePictureErrors = getErrors('profilePicture')
const aliasErrors = getErrors('alias')
const jobTitleErrors = getErrors('jobTitle')
const bioErrors = getErrors('bio')
const isProfilePublicErrors = getErrors('isProfilePublic')
const isEmailPublicErrors = getErrors('isEmailPublic')
const isPhonePublicErrors = getErrors('isPhonePublic')
const isAddressPublicErrors = getErrors('isAddressPublic')
const hideDescriptionsErrors = getErrors('hideDescriptions')

async function saveGeneralInformation() {
  const isValid = await form.value.$validate()
  if (!isValid) {
    return
  }

  try {
    const profileUpdate: ProfileUpdateRequestDto = {
      profilePicture: formState.profilePicture,
      alias: formState.alias,
      jobTitle: formState.jobTitle,
      bio: formState.bio,
      isProfilePublic: formState.isProfilePublic,
      isEmailPublic: formState.isEmailPublic,
      isPhonePublic: formState.isPhonePublic,
      isAddressPublic: formState.isAddressPublic,
      hideDescriptions: formState.hideDescriptions
    }

    const savedProfile = await profileApi.updateGeneralInformation(profileUpdate)
    if (isCreated.value === false) {
      await router.push({ name: 'edit-profile' })
    } else {
      isCreated.value = true
      errorMessages.value = {}

      formState.alias = savedProfile.alias
      formState.jobTitle = savedProfile.jobTitle
      formState.bio = savedProfile.bio
      formState.isProfilePublic = savedProfile.isProfilePublic
      formState.isEmailPublic = savedProfile.isEmailPublic
      formState.isPhonePublic = savedProfile.isPhonePublic
      formState.isAddressPublic = savedProfile.isAddressPublic
      formState.hideDescriptions = savedProfile.hideDescriptions
      formState.profilePicture = undefined

      profilePictureUrl.value = savedProfile.profilePicture
    }
  } catch (e) {
    const error = e as ErrorDto
    if (error.errors) {
      errorMessages.value = error.errors
      savingError.value = undefined
    } else {
      errorMessages.value = {}
      savingError.value = error.message || ' '
    }
  }
}
</script>

<style lang="scss" scoped>
.back-button {
  padding-left: 0;
}

h2 {
  margin-bottom: 10px;
}

.profile-editor {
  .form-sheet {
    margin-top: 10px;
    width: 100%;
    padding: 30px;

    .form-flex {
      flex-direction: row-reverse;

      .profile-picture {
        max-width: min(100%, 400px);
        height: auto;
        margin-bottom: 10px;
        margin-left: 40px;
      }
    }
  }
}
</style>
