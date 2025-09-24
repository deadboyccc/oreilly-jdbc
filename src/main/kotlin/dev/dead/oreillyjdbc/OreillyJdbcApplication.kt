package dev.dead.oreillyjdbc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Service

@SpringBootApplication
class OreillyJdbcApplication
@Service
class jdbcCustomerServiceImp : CustomerService {
    override fun findAll(): List<Customer> {
        TODO("Not yet implemented")
    }

    override fun findById(id: Int): Customer? {
        TODO("Not yet implemented")
    }
}
data class Customer(val id: Int, val name: String)
interface CustomerService {
    fun findAll(): List<Customer>
    fun findById(id: Int): Customer?
}

fun main(args: Array<String>) {
    runApplication<OreillyJdbcApplication>(*args)
}
