package com.t3ddyss.clother.ui.offer_add

import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import com.t3ddyss.clother.data.OffersRepository
import com.t3ddyss.clother.models.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalPagingApi
class AddOfferViewModel @Inject constructor(
        private val repository: OffersRepository,
        private val savedStateHandle: SavedStateHandle
): ViewModel() {
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    fun getCategories(parentId: Int?) {
        viewModelScope.launch {
            val categories = async { repository.getCategories() }
            
            _categories.postValue(repository.getCategories(parentId))
        }
    }
}