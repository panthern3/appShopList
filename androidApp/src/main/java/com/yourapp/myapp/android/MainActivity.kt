package com.yourapp.myapp.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.yourapp.myapp.App
import com.yourapp.myapp.androidViewModels.ShoppingListViewModelAndroid
import com.yourapp.myapp.androidViewModels.ShoppingListViewModelFactory
import com.yourapp.myapp.commonListApi.ShoppingListApi
import com.yourapp.myapp.commonListRepository.ShoppingListRepository
import io.ktor.client.*
import io.ktor.client.engine.android.*

class MainActivity : ComponentActivity() {

    private val viewModel: ShoppingListViewModelAndroid by viewModels {
        ShoppingListViewModelFactory(
            ShoppingListRepository(
                ShoppingListApi(HttpClient(Android)) // Инициализация клиента
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                App(viewModel = viewModel)
            }
        }
    }
}
