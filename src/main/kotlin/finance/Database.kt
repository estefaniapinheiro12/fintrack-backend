package finance

import finance.models.Users
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase() {
    val dbUrl = System.getenv("DATABASE_URL") 
        ?: throw IllegalStateException("DATABASE_URL não configurada")
    
    println("DEBUG: DATABASE_URL = $dbUrl")
    
    // Remove prefixos jdbc: se existirem
    val cleanUrl = dbUrl
        .replace("jdbc:postgresql://", "")
        .replace("jdbc:postgres://", "")
        .replace("postgres://", "")
        .replace("postgresql://", "")
        .removeSuffix("?sslmode=require")
        .trim()
    
    println("DEBUG: cleanUrl = $cleanUrl")
    
    // Parse: user:pass@host:port/database
    val parts = cleanUrl.split("@")
    if (parts.size != 2) {
        throw IllegalStateException("URL inválida: formato esperado user:pass@host:port/db")
    }
    
    val credentials = parts[0]
    val hostAndDb = parts[1]
    
    val credParts = credentials.split(":")
    if (credParts.size != 2) {
        throw IllegalStateException("Credenciais inválidas")
    }
    
    val username = credParts[0]
    val password = credParts[1]
    
    val dbParts = hostAndDb.split("/")
    if (dbParts.size != 2) {
        throw IllegalStateException("Host/DB inválidos")
    }
    
    val hostPort = dbParts[0]
    val database = dbParts[1]
    
    val host: String
    val port: Int
    
    if (hostPort.contains(":")) {
        val hp = hostPort.split(":")
        host = hp[0]
        port = hp[1].toInt()
    } else {
        host = hostPort
        port = 5432
    }
    
    println("DEBUG: host=$host, port=$port, database=$database, username=$username")
    
    val config = HikariConfig().apply {
        jdbcUrl = "jdbc:postgresql://$host:$port/$database"
        this.username = username
        this.password = password
        driverClassName = "org.postgresql.Driver"
        
        maximumPoolSize = 5
        minimumIdle = 2
        connectionTimeout = 30000
        idleTimeout = 300000
        maxLifetime = 600000
        
        addDataSourceProperty("ssl", "true")
        addDataSourceProperty("sslmode", "require")
    }
    
    val dataSource = HikariDataSource(config)
    Database.connect(datasource = dataSource)
    
    transaction {
        SchemaUtils.create(Users)
    }
}
