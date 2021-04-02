package com.t3ddyss.clother.ui.offer_category

import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import com.t3ddyss.clother.data.OffersRepository
import com.t3ddyss.clother.models.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalPagingApi
@ExperimentalCoroutinesApi
class OfferCategoryViewModel @Inject constructor(
        private val repository: OffersRepository,
        private val savedStateHandle: SavedStateHandle
): ViewModel() {
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories
    private var currentParentId: Int? = -1 // Since null indicates root categories

    fun getCategories(parentId: Int?) {
        if (parentId == currentParentId) return

        viewModelScope.launch {
            _categories.postValue(repository.getCategories(parentId))
        }
        currentParentId = parentId
    }
}