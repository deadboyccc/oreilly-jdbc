package dev.dead.oreillyjdbc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@SpringBootApplication
class OreillyJdbcApplication

@Service
class jdbcCustomerServiceImp(val jdbcTemplate: JdbcTemplate) : CustomerService {
    override fun findAll(): List<Customer> =
        jdbcTemplate.query("select * from customers")
        { resultSet, _ ->
            Customer(resultSet.getInt("id"), resultSet.getString("name"))
        }

    override fun findById(id: Int): Customer? =
        jdbcTemplate.queryForObject("select * from customers where id=?", id)
        { resultSet, _ ->
            Customer(resultSet.getInt("id"), resultSet.getString("name"))
        }
}

data class Customer(val id: Int, val name: String)
interface CustomerService {
    fun findAll(): List<Customer>
    fun findById(id: Int): Customer?
}

@Component
class Initializer(val customerService: CustomerService) {
    @EventListener(ApplicationReadyEvent::class)
    fun read() {
        customerService.findAll().filter { it.id in 1..4 }.forEach {
            println(customerService.findById(it.id))
        }

    }

}


fun main(args: Array<String>) {
    runApplication<OreillyJdbcApplication>(*args)
}
