package com.yourapp.myapp.androidViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import com.yourapp.myapp.commonListRepository.ShoppingListRepository
import com.yourapp.myapp.commonModels.*
import com.yourapp.myapp.commonViewModelsInterface.ShoppingListViewModelInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class ShoppingListViewModelAndroid(private val repository: ShoppingListRepository) : ViewModel(),
    ShoppingListViewModelInterface {

    private val _allShoppingLists = MutableStateFlow<List<ShopListModel>>(emptyList())
    override val allShoppingLists: StateFlow<List<ShopListModel>> get() = _allShoppingLists
    private val _newListId = MutableStateFlow<Int?>(null)
    override val newListId: StateFlow<Int?> get() = _newListId
    override val currentShoppingList = MutableStateFlow<List<ShopListModel>>(emptyList())
    override val currentItems = MutableStateFlow<List<ItemModel>>(emptyList())
    override val testKey = MutableStateFlow<String?>(null)
    override val errorMessage = MutableStateFlow<String?>(null)
    override val operationStatus = MutableStateFlow<String?>(null)
    private var currentMaxId = mutableStateOf<Int>(0) // Хранение текущего максимального ID


    override fun setTestKey(key: String) {
        testKey.value = key
    }


    override fun createTestKey() {
        Log.d("ShoppingListViewModel", "createTestKey called")

        viewModelScope.launch {
            try {
                val result = repository.createTestKey()
                result.onSuccess {
                    testKey.value = it
                    errorMessage.value = null
                    Log.d("ShoppingListViewModel", "Test key created successfully: $it")
                }.onFailure {
                    errorMessage.value = it.localizedMessage
                    Log.e("ShoppingListViewModel", "Failed to create test key: ${it.localizedMessage}")
                }
            } catch (exception: Exception) {
                errorMessage.value = exception.localizedMessage
                Log.e("ShoppingListViewModel", "Exception in createTestKey: ${exception.localizedMessage}")
            }
        }
    }


    override fun authenticate(key: String) {
        Log.d("ShoppingListViewModel", "Authentication started with key: $key")  // Логирование начала аутентификации

        viewModelScope.launch {
            val result = repository.authenticate(key)

            if (result) {
                errorMessage.value = null
                Log.d("ShoppingListViewModel", "Authentication successful")  // Логирование успешной аутентификации
            } else {
                errorMessage.value = "Authentication failed"
                Log.e("ShoppingListViewModel", "Authentication failed")  // Логирование ошибки аутентификации
            }
        }
    }

    // Метод для создания списков покупок
    override fun createShoppingList(name: String) {
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


    // Метод для удаления списка покупок
    override fun removeShoppingList(listId: Int) {
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
    override fun updateListId(id: Int) {
        _newListId.value = id
    }


    // Метод для добавления товара в список
    override fun addToShoppingList(listId: Int?, itemName: String, quantity: Int) {
        Log.i("ShoppingListViewModel", "Attempting to add item '$itemName' with quantity $quantity to shopping list ID: $listId")

        viewModelScope.launch {
            try {
                val result = repository.addToShoppingList(listId, itemName, quantity)

                result.onSuccess { _ ->
                    Log.i("ShoppingListViewModel", "Successfully added item '$itemName' to shopping list ID: $listId")
                    getShoppingList(listId)
                }.onFailure { exception ->
                    Log.e("ShoppingListViewModel", "Error adding item '$itemName' to shopping list ID: $listId", exception)
                    errorMessage.value = "Error adding item: ${exception.localizedMessage}"
                }
            } catch (e: Exception) {
                Log.e("ShoppingListViewModel", "Unexpected error adding item '$itemName' to shopping list ID: $listId", e)
                errorMessage.value = "Unexpected error: ${e.localizedMessage}"
            }
        }
    }


    // Метод для удаления товара из списка
    override fun removeFromList(listId: Int?, itemId: Int) {
        Log.i("ShoppingListViewModel", "Attempting to remove item with ID: $itemId from list with ID: $listId")

        viewModelScope.launch {
            try {
                val result = repository.removeFromList(listId, itemId)

                result.onSuccess {
                    Log.i("ShoppingListViewModel", "Successfully removed item with ID: $itemId from list with ID: $listId")

                    getShoppingList(listId)
                }.onFailure { exception ->
                    Log.e("ShoppingListViewModel", "Error removing item with ID: $itemId from list with ID: $listId", exception)
                    errorMessage.value = "Error removing item: ${exception.localizedMessage}"
                }
            } catch (e: Exception) {
                Log.e("ShoppingListViewModel", "Unexpected error removing item with ID: $itemId from list with ID: $listId", e)
            }
        }
    }



    // Метод для зачеркивания товара из списка
    override fun crossItOff(itemId: Int) {
        Log.i("ShoppingListViewModel", "Attempting to cross off item with ID: $itemId")

        viewModelScope.launch {
            try {
                val result = repository.crossItemOff(itemId)

                result.onSuccess {
                    Log.i("ShoppingListViewModel", "Successfully crossed off item with ID: $itemId")

                    val updatedList = currentItems.value.map { item ->
                        if (item.id == itemId) {
                            item.copy(isCrossedOff = !item.isCrossedOff) // Меняем состояние элемента
                        } else {
                            item
                        }
                    }
                    currentItems.value = updatedList
                }.onFailure { exception ->
                    Log.e("ShoppingListViewModel", "Error crossing off item with ID: $itemId", exception)
                    errorMessage.value = "Error crossing off item: ${exception.localizedMessage}" // Обработка ошибки
                }
            } catch (e: Exception) {
                Log.e("ShoppingListViewModel", "Unexpected error crossing off item with ID: $itemId", e)
            }
        }
    }


    // Метод для получения списков покупок
    override fun getAllMyShopLists(key: String) {
        Log.i("ShoppingListViewModel", "Attempting to load all shopping lists for key: $key")

        viewModelScope.launch {
            try {
                val result = repository.getAllMyShopLists(key)

                result.onSuccess { shopLists ->
                    Log.i("ShoppingListViewModel", "Successfully loaded shopping lists. Count: ${shopLists.size}")
                    _allShoppingLists.value = shopLists // Обновляем состояние с полученным списком
                }.onFailure { exception ->
                    Log.e("ShoppingListViewModel", "Error loading shopping lists for key: $key", exception)
                }
            } catch (e: Exception) {
                Log.e("ShoppingListViewModel", "Unexpected error loading shopping lists for key: $key", e)
            }
        }
    }


    // Метод для получения списка покупок
    override fun getShoppingList(listId: Int?) {
        // Логируем начало выполнения метода
        Log.i("ShoppingListViewModel", "Attempting to load shopping list with ID: $listId")

        viewModelScope.launch {
            try {
                val result = repository.getShoppingList(listId)

                result.onSuccess { items ->
                    // Логируем успешную загрузку
                    Log.i("ShoppingListViewModel", "Successfully loaded shopping list with ID: $listId, Items count: ${items.size}")
                    currentItems.value = items // Обновляем элементы с учетом конкретного listId
                }.onFailure { exception ->
                    // Логируем ошибку при загрузке списка
                    Log.e("ShoppingListViewModel", "Error loading shopping list with ID: $listId", exception)
                    errorMessage.value = "Error loading shopping list: ${exception.localizedMessage}"
                }
            } catch (e: Exception) {
                // Логируем исключение при неудаче
                Log.e("ShoppingListViewModel", "Unexpected error loading shopping list with ID: $listId", e)
                errorMessage.value = "Unexpected error: ${e.localizedMessage}"
            }
        }
    }

}
