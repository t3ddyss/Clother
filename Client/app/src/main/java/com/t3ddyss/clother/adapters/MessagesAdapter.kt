package com.t3ddyss.clother.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.ListItemMessageInBinding
import com.t3ddyss.clother.databinding.ListItemMessageOutBinding
import com.t3ddyss.clother.models.chat.Message
import com.t3ddyss.clother.utilities.formatTime
import java.lang.IllegalArgumentException

class MessagesAdapter(private val interlocutorId: Int
) : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.list_item_message_in -> {
                IncomingMessageViewHolder(ListItemMessageInBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                )
                )
            }

            R.layout.list_item_message_out -> {
                OutgoingMessageViewHolder(ListItemMessageOutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                )
                )
            }

            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is IncomingMessageViewHolder -> holder.bind(getItem(position))
            is OutgoingMessageViewHolder -> holder.bind(getItem(position))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).userId == interlocutorId) R.layout.list_item_message_in
        else R.layout.list_item_message_out
    }

    class IncomingMessageViewHolder(
            private val binding: ListItemMessageInBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.textViewBody.requestLayout()

            binding.textViewBody.text = message.body
            binding.textViewTime.text = message.createdAt.formatTime()
        }
    }

    class OutgoingMessageViewHolder(
            private val binding: ListItemMessageOutBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.textViewBody.requestLayout()

            binding.textViewBody.text = message.body
            binding.textViewTime.text = message.createdAt.formatTime()
        }
    }
}