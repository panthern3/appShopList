package com.yourapp.myapp.commonListApi

import com.yourapp.myapp.commonModels.ItemModel
import com.yourapp.myapp.commonModels.RemoveShoppingListResponse
import com.yourapp.myapp.commonModels.ResponseModel
import com.yourapp.myapp.commonModels.ShopListModel
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.coroutines.IO


class ShoppingListApi(private val client: HttpClient) {

    private val baseUrl = "https://cyberprot.ru/shopping/v2/"
    private val json = Json { ignoreUnknownKeys = true } // Инициализация Json

    // Создать тестовый ключ
    suspend fun createTestKey(): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response: HttpResponse = client.get("${baseUrl}CreateTestKey?")
                val responseBody = response.bodyAsText()
                Result.success(responseBody)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Аутентификация по ключу
    suspend fun authenticate(key: String): Boolean {
        val response: HttpResponse = client.get("${baseUrl}Authentication?key=$key")
        return response.status.value == 200
    }

    // Создать список покупок
    suspend fun createShoppingList(key: String, name: String): Result<Int> {
        val response: HttpResponse = client.post("${baseUrl}CreateShoppingList?key=$key&name=$name")
        val responseBody = response.bodyAsText()

        return try {
            val data = json.decodeFromString<ResponseModel>(responseBody)
            if (data.success) {
                Result.success(data.listId ?: -1) // Если listId null, возвращаем -1
            } else {
                Result.failure(Exception("Error creating shopping list"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeShoppingList(listId: Int): Result<RemoveShoppingListResponse> {
        val response: HttpResponse = client.post("${baseUrl}RemoveShoppingList?list_id=$listId")
        val responseBody = response.bodyAsText()
        return try {
            val data = json.decodeFromString<RemoveShoppingListResponse>(responseBody)
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // Добавить товар в список покупок
    suspend fun addToShoppingList(listId: Int?, itemName: String, quantity: Int): Result<Int> {
        val response: HttpResponse = client.post("${baseUrl}AddToShoppingList?id=$listId&value=$itemName&n=$quantity")
        val responseBody = response.bodyAsText()
        return try {
            val data = json.decodeFromString<ResponseModel>(responseBody)
            if (data.success) {
                Result.success(data.itemList?.firstOrNull()?.id ?: -1)
            } else {
                Result.failure(Exception("Error adding item"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Удалить товар из списка
    suspend fun removeFromList(listId: Int?, itemId: Int): Result<Boolean> {
        val response: HttpResponse = client.post("${baseUrl}RemoveFromList?list_id=$listId&item_id=$itemId")
        val responseBody = response.bodyAsText()
        return try {
            val data = json.decodeFromString<ResponseModel>(responseBody)
            Result.success(data.success)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Вычеркнуть товар из списка
    suspend fun crossItemOff(itemId: Int): Result<Boolean> {
        return try {
            val response: HttpResponse = client.post("${baseUrl}CrossItOff?id=$itemId")
            val responseBody = response.bodyAsText()

            // Предполагаем, что API возвращает модель ResponseModel с флагом успеха
            val data = json.decodeFromString<ResponseModel>(responseBody)
            if (data.success) {
                Result.success(true) // Возвращаем успех, если ответ успешен
            } else {
                Result.failure(Exception("Failed to cross off item"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllMyShopLists(key: String): Result<List<ShopListModel>> {
        return try {
            val response: HttpResponse = client.get("${baseUrl}GetAllMyShopLists?key=$key")
            val responseBody = response.bodyAsText()

            val data = json.decodeFromString<ResponseModel>(responseBody)

            if (data.success) {

                // Если запрос успешен, возвращаем список всех shopList
                Result.success(data.shopList ?: emptyList())
            } else {
                Result.failure(Exception("Error loading shopping lists"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }






    // Загрузить конкретный список
    suspend fun getShoppingList(listId: Int?): Result<List<ItemModel>> {
        val response: HttpResponse = client.get("${baseUrl}GetShoppingList?list_id=$listId")
        val responseBody = response.bodyAsText()
        return try {
            val data = json.decodeFromString<ResponseModel>(responseBody)
            Result.success(data.itemList ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
