package uk.gov.justice.digital.hmpps.sentenceplan.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import java.time.ZonedDateTime
import java.util.UUID

@Entity(name = "Action")
@Table(name = "action")
class ActionEntity(
  @ManyToOne
  @JoinColumn(name = "objective_id", nullable = false)
  val objective: ObjectiveEntity,
  var description: String,
  var interventionParticipation: Boolean = false,
  var interventionName: String?,
  var interventionType: String?,
  var status: String,
  var individualOwner: Boolean = false,
  var practitionerOwner: Boolean = false,
  var otherOwner: String?,
  var targetDateMonth: Int,
  var targetDateYear: Int,
  val createdDateTime: ZonedDateTime = ZonedDateTime.now(),
  @Id
  val id: UUID = UUID.randomUUID(),
)

interface ActionRepository : JpaRepository<ActionEntity, UUID> {
  fun findAllByObjectiveIdOrderByCreatedDateTimeAsc(objectiveId: UUID): List<ActionEntity>
}
