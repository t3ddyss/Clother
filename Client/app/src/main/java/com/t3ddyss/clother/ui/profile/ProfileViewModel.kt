package com.t3ddyss.clother.ui.profile

import androidx.lifecycle.*
import androidx.paging.*
import com.t3ddyss.clother.data.OffersRepository
import com.t3ddyss.clother.data.UsersRepository
import com.t3ddyss.clother.models.offers.Offer
import com.t3ddyss.clother.models.user.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
@ExperimentalPagingApi
class ProfileViewModel
@Inject constructor(
        private val offersRepository: OffersRepository,
        private val usersRepository: UsersRepository,
        private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _offers = MutableLiveData<PagingData<Offer>>()
    val offers: LiveData<PagingData<Offer>> = _offers

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val isUserLoaded = AtomicBoolean(false)
    private val isOffersLoaded = AtomicBoolean(false)

    fun getUser() {
        if (isUserLoaded.getAndSet(true)) return

        viewModelScope.launch {
            _user.postValue(usersRepository.getCurrentUser())
        }
    }

    fun getOffers(userId: Int) {
        if (isOffersLoaded.getAndSet(true)) return

        viewModelScope.launch {
            offersRepository
            .getOffers(query = mapOf("user" to userId.toString()), userId = userId)
                    .cachedIn(viewModelScope)
                    .collectLatest {
                        _offers.postValue(it)
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