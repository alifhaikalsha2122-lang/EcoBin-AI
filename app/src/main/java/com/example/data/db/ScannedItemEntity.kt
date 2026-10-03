package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.WasteCategory
import com.example.model.WasteScanResult

@Entity(tableName = "recycling_history")
data class ScannedItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemName: String,
    val categoryName: String,
    val material: String,
    val recyclable: Boolean,
    val hazardous: Boolean,
    val confidence: Float,
    val instructionsRaw: String,
    val instructionsBmRaw: String,
    val estimatedPoints: Int,
    val estimatedWeightKg: Float,
    val carbonSavedKg: Float,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toWasteScanResult(): WasteScanResult {
        val category = try {
            WasteCategory.valueOf(categoryName)
        } catch (e: Exception) {
            WasteCategory.GENERAL
        }

        val instructions = if (instructionsRaw.isNotBlank()) {
            instructionsRaw.split("\n").filter { it.isNotBlank() }
        } else {
            emptyList()
        }

        val instructionsBm = if (instructionsBmRaw.isNotBlank()) {
            instructionsBmRaw.split("\n").filter { it.isNotBlank() }
        } else {
            emptyList()
        }

        return WasteScanResult(
            itemName = itemName,
            category = category,
            material = material,
            recyclable = recyclable,
            hazardous = hazardous,
            confidence = confidence,
            instructions = instructions,
            instructionsBm = instructionsBm,
            estimatedPoints = estimatedPoints,
            estimatedWeightKg = estimatedWeightKg,
            carbonSavedKg = carbonSavedKg
        )
    }
}
