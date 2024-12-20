package com.yourapp.myapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import com.yourapp.myapp.App
import com.yourapp.myapp.commonListApi.ShoppingListApi
import com.yourapp.myapp.commonListRepository.ShoppingListRepository
import com.yourapp.myapp.commonViewModelsInterface.ShoppingListViewModelInterface
import com.yourapp.myapp.iosViewModels.ShoppingListViewModelIOS
import io.ktor.client.HttpClient
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    SafeArea {
        val repository = ShoppingListRepository(ShoppingListApi(HttpClient()))
        val viewModel = ShoppingListViewModelIOS(repository)
        App(viewModel = viewModel)
    }
}

@Composable
fun SafeArea(
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.height(50.dp))
        content.invoke()
    }
}
