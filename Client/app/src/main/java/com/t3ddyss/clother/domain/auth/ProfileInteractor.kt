package com.t3ddyss.clother.domain.auth

import androidx.paging.PagingData
import com.t3ddyss.clother.domain.auth.models.User
import com.t3ddyss.clother.domain.offers.models.Offer
import kotlinx.coroutines.flow.Flow

interface ProfileInteractor {
    fun observeOffersByUser(userId: Int): Flow<PagingData<Offer>>
    fun observeUserInfo(userId: Int): Flow<User>
}