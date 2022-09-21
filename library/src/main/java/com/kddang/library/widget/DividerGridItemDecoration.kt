package com.kddang.library.widget

import android.R
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager


class DividerGridItemDecoration(context: Context) : RecyclerView.ItemDecoration() {
    private val mDivider: Drawable?

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        drawHorizontal(c, parent)
        drawVertical(c, parent)
    }

    private fun getSpanCount(parent: RecyclerView): Int {
        // 列数
        var spanCount = -1
        val layoutManager: RecyclerView.LayoutManager? = parent.layoutManager
        if (layoutManager is GridLayoutManager) {
            spanCount = (layoutManager as GridLayoutManager).getSpanCount()
        } else if (layoutManager is StaggeredGridLayoutManager) {
            spanCount = (layoutManager as StaggeredGridLayoutManager)
                .getSpanCount()
        }
        return spanCount
    }

    fun drawHorizontal(c: Canvas?, parent: RecyclerView) {
        val childCount: Int = parent.getChildCount()
        for (i in 0 until childCount) {
            val child: View = parent.getChildAt(i)
            val params: RecyclerView.LayoutParams = child
                .layoutParams as RecyclerView.LayoutParams
            val left: Int = child.left - params.leftMargin
            val right: Int = (child.right + params.rightMargin
                    + mDivider!!.intrinsicWidth)
            val top: Int = child.bottom + params.bottomMargin
            val bottom = top + mDivider.intrinsicHeight
            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(c!!)
        }
    }

    fun drawVertical(c: Canvas?, parent: RecyclerView) {
        val childCount: Int = parent.getChildCount()
        for (i in 0 until childCount) {
            val child: View = parent.getChildAt(i)
            val params: RecyclerView.LayoutParams = child
                .layoutParams as RecyclerView.LayoutParams
            val top: Int = child.top - params.topMargin
            val bottom: Int = child.bottom + params.bottomMargin
            val left: Int = child.right + params.rightMargin
            val right = left + mDivider!!.intrinsicWidth
            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(c!!)
        }
    }

    private fun isLastColum(parent: RecyclerView, pos: Int, spanCount: Int, childCount: Int): Boolean {
        var childCount = childCount
        val layoutManager: RecyclerView.LayoutManager? = parent.layoutManager
        if (layoutManager is GridLayoutManager) {
            if ((pos + 1) % spanCount == 0) // 如果是最后一列，则不需要绘制右边
            {
                return true
            }
        } else if (layoutManager is StaggeredGridLayoutManager) {
            val orientation: Int = layoutManager.orientation
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                // 如果是最后一列，则不需要绘制右边
                if ((pos + 1) % spanCount == 0) {
                    return true
                }
            } else {
                childCount -= childCount % spanCount
                if (pos >= childCount) // 如果是最后一列，则不需要绘制右边
                    return true
            }
        }
        return false
    }

    private fun isLastRaw(
        parent: RecyclerView, pos: Int, spanCount: Int,
        childCount: Int
    ): Boolean {
        var childCount = childCount
        val layoutManager: RecyclerView.LayoutManager? = parent.layoutManager
        if (layoutManager is GridLayoutManager) {
            childCount -= childCount % spanCount
            if (pos >= childCount) // 如果是最后一行，则不需要绘制底部
                return true
        } else if (layoutManager is StaggeredGridLayoutManager) {
            val orientation: Int = layoutManager.orientation
            // StaggeredGridLayoutManager 且纵向滚动
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                childCount -= childCount % spanCount
                // 如果是最后一行，则不需要绘制底部
                if (pos >= childCount) return true
            } else  // StaggeredGridLayoutManager 且横向滚动
            {
                // 如果是最后一行，则不需要绘制底部
                if ((pos + 1) % spanCount == 0) {
                    return true
                }
            }
        }
        return false
    }

    override fun getItemOffsets(
        outRect: Rect, itemPosition: Int,
        parent: RecyclerView
    ) {
        val spanCount = getSpanCount(parent)
        val childCount: Int = parent?.adapter!!.itemCount
        if (isLastRaw(parent, itemPosition, spanCount, childCount)) // 如果是最后一行，则不需要绘制底部
        {
            outRect[0, 0, mDivider!!.intrinsicWidth] = 0
        } else if (isLastColum(parent, itemPosition, spanCount, childCount)) // 如果是最后一列，则不需要绘制右边
        {
            outRect[0, 0, 0] = mDivider!!.intrinsicHeight
        } else {
            outRect[0, 0, mDivider!!.intrinsicWidth] = mDivider.intrinsicHeight
        }
    }

    companion object {
        private val ATTRS = intArrayOf(R.attr.listDivider)
    }

    init {
        val a = context.obtainStyledAttributes(ATTRS)
        mDivider = a.getDrawable(0)
        a.recycle()
    }
}