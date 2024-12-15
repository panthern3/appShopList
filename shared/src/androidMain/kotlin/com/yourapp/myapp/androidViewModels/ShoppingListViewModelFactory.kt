package com.yourapp.myapp.androidViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yourapp.myapp.commonListRepository.ShoppingListRepository

class ShoppingListViewModelFactory(private val repository: ShoppingListRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShoppingListViewModelAndroid::class.java)) {
            return ShoppingListViewModelAndroid(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
