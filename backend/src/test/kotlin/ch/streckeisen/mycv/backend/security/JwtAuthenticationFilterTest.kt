package ch.streckeisen.mycv.backend.security

import ch.streckeisen.mycv.backend.account.auth.ACCESS_TOKEN_NAME
import io.jsonwebtoken.ExpiredJwtException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.DispatcherType
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.servlet.HandlerExceptionResolver
import org.springframework.web.servlet.ModelAndView
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JwtAuthenticationFilterTest {
    private lateinit var jwtService: JwtService
    private lateinit var userDetailsService: UserDetailsServiceImpl
    private lateinit var handlerExceptionResolver: HandlerExceptionResolver
    private lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter

    @BeforeEach
    fun setup() {
        jwtService = mockk()
        userDetailsService = mockk()
        handlerExceptionResolver = mockk {
            every { resolveException(any(), any(), any(), any()) } returns mockk<ModelAndView>()
        }
        jwtAuthenticationFilter = JwtAuthenticationFilter(jwtService, userDetailsService, handlerExceptionResolver)

        SecurityContextHolder.getContext().authentication = null
    }

    @Test
    fun testNoAuthentication() {
        val request = mockRequest()
        val response = mockResponse()
        val filterChain = mockFilterChain()
        every { request.cookies } returns emptyArray()
        assertNull(SecurityContextHolder.getContext().authentication)

        jwtAuthenticationFilter.doFilter(request, response, filterChain)

        assertNull(SecurityContextHolder.getContext().authentication)
        verify(exactly = 1) { filterChain.doFilter(any(), any()) }
        verify(exactly = 0) { handlerExceptionResolver.resolveException(any(), any(), any(), any()) }
    }

    @Test
    fun testWrongCookieAuthentication() {
        val request = mockRequest()
        val response = mockResponse()
        val filterChain = mockFilterChain()
        every { request.cookies } returns arrayOf(mockk { every { name } returns "wrong-cookie" })
        assertNull(SecurityContextHolder.getContext().authentication)

        jwtAuthenticationFilter.doFilter(request, response, filterChain)

        assertNull(SecurityContextHolder.getContext().authentication)
        verify(exactly = 1) { filterChain.doFilter(any(), any()) }
        verify(exactly = 0) { handlerExceptionResolver.resolveException(any(), any(), any(), any()) }
    }

    @Test
    fun testSuccessfulAuthentication() {
        val request = mockRequest()
        val response = mockResponse()
        val filterChain = mockFilterChain()
        val userId = 1L
        val username = "username"
        val token = "abcdefg"
        val user = User.withUsername(username).password("password").build()
        val userDetails = MyCvUserDetails(user, mockk { every { id } returns userId })
        assertNull(SecurityContextHolder.getContext().authentication)
        every { request.cookies } returns arrayOf(mockk {
            every { name } returns ACCESS_TOKEN_NAME
            every { value } returns token
        })
        every { jwtService.extractUsername(eq(token)) } returns username
        every { jwtService.isTokenValid(any(), any()) } returns true
        every { userDetailsService.loadUserByUsername(eq(username)) } returns userDetails

        jwtAuthenticationFilter.doFilter(request, response, filterChain)

        val auth = SecurityContextHolder.getContext().authentication as UsernamePasswordAuthenticationToken
        assertNotNull(auth)
        assertTrue { auth.isAuthenticated }
        val principal = auth.principal as MyCvPrincipal
        assertEquals(username, principal.username)
        assertEquals(userId, principal.id)
        verify(exactly = 0) { handlerExceptionResolver.resolveException(any(), any(), any(), any()) }
    }

    @Test
    fun testAlreadyAuthenticated() {
        val request = mockRequest()
        val response = mockResponse()
        val filterChain = mockFilterChain()
        every { request.cookies } returns arrayOf(mockk {
            every { name } returns ACCESS_TOKEN_NAME
            every { value } returns "abcdefg"
        })
        every { jwtService.extractUsername(eq("abcdefg")) } returns "username"
        val authToken = UsernamePasswordAuthenticationToken(MyCvPrincipal("username", 1), null, listOf())
        authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
        SecurityContextHolder.getContext().authentication = authToken

        jwtAuthenticationFilter.doFilter(request, response, filterChain)

        val auth = SecurityContextHolder.getContext().authentication as UsernamePasswordAuthenticationToken
        assertNotNull(auth)
        assertTrue { auth.isAuthenticated }
        val principal = auth.principal as MyCvPrincipal
        assertEquals("username", principal.username)
        verify(exactly = 0) { userDetailsService.loadUserByUsername(any()) }
        verify(exactly = 0) { handlerExceptionResolver.resolveException(any(), any(), any(), any()) }
    }

    @Test
    fun testInvalidTokenAuthentication() {
        val request = mockRequest()
        val response = mockResponse()
        val filterChain = mockFilterChain()
        every { request.cookies } returns arrayOf(mockk {
            every { name } returns ACCESS_TOKEN_NAME
            every { value } returns "abcdefg"
        })
        every { jwtService.extractUsername(any()) } throws mockk<ExpiredJwtException>()

        jwtAuthenticationFilter.doFilter(request, response, filterChain)

        assertNull(SecurityContextHolder.getContext().authentication)
        verify(exactly = 0) { filterChain.doFilter(any(), any()) }
        verify(exactly = 1) { handlerExceptionResolver.resolveException(any(), any(), any(), any()) }
    }

    private fun mockRequest(): HttpServletRequest = mockk<HttpServletRequest> {
        every { dispatcherType } returns DispatcherType.REQUEST
        every { getAttribute(any()) } returns null
        every { setAttribute(any(), any()) } returns Unit
        every { removeAttribute(any()) } returns Unit
        every { remoteAddr } returns "127.0.0.1"
        every { getSession(any()) } returns mockk<HttpSession> {
            every { id } returns "sessionId"
        }
    }

    private fun mockResponse(): HttpServletResponse = mockk {
    }

    private fun mockFilterChain(): FilterChain = mockk {
        every { doFilter(any(), any()) } returns Unit
    }
}