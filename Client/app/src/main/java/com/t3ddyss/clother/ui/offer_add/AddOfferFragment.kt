package com.t3ddyss.clother.ui.offer_add

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.t3ddyss.clother.R

class AddOfferFragment : Fragment() {

    private lateinit var offerViewModel: AddOfferViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_offer_add, container, false)
    }
}