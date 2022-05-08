package com.t3ddyss.clother.data.offers.db.models

import androidx.room.Embedded
import com.t3ddyss.clother.data.auth.db.models.UserEntity

data class OfferWithUserEntity(
    @Embedded
    val offer: OfferEntity,
    @Embedded
    val user: UserEntity
)