package com.example.util

import android.util.Log

enum class OperationType {
    GET, LIST, CREATE, UPDATE, DELETE, BATCH
}

fun handleFirestoreError(e: Exception, operation: OperationType, path: String) {
    Log.e(
        "FirestoreError",
        """{"event":"firestore_error","operation":"$operation","path":"$path","error":"${e.message}"}"""
    )
}
