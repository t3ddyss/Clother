package com.t3ddyss.clother.domain.auth

import androidx.paging.PagingData
import com.t3ddyss.clother.domain.auth.models.User
import com.t3ddyss.clother.domain.offers.models.Offer
import com.t3ddyss.core.domain.models.Resource
import kotlinx.coroutines.flow.Flow

interface ProfileInteractor {
    fun observeOffersByUser(userId: Int): Flow<PagingData<Offer>>
    fun observeCurrentUserInfo(): Flow<Resource<User>>
    fun observeUserInfo(userId: Int): Flow<Resource<User>>
    suspend fun updateCurrentUserInfo(name: String, status: String, avatar: String?): Resource<*>
}