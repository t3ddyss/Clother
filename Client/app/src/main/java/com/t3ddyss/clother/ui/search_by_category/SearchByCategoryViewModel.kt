package com.t3ddyss.clother.ui.search_by_category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import com.t3ddyss.clother.data.OffersRepository
import com.t3ddyss.clother.models.offers.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
@ExperimentalPagingApi
class SearchByCategoryViewModel @Inject constructor(
        private val repository: OffersRepository
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