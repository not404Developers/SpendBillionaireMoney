package com.example.spendbillionairemoney

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SummaryItem(
    val name: String,
    val price: Long,
    val imageResId: Int,
    val quantity: Int,
    val imageUri: String? = null
) : Parcelable