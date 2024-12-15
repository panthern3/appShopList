package com.yourapp.myapp.commonListRepository

import com.yourapp.myapp.commonListApi.ShoppingListApi
import com.yourapp.myapp.commonModels.ItemModel
import com.yourapp.myapp.commonModels.RemoveShoppingListResponse
import com.yourapp.myapp.commonModels.ShopListModel


// ShoppingListRepository.kt
class ShoppingListRepository(private val api: ShoppingListApi) {

    suspend fun createTestKey(): Result<String> = api.createTestKey()

    suspend fun authenticate(key: String): Boolean = api.authenticate(key)

    suspend fun createShoppingList(key: String, name: String): Result<Int> = api.createShoppingList(key, name)

    suspend fun removeShoppingList(listId: Int): Result<RemoveShoppingListResponse> {
        if (listId <= 0) {
            return Result.failure(IllegalArgumentException("List ID must be greater than 0"))
        }
        return try {
            api.removeShoppingList(listId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addToShoppingList(listId: Int?, itemName: String, quantity: Int): Result<Int> = api.addToShoppingList(listId, itemName, quantity)

    suspend fun removeFromList(listId: Int?, itemId: Int): Result<Boolean> = api.removeFromList(listId, itemId)

    suspend fun crossItemOff(itemId: Int): Result<Boolean> {
        return api.crossItemOff(itemId)
    }

    suspend fun getAllMyShopLists(key: String): Result<List<ShopListModel>> {
        return api.getAllMyShopLists(key)
    }

    suspend fun getShoppingList(listId: Int?): Result<List<ItemModel>> = api.getShoppingList(listId)
}
