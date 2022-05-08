package com.t3ddyss.clother.presentation.categories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t3ddyss.clother.domain.offers.OffersInteractor
import com.t3ddyss.clother.domain.offers.models.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchByCategoryViewModel @Inject constructor(
    private val offersInteractor: OffersInteractor
) : ViewModel() {
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories
    private var currentParentId: Int? = -1 // Since null indicates root categories

    // TODO extract base ViewModel
    fun getCategories(parentId: Int?) {
        if (parentId == currentParentId) return

        viewModelScope.launch {
            _categories.postValue(offersInteractor.getOfferCategories(parentId))
        }
        currentParentId = parentId
    }
}