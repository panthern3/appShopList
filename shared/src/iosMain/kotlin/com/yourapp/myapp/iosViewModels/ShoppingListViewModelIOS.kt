package com.yourapp.myapp.iosViewModels

import com.yourapp.myapp.commonListRepository.ShoppingListRepository
import com.yourapp.myapp.commonModels.*
import com.yourapp.myapp.commonViewModelsInterface.ShoppingListViewModelInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import platform.Foundation.NSLog
import com.yourapp.myapp.iosLogs.LoggerIOS  // Импортируем LoggerIOS



class ShoppingListViewModelIOS(private val repository: ShoppingListRepository) : ShoppingListViewModelInterface {
    val scope = MainScope() // Для iOS

    private val _allShoppingLists = MutableStateFlow<List<ShopListModel>>(emptyList())
    override val allShoppingLists: StateFlow<List<ShopListModel>> get() = _allShoppingLists
    private val _newListId = MutableStateFlow<Int?>(null)
    override val newListId: StateFlow<Int?> get() = _newListId
    override val currentShoppingList = MutableStateFlow<List<ShopListModel>>(emptyList())
    override val currentItems = MutableStateFlow<List<ItemModel>>(emptyList())
    override val testKey = MutableStateFlow<String?>(null)
    override val errorMessage = MutableStateFlow<String?>(null)
    override val operationStatus = MutableStateFlow<String?>(null)
    private var currentMaxId = MutableStateFlow<Int>(0) // Track current max ID

    private val logger = LoggerIOS()  // Создаем экземпляр LoggerIOS

    // Используем методы logger для логирования
    private fun log(message: String) {
        logger.logInfo(message)  // Логирование информации
    }

    private fun logError(message: String) {
        logger.logError(message)  // Логирование ошибок
    }


    override fun setTestKey(key: String) {
        testKey.value = key
    }

    override fun createTestKey() {
        log("createTestKey called")

        scope.launch(Dispatchers.Main) {
            try {
                val result = repository.createTestKey()
                result.onSuccess {
                    testKey.value = it
                    errorMessage.value = null
                    log("Test key created successfully: $it")
                }.onFailure {
                    errorMessage.value = it.message
                    logError("Failed to create test key: ${it.message}")
                }
            } catch (exception: Exception) {
                errorMessage.value = exception.message
                logError("Exception in createTestKey: ${exception.message}")
            }
        }
    }

    override fun authenticate(key: String) {
        log("Authentication started with key: $key")

        scope.launch(Dispatchers.Main) {
            val result = repository.authenticate(key)

            if (result) {
                errorMessage.value = null
                log("Authentication successful")
            } else {
                errorMessage.value = "Authentication failed"
                logError("Authentication failed")
            }
        }
    }

    override fun createShoppingList(name: String) {
        val key = testKey.value ?: ""

        if (key.isNotEmpty()) {
            scope.launch(Dispatchers.Main) {
                val result = repository.createShoppingList(key, name)

                result.onSuccess { newListId ->
                    val id = if (newListId == -1) {
                        getNextAvailableId()
                    } else {
                        newListId
                    }

                    _newListId.value = id
                    val newShoppingList = ShopListModel(id = id, name = name, key = key)
                    currentShoppingList.value = listOf(newShoppingList)

                    log("Created new shopping list with ID: $id")
                }.onFailure {
                    errorMessage.value = it.message
                    logError("Failed to create shopping list: ${it.message}")
                }
            }
        } else {
            logError("Test key is empty")
        }
    }

    private fun getNextAvailableId(): Int {
        currentMaxId.value += 1
        return currentMaxId.value
    }

    override fun removeShoppingList(listId: Int) {
        scope.launch(Dispatchers.Main) {
            val result = repository.removeShoppingList(listId)
            result.onSuccess { response ->
                if (response.success) {
                    val action = if (response.new_value) "deleted" else "restored"
                    operationStatus.value = "List $listId successfully $action"
                    log("List $listId successfully $action.")
                } else {
                    operationStatus.value = "Failed to update list status"
                    logError("Error updating list $listId.")
                }
            }.onFailure {
                operationStatus.value = "Error: ${it.message}"
                logError("Failed to remove or restore list: ${it.message}")
            }
        }
    }

