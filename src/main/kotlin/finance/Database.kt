package finance

import finance.models.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import io.ktor.server.application.*

fun Application.configureDatabase() {
    // Pega a URL do banco das variáveis de ambiente
    val databaseUrl = System.getenv("DATABASE_URL") 
        ?: "jdbc:h2:mem:finance_app;DB_CLOSE_DELAY=-1" 
    
    // Determina o driver baseado na URL
    val driver = if (databaseUrl.contains("postgresql")) {
        "org.postgresql.Driver"
    } else {
        "org.h2.Driver"
    }
    
    val database = Database.connect(
        url = databaseUrl,
        driver = driver
    )

    // Cria as tabelas se não existirem
    transaction(database) {
        SchemaUtils.create(Users)
    }
}
