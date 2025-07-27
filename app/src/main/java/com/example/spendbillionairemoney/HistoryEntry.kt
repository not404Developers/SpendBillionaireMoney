package com.example.spendbillionairemoney

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class HistoryEntry(
    val billionaireName: String?,
    val billionaireImage: Int,
    val billionaireImageUri: String?,
    val billionaireImagePath: String?,
    val timeTaken: Int,
    val items: List<SummaryItem>?,
    val timestamp: Long
) : Parcelable