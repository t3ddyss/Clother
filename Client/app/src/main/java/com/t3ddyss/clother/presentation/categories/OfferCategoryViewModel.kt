package com.t3ddyss.clother.presentation.categories

import androidx.lifecycle.*
import com.t3ddyss.clother.data.OffersRepository
import com.t3ddyss.core.domain.models.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OfferCategoryViewModel
@Inject constructor(
    private val repository: OffersRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val args =
        OfferCategoryFragmentArgs.fromSavedStateHandle(savedStateHandle)
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    init {
        val parentId = args.parentId.takeIf { it != 0 }
        viewModelScope.launch {
            _categories.postValue(repository.getCategories(parentId))
        }
    }
}