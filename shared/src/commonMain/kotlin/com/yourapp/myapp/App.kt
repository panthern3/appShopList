package com.yourapp.myapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.yourapp.myapp.commonModels.ShopListModel
import com.yourapp.myapp.commonViewModelsInterface.ShoppingListViewModelInterface
import mu.KotlinLogging

@Composable
fun App(viewModel: ShoppingListViewModelInterface) {
    val logger = KotlinLogging.logger {}
    val newListId by viewModel.newListId.collectAsState()  // Наблюдаем за состоянием newListId
    var itemNameToAdd by remember { mutableStateOf("") }
    var quantityToAdd by remember { mutableStateOf("") }
    var newListIdString by remember { mutableStateOf("") } // Сохраняем ID как строку для TextField
    var listName by remember { mutableStateOf("") } // Состояние для имени списка
    val currentItems by viewModel.currentItems.collectAsState()
    val testKey by viewModel.testKey.collectAsState() // Наблюдение за состоянием
    val errorMessage = viewModel.errorMessage.value
    val allShoppingLists by viewModel.allShoppingLists.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Shopping List App", style = MaterialTheme.typography.body2)

        // Логирование ошибки, если она есть
        errorMessage?.let {
            logger.error { "Error: $it" }
            Text(text = it, color = MaterialTheme.colors.error)
        }

        // 1. Create Test Key Button
        Button(onClick = {
            logger.info { "Creating new test key" }
            viewModel.createTestKey()
        }) {
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
                viewModel.authenticate(key)
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
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Shopping List")
        }

        newListId?.let {
            Text(text = "Shopping List ID: $it")
        } ?: run {
            Text(text = "Shopping List ID: not available")
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

        val operationStatus by viewModel.operationStatus.collectAsState()  // Наблюдаем за состоянием
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
                viewModel.removeShoppingList(listId)
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
                viewModel.addToShoppingList(newListId, itemNameToAdd, parsedQuantity)
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
                            style = MaterialTheme.typography.body2.copy(
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
                    Text("No shopping lists available", style = MaterialTheme.typography.body2)
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
                viewModel.getShoppingList(listId)
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
        Text(text = "List ID: ${shopList.id}", style = MaterialTheme.typography.body2)
        Text(text = "Name: ${shopList.name}", style = MaterialTheme.typography.body2)

        // Кнопка для получения элементов списка
        Button(onClick = { onGetItems() }) {
            Text("Get Items")
        }
    }
}
