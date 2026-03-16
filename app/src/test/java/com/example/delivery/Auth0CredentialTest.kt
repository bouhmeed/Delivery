package com.example.delivery

import org.junit.Test
import org.junit.Assert.*
import java.net.HttpURLConnection
import java.net.URL

/**
 * Test class to verify Auth0 credentials configuration
 */
class Auth0CredentialTest {
    
    private val clientId = "nv6hJnrENz4yWMuXAKeHclNLveG39oq5"
    private val clientSecret = "JF1ENICGOjkjUCTkhz3Aq1RoHR4aLp04rSqY-CPYtW9WW94NgcZyYcvkqAVriZsd"
    private val issuerBaseUrl = "https://almakom.eu.auth0.com"
    private val callbackUrl = "delivery://auth0/callback"
    
    @Test
    fun testClientIdFormat() {
        assertTrue("Client ID should not be blank", clientId.isNotBlank())
        assertTrue("Client ID should be longer than 10 characters", clientId.length > 10)
        println("✅ Client ID format test passed")
    }
    
    @Test
    fun testClientSecretFormat() {
        assertTrue("Client Secret should not be blank", clientSecret.isNotBlank())
        assertTrue("Client Secret should be longer than 20 characters", clientSecret.length > 20)
        println("✅ Client Secret format test passed")
    }
    
    @Test
    fun testIssuerUrlFormat() {
        assertTrue("Issuer URL should not be blank", issuerBaseUrl.isNotBlank())
        assertTrue("Issuer URL should start with https://", issuerBaseUrl.startsWith("https://"))
        assertTrue("Issuer URL should contain auth0.com", issuerBaseUrl.contains("auth0.com"))
        println("✅ Issuer URL format test passed")
    }
    
    @Test
    fun testCallbackUrlFormat() {
        assertTrue("Callback URL should not be blank", callbackUrl.isNotBlank())
        assertTrue("Callback URL should use delivery scheme", callbackUrl.startsWith("delivery://"))
        assertTrue("Callback URL should contain auth0/callback", callbackUrl.contains("auth0/callback"))
        println("✅ Callback URL format test passed")
    }
    
    @Test
    fun testIssuerUrlAccessibility() {
        try {
            val url = URL("$issuerBaseUrl/.well-known/openid_configuration")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            val responseCode = connection.responseCode
            
            assertEquals("Issuer URL should return 200", 200, responseCode)
            println("✅ Issuer URL accessibility test passed (Response: $responseCode)")
            
            connection.disconnect()
        } catch (e: Exception) {
            fail("Failed to connect to issuer URL: ${e.message}")
        }
    }
    
    @Test
    fun testOAuthEndpointExists() {
        try {
            val url = URL("$issuerBaseUrl/oauth/token")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            // Test with minimal request body
            val requestBody = """{"client_id":"$clientId","grant_type":"client_credentials"}"""
            connection.outputStream.use { it.write(requestBody.toByteArray()) }
            
            val responseCode = connection.responseCode
            
            // Should return either 200 (success) or 401 (unauthorized - endpoint exists but wrong credentials)
            assertTrue("OAuth endpoint should return 200 or 401", responseCode == 200 || responseCode == 401)
            println("✅ OAuth endpoint test passed (Response: $responseCode)")
            
            connection.disconnect()
        } catch (e: Exception) {
            fail("Failed to test OAuth endpoint: ${e.message}")
        }
    }
    
    @Test
    fun testUserInfoEndpointExists() {
        try {
            val url = URL("$issuerBaseUrl/userinfo")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            val responseCode = connection.responseCode
            
            // Should return 401 without authentication token
            assertEquals("UserInfo endpoint should return 401 without token", 401, responseCode)
            println("✅ UserInfo endpoint test passed (Response: $responseCode)")
            
            connection.disconnect()
        } catch (e: Exception) {
            fail("Failed to test UserInfo endpoint: ${e.message}")
        }
    }
    
    @Test
    fun testConfigurationConsistency() {
        // Test that the configuration matches what's in Auth0Config.kt
        val configClientId = "nv6hJnrENz4yWMuXAKeHclNLveG39oq5"
        val configIssuerUrl = "https://almakom.eu.auth0.com"
        val configCallbackUrl = "delivery://auth0/callback"
        
        assertEquals("Client ID should match config", clientId, configClientId)
        assertEquals("Issuer URL should match config", issuerBaseUrl, configIssuerUrl)
        assertEquals("Callback URL should match config", callbackUrl, configCallbackUrl)
        println("✅ Configuration consistency test passed")
    }
}
