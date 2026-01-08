package finance.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Categories : Table() {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id).nullable() // null = categoria padrão do sistema
    val name = varchar("name", 100)
    val type = varchar("type", 20) // "income" ou "expense"
    val color = varchar("color", 20) // "#FF6B6B"
    val icon = varchar("icon", 50) // "restaurant", "car", "home"
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    
    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class CategoryResponse(
    val id: Int,
    val name: String,
    val type: String,
    val color: String,
    val icon: String
)

@Serializable
data class CreateCategoryRequest(
    val name: String,
    val type: String,
    val color: String,
    val icon: String
)
