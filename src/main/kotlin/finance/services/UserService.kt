package finance.services

import finance.models.LoginRequest
import finance.models.UserRegistrationRequest
import finance.models.UserResponse
import finance.models.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

class UserService {
    
    fun registerUser(request: UserRegistrationRequest): Result<UserResponse> {
        val validationErrors = validateRegistration(request)
        if (validationErrors.isNotEmpty()) {
            return Result.failure(ValidationException(validationErrors))
        }
        
        return try {
            transaction {
                val existingUser = Users.select { Users.email eq request.email }
                    .singleOrNull()
                    
                if (existingUser != null) {
                    return@transaction Result.failure(
                        ValidationException(listOf("Email já cadastrado"))
                    )
                }
                
                val passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt(12))
                
                val userId = Users.insert {
                    it[fullName] = request.fullName
                    it[email] = request.email.lowercase()
                    it[Users.passwordHash] = passwordHash
                } get Users.id
                
                val user = Users.select { Users.id eq userId }
                    .single()
                    
                Result.success(UserResponse(
                    id = user[Users.id],
                    fullName = user[Users.fullName],
                    email = user[Users.email],
                    createdAt = user[Users.createdAt].toString()
                ))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // NOVA FUNÇÃO DE LOGIN
    fun loginUser(request: LoginRequest): Result<UserResponse> {
        val validationErrors = validateLogin(request)
        if (validationErrors.isNotEmpty()) {
            return Result.failure(ValidationException(validationErrors))
        }
        
        return try {
            transaction {
                val user = Users.select { Users.email eq request.email.lowercase() }
                    .singleOrNull()
                
                if (user == null) {
                    return@transaction Result.failure(
                        ValidationException(listOf("Email ou senha incorretos"))
                    )
                }
                
                val passwordMatch = BCrypt.checkpw(request.password, user[Users.passwordHash])
                
                if (!passwordMatch) {
                    return@transaction Result.failure(
                        ValidationException(listOf("Email ou senha incorretos"))
                    )
                }
                
                Result.success(UserResponse(
                    id = user[Users.id],
                    fullName = user[Users.fullName],
                    email = user[Users.email],
                    createdAt = user[Users.createdAt].toString()
                ))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun validateLogin(request: LoginRequest): List<String> {
        val errors = mutableListOf<String>()
        
        if (request.email.isBlank()) {
            errors.add("Email é obrigatório")
        }
        
        if (request.password.isBlank()) {
            errors.add("Senha é obrigatória")
        }
        
        return errors
    }
    
    private fun validateRegistration(request: UserRegistrationRequest): List<String> {
        val errors = mutableListOf<String>()
        
        if (request.fullName.isBlank()) {
            errors.add("Nome completo é obrigatório")
        } else if (request.fullName.trim().split(" ").size < 2) {
            errors.add("Informe nome e sobrenome")
        } else if (request.fullName.length < 3) {
            errors.add("Nome deve ter no mínimo 3 caracteres")
        }
        
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        if (request.email.isBlank()) {
            errors.add("Email é obrigatório")
        } else if (!request.email.matches(emailRegex)) {
            errors.add("Email inválido")
        }
        
        if (request.password.isBlank()) {
            errors.add("Senha é obrigatória")
        } else if (request.password.length < 8) {
            errors.add("Senha deve ter no mínimo 8 caracteres")
        } else if (!request.password.any { it.isUpperCase() }) {
            errors.add("Senha deve conter pelo menos uma letra maiúscula")
        } else if (!request.password.any { it.isLowerCase() }) {
            errors.add("Senha deve conter pelo menos uma letra minúscula")
        } else if (!request.password.any { it.isDigit() }) {
            errors.add("Senha deve conter pelo menos um número")
        }
        
        if (request.password != request.confirmPassword) {
            errors.add("As senhas não coincidem")
        }
        
        return errors
    }
}

class ValidationException(val errors: List<String>) : Exception(errors.joinToString(", "))
