package com.example.rraya.bookshelf.hce

data class ActivePayload(
    val name: String,
    val format: PayloadFormat,
    val bytes: ByteArray,
)

