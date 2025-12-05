package finance

import finance.models.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import io.ktor.server.application.*

fun Application.configureDatabase() {
    val database = Database.connect(
        url = "jdbc:h2:mem:finance_app;DB_CLOSE_DELAY=-1",
        driver = "org.h2.Driver"
    )

    transaction(database) {
        SchemaUtils.create(Users)
    }
}