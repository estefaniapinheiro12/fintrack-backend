package finance

import finance.models.Users
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URI 

fun Application.configureDatabase() {
    val dbUrl = System.getenv("DATABASE_URL") 
        ?: "jdbc:postgresql://localhost:5432/fintrack_dev"
    
    val config = HikariConfig().apply {
        // Parse correto da URL do Neon
        if (dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://")) {
            val uri = URI(dbUrl.replace("postgres://", "postgresql://"))
            
            jdbcUrl = "jdbc:postgresql://${uri.host}:${uri.port}${uri.path}"
            username = uri.userInfo?.split(":")?.get(0)
            password = uri.userInfo?.split(":")?.get(1)
            
            // SSL obrigatório para Neon
            addDataSourceProperty("ssl", "true")
            addDataSourceProperty("sslmode", "require")
        } else {
            // Já está no formato JDBC
            jdbcUrl = dbUrl
        }
        
        driverClassName = "org.postgresql.Driver"
        maximumPoolSize = 5
        minimumIdle = 2
        connectionTimeout = 30000
        idleTimeout = 300000
        maxLifetime = 600000
    }
    
    val dataSource = HikariDataSource(config)
    Database.connect(dataSource)
    
    transaction {
        SchemaUtils.create(Users)
    }
}
