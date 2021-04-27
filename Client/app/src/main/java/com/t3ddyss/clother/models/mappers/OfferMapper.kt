package com.t3ddyss.clother.models.mappers

import com.t3ddyss.clother.models.domain.Offer
import com.t3ddyss.clother.models.dto.OfferDto
import com.t3ddyss.clother.models.entity.OfferEntity

fun mapOfferDtoToEntity(input: OfferDto): OfferEntity {
    return OfferEntity(
        id = input.id,
        categoryId = input.categoryId,
        userId = input.userId,
        userName = input.userName,
        createdAt = input.createdAt,
        title = input.title,
        description = input.description.orEmpty(),
        category = input.category,
        images = input.images,
        size = input.size.orEmpty(),
        location = input.location.orEmpty()
    )
}

fun mapOfferEntityToDomain(input: OfferEntity): Offer {
    return Offer(
        id = input.id,
        userId = input.userId,
        userName = input.userName,
        createdAt = input.createdAt,
        title = input.title,
        description = input.description.orEmpty(),
        category = input.category,
        images = input.images,
        size = input.size.orEmpty(),
        location = input.location.orEmpty()
    )
}

fun mapOfferDtoToDomain(input: OfferDto): Offer {
    return Offer(
        id = input.id,
        userId = input.userId,
        userName = input.userName,
        createdAt = input.createdAt,
        title = input.title,
        description = input.description.orEmpty(),
        category = input.category,
        images = input.images,
        size = input.size.orEmpty(),
        location = input.location.orEmpty()
    )
}