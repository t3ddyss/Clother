package com.t3ddyss.clother.domain.auth

import android.net.Uri
import androidx.paging.PagingData
import arrow.core.Either
import arrow.core.Nel
import com.t3ddyss.clother.domain.auth.models.User
import com.t3ddyss.clother.domain.auth.models.UserInfoState
import com.t3ddyss.clother.domain.offers.models.Offer
import com.t3ddyss.core.domain.models.ApiCallError
import kotlinx.coroutines.flow.Flow

interface ProfileInteractor {
    fun observeOffersByUser(userId: Int): Flow<PagingData<Offer>>
    fun observeCurrentUserInfo(): Flow<UserInfoState>
    fun observeUserInfo(userId: Int): Flow<UserInfoState>
    suspend fun updateCurrentUserInfo(name: String, status: String, avatar: Uri?): Either<ApiCallError, User>
    suspend fun validateParameters(name: String, status: String): Either<Nel<ProfileParam>, Unit>

    enum class ProfileParam {
        NAME,
        STATUS
    }
}