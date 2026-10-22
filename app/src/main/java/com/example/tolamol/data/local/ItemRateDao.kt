package com.example.tolamol.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemRateDao {

    @Query("SELECT * FROM item_rates")
    fun getAllItemRates(): Flow<List<ItemRate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItemRate(itemRate: ItemRate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItemRates(items: List<ItemRate>)

    @Update
    suspend fun updateItemRate(itemRate: ItemRate)

    @Delete
    suspend fun deleteItemRate(itemRate: ItemRate)
}
