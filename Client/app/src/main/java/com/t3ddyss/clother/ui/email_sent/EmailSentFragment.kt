package com.t3ddyss.clother.ui.email_sent

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.text.bold
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.FragmentEmailSentBinding


class EmailSentFragment : Fragment() {

    private var _binding: FragmentEmailSentBinding? = null
    private val binding get() = _binding!!
    private val args: EmailSentFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailSentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val message = SpannableStringBuilder()
            .append(args.emailSentMessage)
            .bold { append(args.emailAddress) }
        binding.textViewEmailSent.text = message

        val navController = NavHostFragment.findNavController(this)

        binding.buttonEmailSent.setOnClickListener {
            navController.navigateUp()
        }
    }
}