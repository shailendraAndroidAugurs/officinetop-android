package com.officinetop.officine.adapter

import androidx.annotation.NonNull
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class PaginationListener(@NonNull val lManager: LinearLayoutManager, var pageSize: Int = 10, var totalItemCount: Int = 0) : RecyclerView.OnScrollListener() {

    private val PAGE_SIZE = pageSize
    private var stopLoading = false

    override fun onScrolled(@NonNull recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val visibleItemCount = lManager.childCount
        if (totalItemCount == 0) {
            totalItemCount = lManager.itemCount
            val firstVisibleItemPosition = lManager.findFirstVisibleItemPosition()

            if (!isLoading() && !isLastPage()) {
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= PAGE_SIZE) {
                    loadMoreItems()
                }
            }
        } else {
            if (totalItemCount <= 30 && !stopLoading)
                loadMoreItems()
        }


    }

    protected abstract fun loadMoreItems()
    abstract fun isLastPage(): Boolean
    abstract fun isLoading(): Boolean
    fun stopLoading(isStopLoading: Boolean = false) {
        stopLoading = isStopLoading
    }

}