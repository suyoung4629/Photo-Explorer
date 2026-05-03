package com.unsplash.photoexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.unsplash.photoexplorer.presentation.navigation.PhotoExplorerNavGraph
import com.unsplash.photoexplorer.presentation.theme.PhotoExplorerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhotoExplorerTheme {
                PhotoExplorerNavGraph()
            }
        }
    }
}
