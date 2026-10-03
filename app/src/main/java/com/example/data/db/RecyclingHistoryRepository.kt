package com.example.data.db

import com.example.model.WasteScanResult
import kotlinx.coroutines.flow.Flow

class RecyclingHistoryRepository(private val dao: ScannedItemDao) {
    val allHistory: Flow<List<ScannedItemEntity>> = dao.getAllHistory()

    suspend fun saveScanResult(result: WasteScanResult): Long {
        val entity = ScannedItemEntity(
            itemName = result.itemName,
            categoryName = result.category.name,
            material = result.material,
            recyclable = result.recyclable,
            hazardous = result.hazardous,
            confidence = result.confidence,
            instructionsRaw = result.instructions.joinToString("\n"),
            instructionsBmRaw = result.instructionsBm.joinToString("\n"),
            estimatedPoints = result.estimatedPoints,
            estimatedWeightKg = result.estimatedWeightKg,
            carbonSavedKg = result.carbonSavedKg
        )
        return dao.insertItem(entity)
    }

    suspend fun deleteItem(id: Long) = dao.deleteById(id)

    suspend fun clearHistory() = dao.clearAll()
}
