package com.zycash.betransaction.constant

object Constant {
    object Response {
        const val SUCCESS_CODE = 200
        const val SUCCESS_MESSAGE = "Success"
        const val CREATED_CODE = 201
        const val CREATED_MESSAGE = "Transaction created successfully"
        const val DELETED_MESSAGE = "Transaction deleted successfully"
    }

    object Message {
        const val NOT_FOUND_MESSAGE = "Transaction not found"
        const val PARSE_ERROR_MESSAGE = "Failed to parse transaction text"
        const val INVALID_REQUEST_MESSAGE = "Invalid transaction request"
    }
}