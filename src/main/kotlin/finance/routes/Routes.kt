package finance.routes

import finance.models.ErrorResponse
import finance.models.SuccessResponse
import finance.models.UserRegistrationRequest
import finance.models.UserLoginRequest
import finance.services.UserService
import finance.services.ValidationException
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*

fun Application.configureRouting() {
    val userService = UserService()

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
            // REGISTRO (já existia)
            post("/register") {
                try {
                    val request = call.receive<UserRegistrationRequest>()

                    val result = userService.registerUser(request)

                    result.fold(
                        onSuccess = { user ->
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

            // LOGIN (NOVO!) ⬅️
            post("/login") {
                try {
                    val request = call.receive<UserLoginRequest>()

                    val result = userService.loginUser(request)

                    result.fold(
                        onSuccess = { user ->
                            call.respond(
                                HttpStatusCode.OK,
                                SuccessResponse(
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
                                            error = error.errors.first(),
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
    }
}
