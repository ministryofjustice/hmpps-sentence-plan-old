package uk.gov.justice.digital.hmpps.sentenceplan.person

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

@Entity(name = "person")
@Table(name = "person")
class PersonEntity(
  @Id
  val id: UUID,
  val crn: String,
  val nomsId: String,
)

interface PersonRepository : JpaRepository<PersonEntity, UUID>
