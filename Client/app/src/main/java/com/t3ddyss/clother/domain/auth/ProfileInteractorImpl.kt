package com.t3ddyss.clother.domain.auth

import android.net.Uri
import androidx.paging.PagingData
import com.t3ddyss.clother.domain.auth.models.User
import com.t3ddyss.clother.domain.offers.ImagesInteractor
import com.t3ddyss.clother.domain.offers.OffersInteractor
import com.t3ddyss.clother.domain.offers.models.Offer
import com.t3ddyss.clother.presentation.profile.ValidationError
import com.t3ddyss.clother.util.handleHttpException
import com.t3ddyss.core.domain.models.Error
import com.t3ddyss.core.domain.models.Resource
import com.t3ddyss.core.util.log
import com.t3ddyss.core.util.utils.StringUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class ProfileInteractorImpl @Inject constructor(
    private val authInteractor: AuthInteractor,
    private val offersInteractor: OffersInteractor,
    private val imagesInteractor: ImagesInteractor,
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

    override fun observeCurrentUserInfo(): Flow<Resource<User>> {
        return profileRepository.observeCurrentUserInfo()
            .onEach {
                log("ProfileInteractorImpl.observeCurrentUserInfo() onEach: $it")
            }
    }

    override fun observeUserInfo(userId: Int): Flow<Resource<User>> {
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
    ): Resource<*> {
        return if (!StringUtils.isValidName(name)) {
            Error(content = ValidationError.NAME)
        } else {
            handleHttpException {
                profileRepository.updateCurrentUserInfo(name, status, avatar)
            }
        }
    }
}