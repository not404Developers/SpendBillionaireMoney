package com.example.spendbillionairemoney.model

data class Item(
    val name: String,
    val price: Long,
    val imageResId: Int = 0,
    val imageUri: String? = null
)