package com.t3ddyss.clother.presentation.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.t3ddyss.clother.databinding.ListItemOnboardingCardBinding

class OnboardingAdapter(
    private val cards: List<OnboardingCard>
) : RecyclerView.Adapter<OnboardingAdapter.OnboardingCardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingCardViewHolder {
        return OnboardingCardViewHolder(
            ListItemOnboardingCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: OnboardingCardViewHolder, position: Int) {
        holder.bind(cards[position])
    }

    override fun getItemCount(): Int = cards.size

    inner class OnboardingCardViewHolder(
        private val binding: ListItemOnboardingCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(card: OnboardingCard) {
            binding.icon.setImageResource(card.icon)
            binding.title.setText(card.title)
            binding.description.setText(card.description)
        }
    }
}