package finance.services

import finance.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDateTime

class TransactionService {
    
    // Criar transação
    fun createTransaction(userId: Int, request: CreateTransactionRequest): Result<TransactionResponse> {
        return try {
            transaction {
                val transactionId = Transactions.insert {
                    it[Transactions.userId] = userId
                    it[categoryId] = request.categoryId
                    it[type] = request.type
                    it[amount] = BigDecimal(request.amount)
                    it[description] = request.description
                    it[date] = LocalDateTime.parse(request.date)
                } get Transactions.id
                
                val transaction = (Transactions innerJoin Categories)
                    .select { Transactions.id eq transactionId }
                    .single()
                
                Result.success(TransactionResponse(
                    id = transaction[Transactions.id],
                    categoryId = transaction[Transactions.categoryId],
                    categoryName = transaction[Categories.name],
                    categoryColor = transaction[Categories.color],
                    categoryIcon = transaction[Categories.icon],
                    type = transaction[Transactions.type],
                    amount = transaction[Transactions.amount].toString(),
                    description = transaction[Transactions.description],
                    date = transaction[Transactions.date].toString(),
                    createdAt = transaction[Transactions.createdAt].toString()
                ))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Listar todas as transações do usuário
    fun getUserTransactions(userId: Int): List<TransactionResponse> {
        return transaction {
            (Transactions innerJoin Categories)
                .select { Transactions.userId eq userId }
                .orderBy(Transactions.date to SortOrder.DESC)
                .map { row ->
                    TransactionResponse(
                        id = row[Transactions.id],
                        categoryId = row[Transactions.categoryId],
                        categoryName = row[Categories.name],
                        categoryColor = row[Categories.color],
                        categoryIcon = row[Categories.icon],
                        type = row[Transactions.type],
                        amount = row[Transactions.amount].toString(),
                        description = row[Transactions.description],
                        date = row[Transactions.date].toString(),
                        createdAt = row[Transactions.createdAt].toString()
                    )
                }
        }
    }
    
    // Dados da Home (saldo, receitas, despesas, categorias, transações recentes)
    fun getHomeData(userId: Int): HomeDataResponse {
        return transaction {
            val now = LocalDateTime.now()
            val startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0)
            
            // Buscar todas as transações
            val allTransactions = Transactions.select { Transactions.userId eq userId }
            
            // Buscar transações do mês atual
            val monthTransactions = Transactions.select { 
                (Transactions.userId eq userId) and (Transactions.date greaterEq startOfMonth)
            }
            
            // Calcular totais gerais
            val totalIncome = allTransactions
                .filter { it[Transactions.type] == "income" }
                .sumOf { it[Transactions.amount] }
            
            val totalExpense = allTransactions
                .filter { it[Transactions.type] == "expense" }
                .sumOf { it[Transactions.amount] }
            
            val totalBalance = totalIncome - totalExpense
            
            // Calcular totais do mês
            val monthIncome = monthTransactions
                .filter { it[Transactions.type] == "income" }
                .sumOf { it[Transactions.amount] }
            
            val monthExpense = monthTransactions
                .filter { it[Transactions.type] == "expense" }
                .sumOf { it[Transactions.amount] }
            
            val monthChange = monthIncome - monthExpense
            
            // Gastos por categoria (do mês)
            val categoryExpenses = (Transactions innerJoin Categories)
                .select { 
                    (Transactions.userId eq userId) and 
                    (Transactions.type eq "expense") and
                    (Transactions.date greaterEq startOfMonth)
                }
                .groupBy { it[Categories.id] }
                .map { (categoryId, rows) ->
                    val total = rows.sumOf { it[Transactions.amount] }
                    val percentage = if (monthExpense > BigDecimal.ZERO) {
                        (total.toDouble() / monthExpense.toDouble() * 100)
                    } else 0.0
                    
                    CategoryExpense(
                        categoryName = rows.first()[Categories.name],
                        amount = total.toString(),
                        color = rows.first()[Categories.color],
                        percentage = percentage
                    )
                }
                .sortedByDescending { it.amount.toDouble() }
                .take(5)
            
            // Transações recentes (últimas 5)
            val recentTransactions = (Transactions innerJoin Categories)
                .select { Transactions.userId eq userId }
                .orderBy(Transactions.date to SortOrder.DESC)
                .limit(5)
                .map { row ->
                    TransactionResponse(
                        id = row[Transactions.id],
                        categoryId = row[Transactions.categoryId],
                        categoryName = row[Categories.name],
                        categoryColor = row[Categories.color],
                        categoryIcon = row[Categories.icon],
                        type = row[Transactions.type],
                        amount = row[Transactions.amount].toString(),
                        description = row[Transactions.description],
                        date = row[Transactions.date].toString(),
                        createdAt = row[Transactions.createdAt].toString()
                    )
                }
            
            HomeDataResponse(
                totalBalance = totalBalance.toString(),
                monthIncome = monthIncome.toString(),
                monthExpense = monthExpense.toString(),
                monthChange = monthChange.toString(),
                categoryExpenses = categoryExpenses,
                recentTransactions = recentTransactions
            )
        }
    }
    
    // Deletar transação - CORRIGIDO
    fun deleteTransaction(userId: Int, transactionId: Int): Result<Boolean> {
        return try {
            transaction {
                val deleted = Transactions.deleteWhere { 
                    (id eq transactionId) and (Transactions.userId eq userId)
                }
                
                if (deleted > 0) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("Transação não encontrada"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
