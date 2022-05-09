package com.t3ddyss.clother.domain.auth

import androidx.paging.PagingData
import com.t3ddyss.clother.domain.auth.models.User
import com.t3ddyss.clother.domain.offers.OffersInteractor
import com.t3ddyss.clother.domain.offers.models.Offer
import com.t3ddyss.core.util.log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

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

    override fun observeUserInfo(userId: Int): Flow<User> {
        return profileRepository.observeUserInfo(userId)
            .onEach { log("ProfileInteractorImpl.observeUserInfo().onEach: ${it.id}") }
    }
}