package com.t3ddyss.clother.presentation.categories

import androidx.lifecycle.*
import com.t3ddyss.clother.domain.offers.OffersInteractor
import com.t3ddyss.clother.domain.offers.models.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchByCategoryViewModel @Inject constructor(
    private val offersInteractor: OffersInteractor,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val args = SearchByCategoryFragmentArgs
        .fromSavedStateHandle(savedStateHandle)
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    init {
        val parentId = args.parentId.takeIf { it != 0 }
        viewModelScope.launch {
            _categories.postValue(offersInteractor.getOfferCategories(parentId))
        }
    }
}