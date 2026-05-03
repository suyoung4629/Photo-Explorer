package com.unsplash.photoexplorer.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.unsplash.photoexplorer.presentation.detail.PhotoDetailScreen
import com.unsplash.photoexplorer.presentation.favorites.FavoritesScreen
import com.unsplash.photoexplorer.presentation.list.PhotoListScreen

@Composable
fun PhotoExplorerNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Route.PhotoList,
    ) {
        composable<Route.PhotoList> {
            PhotoListScreen(
                onPhotoClick = { photoId ->
                    navController.navigate(Route.PhotoDetail(photoId))
                },
                onFavoritesClick = {
                    navController.navigate(Route.Favorites)
                },
            )
        }

        composable<Route.PhotoDetail> {
            PhotoDetailScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable<Route.Favorites> {
            FavoritesScreen(
                onPhotoClick = { photoId ->
                    navController.navigate(Route.PhotoDetail(photoId))
                },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