    override fun updateListId(id: Int) {
        _newListId.value = id
    }

    override fun addToShoppingList(listId: Int?, itemName: String, quantity: Int) {
        log("Attempting to add item '$itemName' with quantity $quantity to shopping list ID: $listId")

        scope.launch(Dispatchers.Main) {
            try {
                val result = repository.addToShoppingList(listId, itemName, quantity)

                result.onSuccess {
                    log("Successfully added item '$itemName' to shopping list ID: $listId")
                    getShoppingList(listId)
                }.onFailure { exception ->
                    logError("Error adding item '$itemName' to shopping list ID: $listId, ${exception.message}")
                    errorMessage.value = "Error adding item: ${exception.message}"
                }
            } catch (e: Exception) {
                logError("Unexpected error adding item '$itemName' to shopping list ID: $listId, ${e.message}")
                errorMessage.value = "Unexpected error: ${e.message}"
            }
        }
    }

    override fun removeFromList(listId: Int?, itemId: Int) {
        log("Attempting to remove item with ID: $itemId from list with ID: $listId")

        scope.launch(Dispatchers.Main) {
            try {
                val result = repository.removeFromList(listId, itemId)

                result.onSuccess {
                    log("Successfully removed item with ID: $itemId from list with ID: $listId")
                    getShoppingList(listId)
                }.onFailure { exception ->
                    logError("Error removing item with ID: $itemId from list with ID: $listId, ${exception.message}")
                    errorMessage.value = "Error removing item: ${exception.message}"
                }
            } catch (e: Exception) {
                logError("Unexpected error removing item with ID: $itemId from list with ID: $listId, ${e.message}")
            }
        }
    }

    override fun crossItOff(itemId: Int) {
        log("Attempting to cross off item with ID: $itemId")

        scope.launch(Dispatchers.Main) {
            try {
                val result = repository.crossItemOff(itemId)

                result.onSuccess {
                    log("Successfully crossed off item with ID: $itemId")

                    val updatedList = currentItems.value.map { item ->
                        if (item.id == itemId) {
                            item.copy(isCrossedOff = !item.isCrossedOff)
                        } else {
                            item
                        }
                    }
                    currentItems.value = updatedList
                }.onFailure { exception ->
                    logError("Error crossing off item with ID: $itemId, ${exception.message}")
                    errorMessage.value = "Error crossing off item: ${exception.message}"
                }
            } catch (e: Exception) {
                logError("Unexpected error crossing off item with ID: $itemId, ${e.message}")
            }
        }
    }

    override fun getAllMyShopLists(key: String) {
        log("Attempting to load all shopping lists for key: $key")

        scope.launch(Dispatchers.Main) {
            try {
                val result = repository.getAllMyShopLists(key)

                result.onSuccess { shopLists ->
                    log("Successfully loaded shopping lists. Count: ${shopLists.size}")
                    _allShoppingLists.value = shopLists
                }.onFailure { exception ->
                    logError("Error loading shopping lists for key: $key, ${exception.message}")
                }
            } catch (e: Exception) {
                logError("Unexpected error loading shopping lists for key: $key, ${e.message}")
            }
        }
    }

    override fun getShoppingList(listId: Int?) {
        log("Attempting to load shopping list with ID: $listId")

        scope.launch(Dispatchers.Main) {
            try {
                val result = repository.getShoppingList(listId)

                result.onSuccess { items ->
                    log("Successfully loaded shopping list with ID: $listId, containing ${items.size} items.")
                    currentItems.value = items
                }.onFailure { exception ->
                    logError("Error loading shopping list with ID: $listId, ${exception.message}")
                }
            } catch (e: Exception) {
                logError("Unexpected error loading shopping list with ID: $listId, ${e.message}")
            }
        }
    }
}
