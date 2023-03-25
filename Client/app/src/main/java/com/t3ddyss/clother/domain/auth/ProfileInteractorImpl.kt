package com.t3ddyss.clother.domain.auth

import android.net.Uri
import androidx.paging.PagingData
import arrow.core.Either
import arrow.core.Nel
import arrow.core.invalidNel
import arrow.core.traverse
import arrow.core.validNel
import com.t3ddyss.clother.domain.auth.models.User
import com.t3ddyss.clother.domain.auth.models.UserInfoState
import com.t3ddyss.clother.domain.offers.OffersInteractor
import com.t3ddyss.clother.domain.offers.models.Offer
import com.t3ddyss.core.domain.models.ApiCallError
import com.t3ddyss.core.util.log
import com.t3ddyss.core.util.utils.StringUtils
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

class ProfileInteractorImpl @Inject constructor(
    private val authInteractor: AuthInteractor,
    private val offersInteractor: OffersInteractor,
    private val profileRepository: ProfileRepository
) : ProfileInteractor {

    override fun observeOffersByUser(userId: Int): Flow<PagingData<Offer>> {
        val query = mapOf("user" to userId.toString())
        return if (userId == authInteractor.authStateFlow.value.userId) {
            offersInteractor.observeOffersFromDatabase(query = query, userId = userId)
        } else {
            offersInteractor.observeOffersFromNetwork(query = query)
        }
    }

    override fun observeCurrentUserInfo(): Flow<UserInfoState> {
        return profileRepository.observeCurrentUserInfo()
            .onEach {
                log("ProfileInteractorImpl.observeCurrentUserInfo() onEach: $it")
            }
    }

    override fun observeUserInfo(userId: Int): Flow<UserInfoState> {
        return if (userId == authInteractor.authStateFlow.value.userId) {
            profileRepository.observeCurrentUserInfo()
        } else {
            profileRepository.observeUserInfo(userId)
        }.onEach {
            log("ProfileInteractorImpl.observeUserInfo(userId = $userId) onEach: $it")
        }
    }

    override suspend fun updateCurrentUserInfo(
        name: String,
        status: String,
        avatar: Uri?
    ): Either<ApiCallError, User> {
        require(validateParameters(name, status).isRight())
        return profileRepository.updateCurrentUserInfo(name, status, avatar)
    }

    override suspend fun validateParameters(
        name: String,
        status: String
    ): Either<Nel<ProfileInteractor.ProfileParam>, Unit> {
        return ProfileInteractor.ProfileParam.values().asList().traverse { param ->
            when (param) {
                ProfileInteractor.ProfileParam.NAME -> if (StringUtils.isValidName(name)) param.validNel() else param.invalidNel()
                ProfileInteractor.ProfileParam.STATUS -> if (StringUtils.isValidStatus(status)) param.validNel() else param.invalidNel()
            }
        }
            .toEither()
            .void()
    }
}