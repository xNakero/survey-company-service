package pl.wat.surveycompanyservice.domain.role

import pl.wat.surveycompanyservice.domain.user.AppUser
import javax.persistence.CascadeType.ALL
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToMany
import javax.persistence.Table

@Entity
@Table(name = "role")
class Role(
    @Id @GeneratedValue val roleId: Long = 0L,
    val name: String,
    @ManyToMany(mappedBy = "roles", cascade = [ALL]) val users: MutableSet<AppUser> = mutableSetOf()
)

enum class AppRole {
    PARTICIPANT, RESEARCHER
}