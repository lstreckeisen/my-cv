import type { PublicWorkExperienceDto } from '@/dto/PublicWorkExperienceDto'
import type { PublicSkillDto } from '@/dto/PublicSkillDto'
import type { PublicEducationDto } from '@/dto/PublicEducationDto'

export type PublicProfileDto = {
  firstName: string,
  lastName: string,
  jobTitle: string,
  aboutMe: string,
  email?: string,
  phone?: string,
  address?: PublicAddressDto
  workExperiences: Array<PublicWorkExperienceDto>,
  skills: Array<PublicSkillDto>,
  education: Array<PublicEducationDto>
}

export type PublicAddressDto = {
  street: string,
  houseNumber?: string,
  postcode: string,
  city: string,
  country: string,
}