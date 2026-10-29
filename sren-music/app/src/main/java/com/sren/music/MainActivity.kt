package com.sren.music

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sren.music.presentation.main.MainViewModel
import com.sren.music.presentation.main.SrenApp
import com.sren.music.presentation.theme.SrenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SrenTheme {
                val appContainer = (application as SrenApplication).appContainer
                val mainViewModel: MainViewModel = viewModel(
                    factory = MainViewModel.factory(appContainer),
                )
                SrenApp(viewModel = mainViewModel)
            }
        }
    }
}
