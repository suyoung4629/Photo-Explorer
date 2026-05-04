package com.unsplash.photoexplorer.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.unsplash.photoexplorer.domain.model.Photo
import com.unsplash.photoexplorer.presentation.common.PhotoCard

@Composable
fun PhotoListScreen(
    onPhotoClick: (String) -> Unit,
    onFavoritesClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PhotoListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val photos = viewModel.photos.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.userMessages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val pagingError = (photos.loadState.refresh as? LoadState.Error
        ?: photos.loadState.append as? LoadState.Error)?.error

    LaunchedEffect(pagingError) {
        pagingError?.message?.let { snackbarHostState.showSnackbar(it) }
    }

    PhotoListContent(
        photos = photos,
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onPhotoClick = onPhotoClick,
        onFavoritesClick = onFavoritesClick,
        onToggleItemFavorite = viewModel::toggleFavorite,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoListContent(
    photos: LazyPagingItems<Photo>,
    uiState: PhotoListUiState,
    snackbarHostState: SnackbarHostState,
    onPhotoClick: (String) -> Unit,
    onFavoritesClick: () -> Unit,
    onToggleItemFavorite: (Photo) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Unsplash Explorer") },
                actions = {
                    IconButton(
                        onClick = onFavoritesClick,
                        modifier = Modifier.size(56.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorites",
                            modifier = Modifier.size(28.dp),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when {
            photos.loadState.refresh is LoadState.Loading -> {
                FullScreenLoading(modifier = Modifier.padding(innerPadding))
            }
            photos.loadState.refresh is LoadState.Error -> {
                FullScreenError(
                    onRetry = { photos.retry() },
                    modifier = Modifier.padding(innerPadding),
                )
            }
            photos.itemCount == 0 -> {
                FullScreenEmpty(modifier = Modifier.padding(innerPadding))
            }
            else -> {
                PhotoStaggeredGrid(
                    photos = photos,
                    togglingPhotoIds = uiState.togglingPhotoIds,
                    onPhotoClick = onPhotoClick,
                    onToggleFavorite = onToggleItemFavorite,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@Composable
private fun PhotoStaggeredGrid(
    photos: LazyPagingItems<Photo>,
    togglingPhotoIds: Set<String>,
    onPhotoClick: (String) -> Unit,
    onToggleFavorite: (Photo) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            count = photos.itemCount,
            key = photos.itemKey { it.id },
        ) { index ->
            val photo = photos[index] ?: return@items
            PhotoCard(
                photo = photo,
                isToggling = photo.id in togglingPhotoIds,
                onClick = { onPhotoClick(photo.id) },
                onToggleFavorite = { onToggleFavorite(photo) },
            )
        }

        when (photos.loadState.append) {
            is LoadState.Loading -> {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is LoadState.Error -> {
                item(span = StaggeredGridItemSpan.FullLine) {
                    AppendErrorRow(onRetry = { photos.retry() })
                }
            }
            else -> Unit
        }
    }
}

@Composable
private fun FullScreenLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun FullScreenError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("사진을 불러오지 못했습니다.")
        Spacer(Modifier.height(8.dp))
        Button(onClick = onRetry) { Text("다시 시도") }
    }
}

@Composable
private fun FullScreenEmpty(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text("사진이 없습니다.")
    }
}

@Composable
private fun AppendErrorRow(onRetry: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("페이지를 불러오지 못했습니다.")
        Spacer(Modifier.width(8.dp))
        TextButton(onClick = onRetry) { Text("재시도") }
    }
}
