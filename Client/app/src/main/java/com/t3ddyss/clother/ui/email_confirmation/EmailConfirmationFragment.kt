package com.t3ddyss.clother.ui.email_confirmation

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.core.text.bold
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentEmailConfirmationBinding
import com.t3ddyss.clother.ui.BaseFragment

class EmailConfirmationFragment
    : BaseFragment<FragmentEmailConfirmationBinding>(FragmentEmailConfirmationBinding::inflate) {

    private val args: EmailConfirmationFragmentArgs by navArgs()

    // TODO move this logic from fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val message = SpannableStringBuilder().append(args.emailActionMessage)
        if (args.emailAddress.isEmpty()) {
            message.append(getString(R.string.your_email))
        } else {
            message.bold { append(args.emailAddress) }
        }
        binding.textViewMessage.text = message

        binding.buttonOkay.setOnClickListener {
            findNavController().navigate(R.id.action_emailActionFragment_to_signInFragment)
        }
    }
}