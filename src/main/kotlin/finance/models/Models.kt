package finance.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Users : Table() {
    val id = integer("id").autoIncrement()
    val fullName = varchar("full_name", 255)
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val createdAt = datetime("created_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class UserRegistrationRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val confirmPassword: String
)

@Serializable
data class UserResponse(
    val id: Int,
    val fullName: String,
    val email: String,
    val createdAt: String
)

@Serializable
data class ErrorResponse(
    val error: String,
    val details: List<String>? = null
)

@Serializable
data class SuccessResponse(
    val message: String,
    val user: UserResponse? = null
)
