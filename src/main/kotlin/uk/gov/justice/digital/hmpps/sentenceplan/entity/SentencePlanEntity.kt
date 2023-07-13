package uk.gov.justice.digital.hmpps.sentenceplan.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.sentenceplan.exception.NotFoundException
import java.time.ZonedDateTime
import java.util.UUID

@Entity(name = "SentencePlan")
@Table(name = "sentence_plan")
class SentencePlanEntity(
  @ManyToOne
  @JoinColumn(name = "person_id", nullable = false)
  val person: PersonEntity,

  val createdDate: ZonedDateTime,

  var activeDate: ZonedDateTime? = null,

  var closedDate: ZonedDateTime? = null,

  var riskFactors: String? = null,

  var protectiveFactors: String? = null,

  var practitionerComments: String? = null,

  var individualComments: String? = null,

  @Id
  val id: UUID = UUID.randomUUID(),
)

interface SentencePlanRepository : JpaRepository<SentencePlanEntity, UUID> {
  fun existsByPersonIdAndClosedDateIsNull(personId: UUID): Boolean

  @Query(
    """
    select case when count(sp) = 0 then false else true end
    from SentencePlan sp
    where sp.person.id = :personId
    and sp.closedDate is null
    and sp.id <> :sentencePlanId
  """,
  )
  fun existsAnotherActiveSentencePlan(personId: UUID, sentencePlanId: UUID): Boolean
  fun findByPersonId(personId: UUID): List<SentencePlanEntity>
}

fun SentencePlanRepository.getByIdOrThrow(id: UUID): SentencePlanEntity =
  findById(id).orElseThrow { NotFoundException("SentencePlan", "id", id) }
