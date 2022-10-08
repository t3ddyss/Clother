package com.t3ddyss.clother.presentation.profile

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentProfileEditorBinding
import com.t3ddyss.clother.presentation.chat.ImageSelectorDialog
import com.t3ddyss.clother.util.text
import com.t3ddyss.clother.util.toEditable
import com.t3ddyss.core.domain.models.Error
import com.t3ddyss.core.domain.models.Success
import com.t3ddyss.core.presentation.BaseFragment
import com.t3ddyss.core.util.extensions.getThemeColor
import com.t3ddyss.core.util.extensions.showSnackbarWithText
import com.t3ddyss.core.util.utils.ToolbarUtils
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class ProfileEditorFragment :
    BaseFragment<FragmentProfileEditorBinding>(FragmentProfileEditorBinding::inflate) {
    private val viewModel by viewModels<ProfileEditorViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolbarUtils.setupToolbar(
            activity,
            binding.toolbar,
            getString(R.string.profile_edit),
            ToolbarUtils.NavIcon.CLOSE
        )
        setHasOptionsMenu(true)

        binding.avatar.setOnClickListener {
            findNavController().navigate(
                ProfileEditorFragmentDirections.actionProfileEditorFragmentToAvatarMenuDialog(
                    isRemoveVisible = viewModel.avatar.value != null
                )
            )
        }

        subscribeUi()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_apply_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.apply -> {
                viewModel.onApplyClick(
                    nameInput = binding.editTextName.text(),
                    statusInput = binding.editTextStatus.text()
                )
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.updateName(binding.editTextName.text())
        viewModel.updateStatus(binding.editTextStatus.text())
    }

    private fun subscribeUi() {
        viewModel.avatar.observe(viewLifecycleOwner) { avatar ->
            AvatarLoader.loadAvatar(binding.avatar, avatar, R.drawable.ic_avatar_add)
        }

        viewModel.name.observe(viewLifecycleOwner) {
            binding.editTextName.text = it.toEditable()
        }

        viewModel.status.observe(viewLifecycleOwner) {
            binding.editTextStatus.text = it.orEmpty().toEditable()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.layoutLoading.isVisible = it
        }

        viewModel.applyResult.observe(viewLifecycleOwner) {
            when (it) {
                is Success -> {
                    findNavController().popBackStack()
                }
                is Error -> {
                    binding.layoutLoading.isVisible = false
                    when (it.content) {
                        is ValidationError -> {
                            binding.textInputName.error = getString(R.string.auth_name_requirements)
                            binding.textInputName.isErrorEnabled = true
                        }
                        else -> {
                            binding.textInputName.isErrorEnabled = false
                            showSnackbarWithText(it)
                        }
                    }
                }
                else -> Unit
            }
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

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val listener = DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int -> }

        DatePickerDialog(
            requireContext(),
            listener,
            currentYear,
            currentMonth,
            currentDay
        ).apply {
            show()
            datePicker.maxDate = System.currentTimeMillis()

            val buttonTextColor = requireContext().getThemeColor(R.attr.colorOnPrimary)
            getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(buttonTextColor)
            getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(buttonTextColor)
        }
    }
}