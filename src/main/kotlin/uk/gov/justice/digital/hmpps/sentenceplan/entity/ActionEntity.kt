package uk.gov.justice.digital.hmpps.sentenceplan.entity

import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import java.time.ZonedDateTime
import java.util.UUID

@Entity(name = "Action")
@Table(name = "action")
class ActionEntity(

  val objectiveId: UUID,
  var description: String,
  @Convert(converter = NumericToBooleanConverter::class)
  var interventionParticipation: Boolean = false,
  var interventionName: String?,
  var interventionType: String?,
  var status: String,
  var owner: String,
  val createdDateTime: ZonedDateTime = ZonedDateTime.now(),
  @Id
  val id: UUID = UUID.randomUUID(),
)

interface ActionRepository : JpaRepository<ActionEntity, UUID> {
  fun findAllByObjectiveIdOrderByCreatedDateTimeAsc(objectiveId: UUID): List<ActionEntity>
}
