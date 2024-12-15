package com.yourapp.myapp.commonViewModelsInterface

import com.yourapp.myapp.commonModels.*
import kotlinx.coroutines.flow.StateFlow

// commonMain
interface ShoppingListViewModelInterface {

    val allShoppingLists: StateFlow<List<ShopListModel>>
    val newListId: StateFlow<Int?>
    val currentShoppingList: StateFlow<List<ShopListModel>>
    val currentItems: StateFlow<List<ItemModel>>
    val testKey: StateFlow<String?>
    val errorMessage: StateFlow<String?>
    val operationStatus: StateFlow<String?>

    fun setTestKey(key: String)
    fun createTestKey()
    fun authenticate(key: String)
    fun createShoppingList(name: String)
    fun removeShoppingList(listId: Int)
    fun updateListId(id: Int)
    fun addToShoppingList(listId: Int?, itemName: String, quantity: Int)
    fun removeFromList(listId: Int?, itemId: Int)
    fun crossItOff(itemId: Int)
    fun getAllMyShopLists(key: String)
    fun getShoppingList(listId: Int?)
}
