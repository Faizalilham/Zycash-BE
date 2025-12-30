package com.zycash.betransaction.controller

import com.zycash.betransaction.dto.Response
import com.zycash.betransaction.dto.TransactionRequest
import com.zycash.betransaction.model.Transaction
import com.zycash.betransaction.service.TransactionService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/transaction")
class TransactionController(
    private val transactionService: TransactionService
) {

    @PostMapping
    fun createTransaction(
        @Valid @RequestBody request: TransactionRequest
    ): ResponseEntity<Response<List<Transaction>>> {
        val response = transactionService.createTransaction(request)
        val status = if (response.responseCode == 201) HttpStatus.CREATED else HttpStatus.INTERNAL_SERVER_ERROR
        return ResponseEntity.status(status).body(response)
    }

    @GetMapping
    fun getAllTransactions(): ResponseEntity<Response<List<Transaction>>> {
        return ResponseEntity.ok(transactionService.getAllTransactions())
    }

    @GetMapping("/{id}")
    fun getTransactionById(@PathVariable id: Long): ResponseEntity<Response<Transaction>> {
        val response = transactionService.getTransactionById(id)
        val status = if (response.responseCode == 200) HttpStatus.OK else HttpStatus.NOT_FOUND
        return ResponseEntity.status(status).body(response)
    }

    @GetMapping("/category/{category}")
    fun getTransactionsByCategory(@PathVariable category: String): ResponseEntity<Response<List<Transaction>>> {
        return ResponseEntity.ok(transactionService.getTransactionsByCategory(category))
    }

    @DeleteMapping("/{id}")
    fun deleteTransaction(@PathVariable id: Long): ResponseEntity<Response<Unit>> {
        val response = transactionService.deleteTransaction(id)
        val status = when (response.responseCode) {
            200 -> HttpStatus.OK
            404 -> HttpStatus.NOT_FOUND
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }
        return ResponseEntity.status(status).body(response)
    }
}