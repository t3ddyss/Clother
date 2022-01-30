package com.t3ddyss.clother.models.mappers

import com.t3ddyss.clother.models.dto.UserDto
import com.t3ddyss.clother.models.entity.UserEntity
import com.t3ddyss.core.domain.User

fun mapUserDtoToDomain(input: UserDto): User {
    return User(
        id = input.id,
        name = input.name,
        email = input.email.orEmpty(),
        image = input.image ?: ""
    )
}

fun mapUserDtoToEntity(input: UserDto): UserEntity {
    return UserEntity(
        id = input.id,
        name = input.name,
        email = input.email,
        image = input.image
    )
}

fun mapUserDomainToEntity(input: User): UserEntity {
    return UserEntity(
        id = input.id,
        name = input.name,
        email = input.email,
        image = input.image
    )
}

fun mapUserEntityToDomain(input: UserEntity): User {
    return User(
        id = input.id,
        name = input.name,
        email = input.email ?: "",
        image = input.image ?: ""
    )
}