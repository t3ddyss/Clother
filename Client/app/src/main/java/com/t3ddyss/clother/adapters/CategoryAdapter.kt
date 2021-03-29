package com.t3ddyss.clother.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.t3ddyss.clother.databinding.ListItemCategoryBinding
import com.t3ddyss.clother.models.Category

class CategoryAdapter(
        private val categories: List<Category>,
        private val clickListener: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(ListItemCategoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
        ))
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position].title)
    }

    override fun getItemCount() = categories.size

    inner class CategoryViewHolder(
            val binding:ListItemCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                clickListener.invoke(categories[absoluteAdapterPosition])
            }
        }

        fun bind(title: String) {
            binding.textViewTitle.text = title
        }
    }
}