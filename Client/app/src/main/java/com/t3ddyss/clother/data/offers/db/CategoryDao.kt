package com.t3ddyss.clother.data.offers.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.t3ddyss.clother.data.offers.db.models.CategoryEntity

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Query("SELECT * FROM category WHERE (:parentId IS NULL AND parent_id IS NULL) OR (:parentId IS NOT NULL AND parent_id == :parentId)")
    suspend fun getSubcategories(parentId: Int?): List<CategoryEntity>
}