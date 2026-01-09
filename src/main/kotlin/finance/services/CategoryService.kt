package finance.services

import finance.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class CategoryService {
    
    // Cria categorias padrão quando o usuário se registra
    fun createDefaultCategories(userId: Int) {
        transaction {
            val defaultCategories = listOf(
                // Categorias de Despesa
                CategoryData("Alimentação", "expense", "#FF6B6B", "restaurant"),
                CategoryData("Transporte", "expense", "#FFB800", "car"),
                CategoryData("Moradia", "expense", "#3B82F6", "home"),
                CategoryData("Lazer", "expense", "#8B5CF6", "game"),
                CategoryData("Saúde", "expense", "#EC4899", "medical"),
                CategoryData("Educação", "expense", "#10B981", "school"),
                CategoryData("Compras", "expense", "#F59E0B", "shopping"),
                CategoryData("Contas", "expense", "#EF4444", "bill"),
                
                // Categorias de Receita
                CategoryData("Salário", "income", "#4CAF50", "money"),
                CategoryData("Freelance", "income", "#66BB6A", "work"),
                CategoryData("Investimentos", "income", "#81C784", "chart"),
                CategoryData("Outros", "income", "#A5D6A7", "other")
            )
            
            defaultCategories.forEach { cat ->
                Categories.insert {
                    it[Categories.userId] = userId
                    it[name] = cat.name
                    it[type] = cat.type
                    it[color] = cat.color
                    it[icon] = cat.icon
                }
            }
        }
    }
    
    // Lista categorias do usuário
    fun getUserCategories(userId: Int): List<CategoryResponse> {
        return transaction {
            Categories.select { Categories.userId eq userId }
                .map { row ->
                    CategoryResponse(
                        id = row[Categories.id],
                        name = row[Categories.name],
                        type = row[Categories.type],
                        color = row[Categories.color],
                        icon = row[Categories.icon]
                    )
                }
        }
    }
    
    // Cria categoria customizada
    fun createCategory(userId: Int, request: CreateCategoryRequest): Result<CategoryResponse> {
        return try {
            transaction {
                val categoryId = Categories.insert {
                    it[Categories.userId] = userId
                    it[name] = request.name
                    it[type] = request.type
                    it[color] = request.color
                    it[icon] = request.icon
                } get Categories.id
                
                val category = Categories.select { Categories.id eq categoryId }
                    .single()
                
                Result.success(CategoryResponse(
                    id = category[Categories.id],
                    name = category[Categories.name],
                    type = category[Categories.type],
                    color = category[Categories.color],
                    icon = category[Categories.icon]
                ))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class CategoryData(
    val name: String,
    val type: String,
    val color: String,
    val icon: String
)
