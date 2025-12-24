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
        ?: "jdbc:postgresql://localhost:5432/fintrack_dev"
    
    val config = HikariConfig().apply {
        jdbcUrl = if (dbUrl.startsWith("postgres://")) {
            dbUrl.replace("postgres://", "jdbc:postgresql://")
        } else {
            dbUrl
        }
        driverClassName = "org.postgresql.Driver"
        maximumPoolSize = 5
        minimumIdle = 2
        connectionTimeout = 30000
        idleTimeout = 300000
        maxLifetime = 600000
        
        addDataSourceProperty("sslmode", "require")
    }
    
    val dataSource = HikariDataSource(config)
    Database.connect(dataSource)
    
    transaction {
        SchemaUtils.create(Users)
    }
}
