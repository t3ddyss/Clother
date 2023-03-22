package com.t3ddyss.clother.data.offers.db.models

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class CategoryInfoEntity(
    @Embedded
    val category: CategoryEntity,
    @ColumnInfo(name = "last")
    val isLast: Boolean
)