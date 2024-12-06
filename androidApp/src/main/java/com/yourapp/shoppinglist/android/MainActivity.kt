package com.yourapp.shoppinglist.android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import com.yourapp.shoppinglist.api.ShopListModel
import com.yourapp.shoppinglist.api.ShoppingListApi
import com.yourapp.shoppinglist.api.ShoppingListRepository


class MainActivity : ComponentActivity() {

    private val viewModel: ShoppingListViewModel by viewModels {
        ShoppingListViewModelFactory(
            ShoppingListRepository(
                ShoppingListApi(HttpClient(Android)) // Инициализация клиента
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShoppingListTheme {
                MainScreen(viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: ShoppingListViewModel) {
    val newListId = viewModel.newListId.value // Получаем ID нового списка
    var itemNameToAdd by remember { mutableStateOf("") }
    var quantityToAdd by remember { mutableStateOf("") }
    var newListIdString by remember { mutableStateOf("") } // Сохраняем ID как строку для TextField
    var listName by remember { mutableStateOf("") } // Состояние для имени списка
    val currentItems = viewModel.currentItems.value
    val testKey = viewModel.testKey.value
    val errorMessage = viewModel.errorMessage.value
    val allShoppingLists = viewModel.allShoppingLists.value
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Shopping List App", style = MaterialTheme.typography.bodyMedium)

        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        // 1. Create Test Key Button
        Button(onClick = { viewModel.createTestKey() }) {
            Text("Create Test Key")
        }

        testKey?.let {
            Text(text = "Test Key: $it")
        }

        // 2. Authentication Button
        TextField(
            value = testKey.orEmpty(),
            onValueChange = { viewModel.setTestKey(it) },
            label = { Text("Enter Test Key") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = {
            val key = testKey ?: ""
            if (key.isNotEmpty()) {
                Log.d("MainActivity", "Authenticating with key: $key")
                viewModel.authenticate(key)
            } else {
                Log.e("MainActivity", "Test key is not available for authentication.")
            }
        }) {
            Text("Authenticate")
        }

        // 3. Create Shopping List Button
        TextField(
            value = listName,
            onValueChange = { listName = it },
            label = { Text("Enter Shopping List Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = {
                if (listName.isNotEmpty()) {
                    viewModel.createShoppingList(listName)
                } else {
                    Log.e("CreateShoppingListView", "List name is empty")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Shopping List")
        }

        newListId?.let {
            Text(text = "Shopping List ID: $it")
        }

        // 4. Remove Shopping List Button
        var listIdToRemove by remember { mutableStateOf("") }
        TextField(
            value = listIdToRemove,
            onValueChange = { listIdToRemove = it },
            label = { Text("Shopping List ID to Remove or Restore") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        val operationStatus by viewModel.operationStatus
        operationStatus?.let {
            Text(
                text = it,
                color = if (it.contains("successfully")) Color.Green else Color.Red,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Button(onClick = {
            val listId = listIdToRemove.toIntOrNull()
            if (listId != null) {
                Log.d("MainActivity", "Processing shopping list with ID: $listId")
                viewModel.removeShoppingList(listId)
            } else {
                Log.e("MainActivity", "Invalid list ID.")
            }
        }) {
            Text("Remove/Restore Shopping List")
        }

        // 5. Add To Shopping List Button
        TextField(
            value = newListIdString,
            onValueChange = { value ->
                if (value.all { char -> char.isDigit() }) { // Проверка на ввод только цифр
                    newListIdString = value // Обновляем строку на введённый ID
                    // Конвертируем строку в Int и обновляем в ViewModel
                    val intId = value.toIntOrNull()
                    if (intId != null) {
                        viewModel.updateListId(intId) // Передаем Int ID в ViewModel
                    }
                }
            },
            label = { Text("List ID") },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = itemNameToAdd,
            onValueChange = { itemNameToAdd = it },
            label = { Text("Item Name") },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = quantityToAdd,
            onValueChange = {
                if (it.all { char -> char.isDigit() }) { // Проверка на ввод только цифр
                    quantityToAdd = it
                }
            },
            label = { Text("Quantity") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = {
            val parsedQuantity: Int? =
                quantityToAdd.toIntOrNull() // Парсим quantity как nullable Int

            if (parsedQuantity != null && itemNameToAdd.isNotEmpty() && (newListId ?: 0) > 0) {
                Log.d(
                    "MainActivity",
                    "Adding item: $itemNameToAdd, quantity: $parsedQuantity to list: $newListId"
                )
                viewModel.addToShoppingList(newListId, itemNameToAdd, parsedQuantity)
            } else {
                Log.e("MainActivity", "Invalid input for adding item.")
            }
        }) {
            Text("Add To Shopping List")
        }


        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
        ) {
            // Отображаем элементы текущего списка покупок
            if (currentItems.isNotEmpty()) {
                itemsIndexed(currentItems) { _, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${item.name} - ${item.created}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textDecoration = if (item.isCrossedOff) TextDecoration.LineThrough else TextDecoration.None
                            )
                        )
                        Row {
                            Button(onClick = {
                                viewModel.crossItOff(item.id)
                            }) {
                                Text(text = if (item.isCrossedOff) "Uncross Off" else "Cross Off")
                            }
                            Button(onClick = {
                                val currentListId = newListId
                                viewModel.removeFromList(currentListId, item.id)
                            }) {
                                Text("Remove Item")
                            }
                        }
                    }
                }
            }

            // Показ списка покупок, если он доступен
            if (allShoppingLists.isEmpty()) {
                item {
                    Text("No shopping lists available", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                itemsIndexed(allShoppingLists) { _, list ->
                    ShoppingListItem(shopList = list, onGetItems = {
                        // Вызываем метод getShoppingList при нажатии на кнопку для загрузки элементов списка
                        viewModel.getShoppingList(list.id)
                    })
                }
            }

            // 7. Get All Shopping Lists Button
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp), // Добавим отступы для эстетики
                    contentAlignment = Alignment.Center // Центрируем содержимое
                ) {
                    Button(onClick = {
                        val key = viewModel.testKey.value ?: ""
                        if (key.isNotEmpty()) {
                            viewModel.getAllMyShopLists(key)
                        } else {
                            Log.e("MainActivity", "Key is empty.")
                        }
                    }) {
                        Text("Load Shopping Lists")
                    }
                }
            }
        }



        // 8. Get Shopping List Button
        var listIdToLoad by remember { mutableStateOf("") }
        TextField(
            value = listIdToLoad,
            onValueChange = { listIdToLoad = it },
            label = { Text("Shopping List ID to Load") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = {
            val listId = listIdToLoad.toIntOrNull()
            if (listId != null) {
                Log.d("MainActivity", "Loading shopping list with id: $listId")
                viewModel.getShoppingList(listId)
            } else {
                Log.e("MainActivity", "Invalid list ID.")
            }
        }) {
            Text("Get Shopping List")
        }
    }
}


@Composable
fun ShoppingListItem(shopList: ShopListModel, onGetItems: () -> Unit) {
    Column(modifier = Modifier.padding(8.dp)) {
        // Отображение информации о списке покупок
        Text(text = "List ID: ${shopList.id}", style = MaterialTheme.typography.bodyMedium)
        Text(text = "Name: ${shopList.name}", style = MaterialTheme.typography.bodyMedium)

        // Кнопка для получения элементов списка
        Button(onClick = { onGetItems() }) {
            Text("Get Items")
        }
    }
}


