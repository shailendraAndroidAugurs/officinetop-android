package com.officinetop.officine.adapter

import androidx.annotation.NonNull
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class PaginationListener(@NonNull val lManager: LinearLayoutManager, pageSize: Int = 10) : RecyclerView.OnScrollListener() {

    private val PAGE_SIZE = pageSize

    override fun onScrolled(@NonNull recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val visibleItemCount = lManager.childCount
        val totalItemCount = lManager.itemCount
        val firstVisibleItemPosition = lManager.findFirstVisibleItemPosition()

        if (!isLoading() && !isLastPage()) {
            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= PAGE_SIZE) {
                loadMoreItems()
            }
        }
    }

    protected abstract fun loadMoreItems()
    abstract fun isLastPage(): Boolean
    abstract fun isLoading(): Boolean


}