package com.t3ddyss.clother.db

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.t3ddyss.clother.models.Category

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<Category>)

    @Query("SELECT COUNT(*) FROM category")
    suspend fun getCategoriesCount(): Int

    @Query("SELECT * FROM category WHERE (:parentId IS NULL AND parent_id IS NULL) OR (:parentId IS NOT NULL AND parent_id == :parentId)")
    suspend fun getSubcategories(parentId: Int?): List<Category>

    // Helper method which loads data into actual .db file from .db-wal and .db-shm files
    // for prepopulation purposes
    @RawQuery
    suspend fun loadDataIntoDb(
            query: SupportSQLiteQuery = SimpleSQLiteQuery("pragma wal_checkpoint;")): Int?
}