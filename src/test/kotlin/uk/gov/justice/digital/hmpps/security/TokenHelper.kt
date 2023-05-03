package uk.gov.justice.digital.hmpps.security

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.web.client.RestTemplate

class TokenHelper(
  private val url: String,
) {
  fun getToken(): String {
    val authResponse = RestTemplate()
      .postForObject("$url/auth/oauth/token", null, JsonNode::class.java)!!
    return authResponse["access_token"].asText()
  }
}

fun MockHttpServletRequestBuilder.withOAuth2Token(host: String) =
  this.header(AUTHORIZATION, "Bearer ${TokenHelper(host).getToken()}")

fun MockHttpServletRequestBuilder.json(content: String) = this
  .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
  .content(content)
