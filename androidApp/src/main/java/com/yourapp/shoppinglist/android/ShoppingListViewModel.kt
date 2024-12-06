package com.yourapp.shoppinglist.android

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.yourapp.shoppinglist.api.ItemModel
import com.yourapp.shoppinglist.api.ShopListModel
import com.yourapp.shoppinglist.api.ShoppingListRepository


class ShoppingListViewModel(private val repository: ShoppingListRepository) : ViewModel() {

    private val _allShoppingLists = mutableStateOf<List<ShopListModel>>(emptyList())
    val allShoppingLists: State<List<ShopListModel>> get() = _allShoppingLists
    private val _newListId = mutableStateOf<Int?>(0) // Начальное значение null
    val newListId: State<Int?> = _newListId
    val currentShoppingList = mutableStateOf<List<ShopListModel>>(emptyList())  // Список покупок
    val currentItems = mutableStateOf<List<ItemModel>>(emptyList())  // Для товаров текущего списка
    val testKey = mutableStateOf<String?>(null)  // Для хранения тестового ключа
    val errorMessage = mutableStateOf<String?>(null)  // Сообщения об ошибках    private var currentMaxId = mutableStateOf(0) // Хранение текущего максимального ID
    val operationStatus = mutableStateOf<String?>(null)
    private var currentMaxId = mutableStateOf<Int>(0) // Хранение текущего максимального ID


    fun setTestKey(key: String) {
        testKey.value = key
    }


    fun createTestKey() {
        viewModelScope.launch {
            val result = repository.createTestKey()
            result.onSuccess {
                testKey.value = it
                errorMessage.value = null
            }.onFailure {
                errorMessage.value = it.localizedMessage
            }
        }
    }

    fun authenticate(key: String) {
        viewModelScope.launch {
            val result = repository.authenticate(key)
            if (result) {
                errorMessage.value = null
            } else {
                errorMessage.value = "Authentication failed"
            }
        }
    }

    fun createShoppingList(name: String) {
        val key = testKey.value ?: "" // Если key равен null, устанавливаем пустую строку

        if (key.isNotEmpty()) {
            viewModelScope.launch {
                val result = repository.createShoppingList(key, name)

                result.onSuccess { newListId ->
                    // Логика обработки результата
                    val id = if (newListId == -1) {
                        getNextAvailableId() // Генерация ID если сервер вернул -1
                    } else {
                        newListId
                    }

                    // Сохраняем ID нового списка в состояние
                    _newListId.value = id // Обновляем состояние с новым ID, без приведения к строке

                    val newShoppingList = ShopListModel(id = id, name = name, key = key)
                    currentShoppingList.value = listOf(newShoppingList)

                    Log.d("ShoppingListViewModel", "Created new shopping list with ID: $id")
                }.onFailure {
                    errorMessage.value = it.localizedMessage
                    Log.e("ShoppingListViewModel", "Failed to create shopping list: ${it.localizedMessage}")
                }
            }
        } else {
            Log.e("ShoppingListViewModel", "Test key is empty")
        }
    }


    // Функция для получения следующего доступного ID
    private fun getNextAvailableId(): Int {
        currentMaxId.value += 1
        return currentMaxId.value
    }


    fun removeShoppingList(listId: Int) {
        viewModelScope.launch {
            val result = repository.removeShoppingList(listId)
            result.onSuccess { response ->
                if (response.success) {
                    val action = if (response.new_value) "deleted" else "restored"
                    operationStatus.value = "List $listId successfully $action"
                    Log.d("ShoppingListViewModel", "List $listId successfully $action.")
                } else {
                    operationStatus.value = "Failed to update list status"
                    Log.e("ShoppingListViewModel", "Error updating list $listId.")
                }
            }.onFailure {
                operationStatus.value = "Error: ${it.localizedMessage}"
                Log.e("ShoppingListViewModel", "Failed to remove or restore list: ${it.localizedMessage}")
            }
        }
    }

    // Метод для обновления ID
    fun updateListId(id: Int) {
        _newListId.value = id
    }


    // Метод для добавления товара в список
    fun addToShoppingList(listId: Int?, itemName: String, quantity: Int) {
        // Добавление элемента в список
        viewModelScope.launch {
            val result = repository.addToShoppingList(listId, itemName, quantity)
            result.onSuccess { _ ->
                // Получаем актуальный список после добавления элемента
                getShoppingList(listId)
            }.onFailure { exception ->
                errorMessage.value = "Error adding item: ${exception.localizedMessage}"
            }
        }
    }

    fun removeFromList(listId: Int?, itemId: Int) {
        viewModelScope.launch {
            val result = repository.removeFromList(listId, itemId)
            result.onSuccess {
                getShoppingList(listId)
            }.onFailure { exception ->
                errorMessage.value = "Error removing item: ${exception.localizedMessage}"
            }
        }
    }



    fun crossItOff(itemId: Int) {
        viewModelScope.launch {
            val result = repository.crossItemOff(itemId)

            result.onSuccess {
                val updatedList = currentItems.value.map { item ->
                    if (item.id == itemId) {
                        item.copy(isCrossedOff = !item.isCrossedOff) // Меняем состояние элемента
                    } else {
                        item
                    }
                }
                currentItems.value = updatedList
            }.onFailure { exception ->
                errorMessage.value = "Error crossing off item: ${exception.localizedMessage}" // Обработка ошибки
            }
        }
    }

    // Метод для получения списка покупок
    fun getAllMyShopLists(key: String) {
        viewModelScope.launch {
            val result = repository.getAllMyShopLists(key)
            result.onSuccess { shopLists ->
                _allShoppingLists.value = shopLists // Обновляем состояние с полученным списком
            }.onFailure {
                Log.e("ShoppingListViewModel", "Failed to load shopping lists: ${it.localizedMessage}")
            }
        }
    }


    fun getShoppingList(listId: Int?) {
        viewModelScope.launch {
            val result = repository.getShoppingList(listId)
            result.onSuccess { items ->
                currentItems.value = items // Обновляем элементы с учетом конкретного listId
            }.onFailure { exception ->
                errorMessage.value = "Error loading shopping list: ${exception.localizedMessage}"
            }
        }
    }


}
