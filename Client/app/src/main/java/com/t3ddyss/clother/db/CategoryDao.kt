package com.t3ddyss.clother.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.t3ddyss.clother.models.Category

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<Category>)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoriesCount(): Int

    @Query("SELECT COUNT(*) FROM categories WHERE parent_id == :parentId")
    suspend fun getSubcategoriesCount(parentId: Int?): Int

    @Query("SELECT * FROM categories WHERE parent_id == :parentId")
    suspend fun getSubcategories(parentId: Int?): List<Category>
}