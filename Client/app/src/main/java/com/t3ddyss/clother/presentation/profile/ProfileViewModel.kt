package com.t3ddyss.clother.presentation.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.t3ddyss.clother.data.auth.db.UserDao
import com.t3ddyss.clother.domain.auth.AuthInteractor
import com.t3ddyss.clother.domain.auth.ProfileInteractor
import com.t3ddyss.clother.domain.auth.models.User
import com.t3ddyss.clother.domain.offers.models.Offer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileInteractor: ProfileInteractor,
    authInteractor: AuthInteractor,
    userDao: UserDao
) : ViewModel() {
    private val _offers = MutableLiveData<PagingData<Offer>>()
    val offers: LiveData<PagingData<Offer>> = _offers

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    init {
        val userId = authInteractor.authStateFlow.value.userId ?: 0
        viewModelScope.launch {
            launch {
                profileInteractor
                    .observeOffersByUser(userId)
                    .cachedIn(viewModelScope)
                    .collectLatest {
                        _offers.postValue(it)
                    }
            }
            // TODO change to details request
            launch {
//                _user.postValue(userDao.getUserWithDetailsById(userId).toDomain())
            }
        }
    }

    fun removeOffers(removedOffers: Set<Int>) {
        val currentOffers = offers.value

        viewModelScope.launch {
            currentOffers?.filter {
                it.id !in removedOffers
            }?.let {
                _offers.postValue(it)
            }
        }
    }
}