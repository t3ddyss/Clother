package com.t3ddyss.clother.data.offers.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.t3ddyss.clother.data.offers.db.models.CategoryEntity
import com.t3ddyss.clother.data.offers.db.models.CategoryInfoEntity

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Query("""SELECT c.*,
        (SELECT COUNT(*) FROM category WHERE parent_id == c.id) == 0 AS last
        FROM category c
        WHERE (:parentId IS NULL AND parent_id IS NULL)
        OR (:parentId IS NOT NULL AND parent_id == :parentId)
    """)
    suspend fun getCategories(parentId: Int?): List<CategoryInfoEntity>
}