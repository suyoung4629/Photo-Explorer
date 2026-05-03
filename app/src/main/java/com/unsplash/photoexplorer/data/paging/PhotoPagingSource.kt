package com.unsplash.photoexplorer.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.unsplash.photoexplorer.data.remote.api.UnsplashApi
import com.unsplash.photoexplorer.data.remote.dto.PhotoDto
import retrofit2.HttpException
import java.io.IOException

class PhotoPagingSource(
    private val api: UnsplashApi,
) : PagingSource<Int, PhotoDto>() {

    override fun getRefreshKey(state: PagingState<Int, PhotoDto>): Int? {
        val anchor = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchor) ?: return null
        return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PhotoDto> {
        val page = params.key ?: STARTING_PAGE
        return try {
            val photos = api.getPhotos(
                page = page,
                perPage = params.loadSize.coerceAtMost(MAX_PER_PAGE),
                orderBy = ORDER_BY,
            )
            LoadResult.Page(
                data = photos,
                prevKey = if (page == STARTING_PAGE) null else page - 1,
                nextKey = if (photos.isEmpty()) null else page + 1,
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }

    private companion object {
        const val STARTING_PAGE = 1
        const val MAX_PER_PAGE = 30
        const val ORDER_BY = "latest"
    }
}
