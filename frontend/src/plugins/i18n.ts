import { createI18n } from 'vue-i18n'
import LanguageService from '@/services/LanguageService'

type NestedMessage = {
  [key: string]: string | NestedMessage
}

type Messages = {
  [locale: string]: NestedMessage
}

const messages: Messages = {}
const langModules = import.meta.glob('../locales/*.json', { eager: true })
Object.keys(langModules).forEach((filePath) => {
  const lang = filePath.replace('../locales/', '').replace('.json', '')
  messages[lang] = langModules[filePath] as NestedMessage
})

const i18n = createI18n({
  locale: LanguageService.getLanguage(),
  fallbackLocale: 'en',
  legacy: false,
  messages
})

export default i18n
