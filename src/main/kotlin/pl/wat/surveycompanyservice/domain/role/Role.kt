package pl.wat.surveycompanyservice.domain.role

import pl.wat.surveycompanyservice.domain.user.AppUser
import javax.persistence.*
import javax.persistence.CascadeType.ALL

@Entity
@Table(name = "role")
class Role(
    @Id @GeneratedValue val roleId: Long = 0L,
    val name: String,
    @ManyToMany(mappedBy = "roles", cascade = [ALL]) val users: MutableSet<AppUser> = mutableSetOf()
)

enum class AppRole {
    INTERVIEWEE, INTERVIEWER
}