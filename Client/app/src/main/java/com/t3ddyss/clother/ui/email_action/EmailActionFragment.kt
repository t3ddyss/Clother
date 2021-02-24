package com.t3ddyss.clother.ui.email_action

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.bold
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import com.t3ddyss.clother.databinding.FragmentEmailActionBinding

class EmailActionFragment : Fragment() {

    private var _binding: FragmentEmailActionBinding? = null
    private val binding get() = _binding!!

    private val navController by lazy { NavHostFragment.findNavController(this) }
    private val args: EmailActionFragmentArgs by navArgs()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailActionBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val message = SpannableStringBuilder()
                .append(args.emailActionMessage)
                .bold { append(args.emailAddress) }
        binding.textViewEmailSent.text = message

        binding.buttonEmailSent.setOnClickListener {
            navController.navigateUp()
        }
    }
}