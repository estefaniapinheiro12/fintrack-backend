package finance.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.math.BigDecimal
import java.time.LocalDateTime

object Transactions : Table() {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id)
    val categoryId = integer("category_id").references(Categories.id)
    val type = varchar("type", 20) // "income" ou "expense"
    val amount = decimal("amount", 15, 2)
    val description = varchar("description", 255).nullable()
    val date = datetime("date")
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    
    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class TransactionResponse(
    val id: Int,
    val categoryId: Int,
    val categoryName: String,
    val categoryColor: String,
    val categoryIcon: String,
    val type: String,
    val amount: String, // Vou passar como String para evitar problemas de serialização
    val description: String?,
    val date: String,
    val createdAt: String
)

@Serializable
data class CreateTransactionRequest(
    val categoryId: Int,
    val type: String,
    val amount: String,
    val description: String?,
    val date: String // Formato: "2026-01-07T10:30:00"
)

@Serializable
data class HomeDataResponse(
    val totalBalance: String,
    val monthIncome: String,
    val monthExpense: String,
    val monthChange: String,
    val categoryExpenses: List<CategoryExpense>,
    val recentTransactions: List<TransactionResponse>
)

@Serializable
data class CategoryExpense(
    val categoryName: String,
    val amount: String,
    val color: String,
    val percentage: Double
)
