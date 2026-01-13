package finance.routes

import finance.models.*
import finance.services.UserService
import finance.services.CategoryService
import finance.services.TransactionService
import finance.services.ValidationException
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*

fun Application.configureRouting() {
    val userService = UserService()
    val categoryService = CategoryService()
    val transactionService = TransactionService()

    routing {
        get("/") {
            call.respondText("API de Finanças - v1.0.0")
        }

        get("/health") {
            call.respond(mapOf(
                "status" to "UP",
                "timestamp" to System.currentTimeMillis()
            ))
        }

        route("/api/auth") {
            // REGISTRO
            post("/register") {
                try {
                    val request = call.receive<UserRegistrationRequest>()
                    val result = userService.registerUser(request)

                    result.fold(
                        onSuccess = { user ->
                            // Criar categorias padrão para o usuário
                            categoryService.createDefaultCategories(user.id)
                            
                            call.respond(
                                HttpStatusCode.Created,
                                SuccessResponse(
                                    message = "Usuário cadastrado com sucesso!",
                                    user = user
                                )
                            )
                        },
                        onFailure = { error ->
                            when (error) {
                                is ValidationException -> {
                                    call.respond(
                                        HttpStatusCode.BadRequest,
                                        ErrorResponse(
                                            error = "Erro de validação",
                                            details = error.errors
                                        )
                                    )
                                }
                                else -> {
                                    call.respond(
                                        HttpStatusCode.InternalServerError,
                                        ErrorResponse(error = "Erro ao cadastrar usuário")
                                    )
                                }
                            }
                        }
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(error = "Dados inválidos")
                    )
                }
            }

            // LOGIN
            post("/login") {
                try {
                    val request = call.receive<LoginRequest>()
                    val result = userService.loginUser(request)

                    result.fold(
                        onSuccess = { user ->
                            call.respond(
                                HttpStatusCode.OK,
                                LoginResponse(
                                    message = "Login realizado com sucesso!",
                                    user = user
                                )
                            )
                        },
                        onFailure = { error ->
                            when (error) {
                                is ValidationException -> {
                                    call.respond(
                                        HttpStatusCode.Unauthorized,
                                        ErrorResponse(
                                            error = error.errors.firstOrNull() ?: "Credenciais inválidas",
                                            details = null
                                        )
                                    )
                                }
                                else -> {
                                    call.respond(
                                        HttpStatusCode.InternalServerError,
                                        ErrorResponse(error = "Erro ao fazer login")
                                    )
                                }
                            }
                        }
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(error = "Dados inválidos")
                    )
                }
            }
        }

        // ROTAS DE CATEGORIAS
        route("/api/categories") {
            // Listar categorias do usuário
            get("/{userId}") {
                try {
                    val userId = call.parameters["userId"]?.toIntOrNull()
                    
                    if (userId == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(error = "ID de usuário inválido")
                        )
                        return@get
                    }
                    
                    val categories = categoryService.getUserCategories(userId)
                    call.respond(HttpStatusCode.OK, categories)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse(error = "Erro ao buscar categorias")
                    )
                }
            }

            // Criar categoria customizada
            post("/{userId}") {
                try {
                    val userId = call.parameters["userId"]?.toIntOrNull()
                    
                    if (userId == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(error = "ID de usuário inválido")
                        )
                        return@post
                    }
                    
                    val request = call.receive<CreateCategoryRequest>()
                    val result = categoryService.createCategory(userId, request)
                    
                    result.fold(
                        onSuccess = { category ->
                            call.respond(HttpStatusCode.Created, category)
                        },
                        onFailure = {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                ErrorResponse(error = "Erro ao criar categoria")
                            )
                        }
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(error = "Dados inválidos")
                    )
                }
            }
        }

        // ROTAS DE TRANSAÇÕES
        route("/api/transactions") {
            // Dados da Home
            get("/{userId}/home") {
                try {
                    val userId = call.parameters["userId"]?.toIntOrNull()
                    
                    if (userId == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(error = "ID de usuário inválido")
                        )
                        return@get
                    }
                    
                    val homeData = transactionService.getHomeData(userId)
                    call.respond(HttpStatusCode.OK, homeData)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse(error = "Erro ao buscar dados da home")
                    )
                }
            }

            // Listar todas as transações
            get("/{userId}") {
                try {
                    val userId = call.parameters["userId"]?.toIntOrNull()
                    
                    if (userId == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(error = "ID de usuário inválido")
                        )
                        return@get
                    }
                    
                    val transactions = transactionService.getUserTransactions(userId)
                    call.respond(HttpStatusCode.OK, transactions)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse(error = "Erro ao buscar transações")
                    )
                }
            }

            // Criar transação
            post("/{userId}") {
                try {
                    val userId = call.parameters["userId"]?.toIntOrNull()
                    
                    if (userId == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(error = "ID de usuário inválido")
                        )
                        return@post
                    }
                    
                    val request = call.receive<CreateTransactionRequest>()
                    val result = transactionService.createTransaction(userId, request)
                    
                    result.fold(
                        onSuccess = { transaction ->
                            call.respond(HttpStatusCode.Created, transaction)
                        },
                        onFailure = {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                ErrorResponse(error = "Erro ao criar transação")
                            )
                        }
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(error = "Dados inválidos: ${e.message}")
                    )
                }
            }

            // Deletar transação
            delete("/{userId}/{transactionId}") {
                try {
                    val userId = call.parameters["userId"]?.toIntOrNull()
                    val transactionId = call.parameters["transactionId"]?.toIntOrNull()
                    
                    if (userId == null || transactionId == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(error = "IDs inválidos")
                        )
                        return@delete
                    }
                    
                    val result = transactionService.deleteTransaction(userId, transactionId)
                    
                    result.fold(
                        onSuccess = {
                            call.respond(
                                HttpStatusCode.OK,
                                mapOf("message" to "Transação deletada com sucesso")
                            )
                        },
                        onFailure = { error ->
                            call.respond(
                                HttpStatusCode.NotFound,
                                ErrorResponse(error = error.message ?: "Transação não encontrada")
                            )
                        }
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse(error = "Erro ao deletar transação")
                    )
                }
            }
        }
    }
}
