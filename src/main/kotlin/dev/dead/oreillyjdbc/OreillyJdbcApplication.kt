package dev.dead.oreillyjdbc

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import javax.sql.DataSource

@SpringBootApplication
open class OreillyJdbcApplication

// --- Domain ---
data class Customer(val id: Int, val name: String)

// --- Service Interface ---
interface CustomerService {
    fun findAll(): List<Customer>
    fun findById(id: Int): Customer?
}

// --- Exposed ORM ---
object Customers : Table("customers") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    override val primaryKey = PrimaryKey(id, name = "PK_CUSTOMER_ID")
}

/**
 * Use this bean by default. If you want to use JdbcTemplate instead,
 * activate the "jdbcTemplate" profile so this bean is skipped.
 */
@Profile("!jdbcTemplate")
@Service
class ExposedOrmCustomerService(private val dataSource: DataSource) : CustomerService {
    init {
        // If you're using the exposed-spring-boot-starter this explicit connect may be unnecessary.
        Database.connect(dataSource)
        transaction {
            SchemaUtils.create(Customers)
        }
    }

    override fun findAll(): List<Customer> = transaction {
        Customers.selectAll()
            .map { Customer(it[Customers.id], it[Customers.name]) }
    }

    override fun findById(id: Int): Customer? = transaction {
        Customers
            .select { Customers.id eq id }
            .map { Customer(it[Customers.id], it[Customers.name]) }
            .singleOrNull()
    }
}

// --- JDBC Template ---
@Profile("jdbcTemplate")
@Service
class JdbcCustomerService(private val jdbcTemplate: JdbcTemplate) : CustomerService {
    override fun findAll(): List<Customer> =
        jdbcTemplate.query("SELECT id, name FROM customers") { rs, _ ->
            Customer(rs.getInt("id"), rs.getString("name"))
        }

    override fun findById(id: Int): Customer? {
        val results: List<Customer> = jdbcTemplate.query(
            "SELECT id, name FROM customers WHERE id = ?",
            arrayOf(id)
        ) { rs, _ ->
            Customer(rs.getInt("id"), rs.getString("name"))
        }
        return results.firstOrNull()
    }
}

// --- Initializer ---
@Component
class Initializer(private val customerService: CustomerService) {
    @EventListener(ApplicationReadyEvent::class)
    fun read() {
        customerService.findAll()
            .filter { it.id in 1..4 }
            .forEach { println(customerService.findById(it.id)) }
    }
}

// --- Main ---
fun main(args: Array<String>) =
    runApplication<OreillyJdbcApplication>(*args)
