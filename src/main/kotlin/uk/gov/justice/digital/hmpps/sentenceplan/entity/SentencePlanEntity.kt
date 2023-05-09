package uk.gov.justice.digital.hmpps.sentenceplan.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import java.time.ZonedDateTime
import java.util.UUID

@Entity(name = "SentencePlan")
@Table(name = "sentence_plan")
class SentencePlanEntity(
  @ManyToOne
  @JoinColumn(name = "person_id", nullable = false)
  val person: PersonEntity,

  val createdDate: ZonedDateTime,

  val activeDate: ZonedDateTime? = null,

  val closedDate: ZonedDateTime? = null,

  @Id
  val id: UUID = UUID.randomUUID(),
)

interface SentencePlanRepository : JpaRepository<SentencePlanEntity, UUID> {
  fun existsByPersonIdAndClosedDateIsNull(personId: UUID): Boolean
  fun findByPersonId(personId: UUID): List<SentencePlanEntity>
}
