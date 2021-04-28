package com.t3ddyss.clother.models.mappers

import com.t3ddyss.clother.models.domain.Category
import com.t3ddyss.clother.models.entity.CategoryEntity

fun mapCategoryEntityToDomain(input: CategoryEntity): Category {
    return Category(
        id = input.id,
        title = input.title,
        isLastLevel = input.isLastLevel
    )
}