package com.officinetop.adapter

import androidx.annotation.NonNull
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class PaginationListener(@NonNull val lManager: LinearLayoutManager, pageSize: Int = 10, var totalItemCount: Int = 0) : RecyclerView.OnScrollListener() {

    private val PAGE_SIZE = pageSize
    private var stopLoading = false
    private var forWorkshopList = false
    override fun onScrolled(@NonNull recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val visibleItemCount = lManager.childCount
        if (totalItemCount != 1) {
            totalItemCount = lManager.itemCount
            val firstVisibleItemPosition = lManager.findFirstVisibleItemPosition()

            if (!isLoading() && !isLastPage() && !stopLoading) {
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= PAGE_SIZE) {
                    loadMoreItems()
                }
            }
        } else {
            if ((totalItemCount == 1|| forWorkshopList) && !stopLoading) {
                totalItemCount = lManager.itemCount
                forWorkshopList = true
                loadMoreItems()
            }

        }


    }

    protected abstract fun loadMoreItems()
    abstract fun isLastPage(): Boolean
    abstract fun isLoading(): Boolean
    fun stopLoading(isStopLoading: Boolean = false) {
        stopLoading = isStopLoading
    }

}