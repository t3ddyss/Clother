package com.t3ddyss.clother.models.mappers

import com.t3ddyss.clother.models.domain.Response
import com.t3ddyss.clother.models.dto.ResponseDto

fun mapResponseDtoToDomain(input: ResponseDto): Response {
    return Response(
        message = input.message.orEmpty()
    )
}