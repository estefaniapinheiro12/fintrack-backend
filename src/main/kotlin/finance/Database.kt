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
    
    // Parse manual da URL do Neon
    val cleanUrl = dbUrl
        .replace("postgres://", "")
        .replace("postgresql://", "")
    
    val (credentials, hostAndDb) = cleanUrl.split("@")
    val (username, password) = credentials.split(":")
    val (hostPort, database) = hostAndDb.split("/")
    val (host, port) = if (hostPort.contains(":")) {
        hostPort.split(":").let { it[0] to it[1].toInt() }
    } else {
        hostPort to 5432
    }
    
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
