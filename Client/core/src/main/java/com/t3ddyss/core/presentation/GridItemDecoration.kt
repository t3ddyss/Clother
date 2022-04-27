package com.t3ddyss.core.presentation

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean,
    private val headerNum: Int = 0
    ) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view) - headerNum // item position

        if (position >= 0) {
            val column = position % spanCount // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount
                outRect.right = (column + 1) * spacing / spanCount

                if (position < spanCount) { // top edge
                    outRect.top = spacing
                }
                outRect.bottom = spacing // item bottom
            } else {
                outRect.left = column * spacing / spanCount
                outRect.right = spacing - (column + 1) * spacing / spanCount
                if (position >= spanCount) {
                    outRect.top = spacing // item top
                }
            }
        } else {
            outRect.left = 0
            outRect.right = 0
            outRect.top = 0
            outRect.bottom = 0
        }
    }
}