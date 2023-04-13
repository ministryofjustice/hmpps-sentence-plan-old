package uk.gov.justice.digital.hmpps.sentenceplan

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SentencePlanApplication

fun main(args: Array<String>) {
  runApplication<SentencePlanApplication>(*args)
}
