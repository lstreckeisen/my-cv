import {
  commonHeaders,
  fetchFromApi,
  getJSONIfResponseIsOk,
  processAuthResponse
} from '@/api/ApiHelper'
import type { AccountDto } from '@/dto/AccountDto'
import LoginStateService from '@/services/LoginStateService'

async function login(email: string, password: string): Promise<void> {
  try {
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({
        username: email,
        password: password
      }),
      headers: commonHeaders
    })
    return processAuthResponse(response)
  } catch (error) {
    return Promise.reject(error)
  }
}

async function signUp(account: AccountDto): Promise<void> {
  try {
    const response = await fetch('/api/auth/signUp', {
      method: 'POST',
      body: JSON.stringify(account),
      headers: commonHeaders
    })
    return processAuthResponse(response)
  } catch (error) {
    return Promise.reject(error)
  }
}

async function getAccountInfo(): Promise<AccountDto> {
  try {
    const response = await fetchFromApi('/account')
    const account = await getJSONIfResponseIsOk<AccountDto>(response)
    return Promise.resolve(account)
  } catch (error) {
    return Promise.reject(error)
  }
}

function isUserLoggedIn(): boolean {
  return LoginStateService.isLoggedIn()
}

async function logout(): Promise<void> {
  try {
    const response = await fetch('/api/auth/logout', {
      method: 'POST'
    })
    if (!response.ok) {
      return Promise.reject('Failed to perform logout')
    }
    LoginStateService.loggedOut()
    return Promise.resolve()
  } catch (error) {
    return Promise.reject(error)
  }
}

export default {
  login,
  signUp,
  isUserLoggedIn,
  logout,
  getAccountInfo
}
