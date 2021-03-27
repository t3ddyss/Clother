package com.t3ddyss.clother.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.t3ddyss.clother.data.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UsersRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

}