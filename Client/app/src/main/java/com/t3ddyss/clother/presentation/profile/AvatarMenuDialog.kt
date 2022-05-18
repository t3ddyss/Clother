package com.t3ddyss.clother.presentation.profile

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.DialogAvatarMenuBinding
import com.t3ddyss.core.util.extensions.showSnackbarWithAction
import com.t3ddyss.core.util.utils.IntentUtils
import com.t3ddyss.navigation.util.setNavigationResult

class AvatarMenuDialog : BottomSheetDialogFragment() {
    private var _binding: DialogAvatarMenuBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<AvatarMenuDialogArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAvatarMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.remove.isVisible = args.isRemoveVisible

        val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                findNavController().navigate(
                    AvatarMenuDialogDirections.actionAvatarMenuDialogToImageSelectorDialog()
                )
            } else {
                showSnackbarWithAction(
                    text = R.string.error_no_files_access,
                    actionText = R.string.action_grant_access,
                    action = { IntentUtils.openApplicationSettings(requireContext()) }
                )
            }
        }
        binding.remove.setOnClickListener {
            setNavigationResult(SELECTED_ACTION, Action.REMOVE.name)
            findNavController().popBackStack()
        }
        binding.upload.setOnClickListener {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    enum class Action {
        REMOVE
    }

    companion object {
        const val SELECTED_ACTION = "selected_action"
    }
}