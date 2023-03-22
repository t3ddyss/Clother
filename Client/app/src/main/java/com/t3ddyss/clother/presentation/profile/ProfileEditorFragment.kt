package com.t3ddyss.clother.presentation.profile

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentProfileEditorBinding
import com.t3ddyss.clother.domain.auth.ProfileInteractor
import com.t3ddyss.clother.presentation.chat.ImageSelectorDialog
import com.t3ddyss.clother.presentation.profile.models.ProfileEditState
import com.t3ddyss.clother.util.extensions.toEditable
import com.t3ddyss.core.presentation.BaseFragment
import com.t3ddyss.core.util.extensions.collectViewLifecycleAware
import com.t3ddyss.core.util.extensions.showSnackbarWithText
import com.t3ddyss.core.util.extensions.textRes
import com.t3ddyss.core.util.utils.ToolbarUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileEditorFragment :
    BaseFragment<FragmentProfileEditorBinding>(FragmentProfileEditorBinding::inflate) {
    private val viewModel by viewModels<ProfileEditorViewModel>()

    private var isApplyEnabled = false
        set(value) {
            if (field != value) {
                requireActivity().invalidateOptionsMenu()
                field = value
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolbarUtils.setupToolbar(
            activity,
            binding.toolbar,
            getString(R.string.profile_edit),
            ToolbarUtils.NavIcon.CLOSE
        )
        setHasOptionsMenu(true)

        binding.avatar.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val action = if (viewModel.change.first().updated.image == null) {
                    ProfileEditorFragmentDirections.actionProfileEditorFragmentToImageSelectorDialog()
                } else {
                    ProfileEditorFragmentDirections.actionProfileEditorFragmentToAvatarMenuDialog()
                }
                findNavController().navigate(action)
            }
        }

        binding.editTextName.doAfterTextChanged { text ->
            viewModel.updateName(text.toString().trim())
        }

        binding.editTextStatus.doAfterTextChanged { text ->
            viewModel.updateStatus(text.toString().trim())
        }

        subscribeUi()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_apply_menu, menu)
        menu.findItem(R.id.apply).isVisible = isApplyEnabled
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.apply -> {
                viewModel.onApplyClick()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun subscribeUi() {
        viewModel.change.collectViewLifecycleAware { change ->
            isApplyEnabled = change.isChanged
            AvatarLoader.loadAvatar(binding.avatar, change.updated.image, R.drawable.ic_avatar_add)
            binding.editTextName.text?.clear()
            binding.editTextName.append(change.updated.name.toEditable())
            binding.editTextStatus.text?.clear()
            binding.editTextStatus.append(change.updated.details?.status.orEmpty().toEditable())
        }

        viewModel.state.collectViewLifecycleAware { state ->
            clearValidationErrors()

            when (state) {
                ProfileEditState.Loading -> {
                    binding.layoutLoading.isVisible = true
                }
                ProfileEditState.Success -> {
                    findNavController().popBackStack()
                }
                ProfileEditState.Error -> {
                    binding.layoutLoading.isVisible = false
                }
                is ProfileEditState.ValidationError -> {
                    setValidationErrors(state)
                }
            }
        }

        viewModel.error.collectViewLifecycleAware { error ->
            error.getContentOrNull()?.let { showSnackbarWithText(it.textRes) }
        }

        setFragmentResultListener(ImageSelectorDialog.SELECTED_IMAGE_KEY) { _, bundle ->
            bundle.getParcelable<Uri>(ImageSelectorDialog.SELECTED_IMAGE_URI)?.let { uri ->
                viewModel.updateAvatar(uri)
            }
        }

        setFragmentResultListener(AvatarMenuDialog.SELECTED_ACTION_KEY) { _, bundle ->
            (bundle.getSerializable(AvatarMenuDialog.SELECTED_ACTION) as? AvatarMenuDialog.Action)?.let { action ->
                if (action == AvatarMenuDialog.Action.REMOVE) {
                    viewModel.removeAvatar()
                }
            }
        }
    }

    private fun clearValidationErrors() {
        binding.textInputName.isErrorEnabled = false
    }

    private fun setValidationErrors(error: ProfileEditState.ValidationError) {
        error.causes.forEach {
            when (it) {
                ProfileInteractor.ProfileParam.NAME -> {
                    binding.textInputName.error = getString(R.string.auth_name_requirements)
                    binding.textInputName.isErrorEnabled = true
                }
                ProfileInteractor.ProfileParam.STATUS -> Unit // Character limit is set in layout
            }
        }
    }
}