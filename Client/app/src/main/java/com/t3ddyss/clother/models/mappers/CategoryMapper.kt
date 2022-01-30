package com.t3ddyss.clother.models.mappers

import com.t3ddyss.clother.models.entity.CategoryEntity
import com.t3ddyss.core.domain.Category

fun mapCategoryEntityToDomain(input: CategoryEntity): Category {
    return Category(
        id = input.id,
        title = input.title,
        isLastLevel = input.isLastLevel
    )
}