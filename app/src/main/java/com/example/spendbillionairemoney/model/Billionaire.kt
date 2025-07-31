package com.example.spendbillionairemoney.model

data class Billionaire (
    val name: String,
    val netWorth: Long,
    val imageResId: Int = 0,
    val imagePath: String? = null,
    val isPremium: Boolean = false,
    val isHard: Boolean = false
)