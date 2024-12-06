import SwiftUI
import shared

struct ContentView: View {
    @ObservedObject var viewModel = ShoppingListViewModel()

    @State private var itemNameToAdd: String = ""
    @State private var quantityToAdd: String = ""
    @State private var listName: String = ""
    @State private var listIdToRemove: String = ""

    var body: some View {
        VStack {
            Text("Shopping List App")
                .font(.title)

            // Ошибка
            if let errorMessage = viewModel.errorMessage {
                Text(errorMessage)
                    .foregroundColor(.red)
            }

            // Кнопка для создания ключа
            Button("Create Test Key") {
                viewModel.createTestKey()
            }

            // Ввод ключа
            TextField("Enter Test Key", text: $viewModel.testKey ?? "")
                .padding()
                .textFieldStyle(RoundedBorderTextFieldStyle())

            Button("Authenticate") {
                if let key = viewModel.testKey, !key.isEmpty {
                    viewModel.authenticate(key)
                }
            }

            // Ввод имени списка
            TextField("Enter Shopping List Name", text: $listName)
                .padding()
                .textFieldStyle(RoundedBorderTextFieldStyle())

            Button("Create Shopping List") {
                if !listName.isEmpty {
                    viewModel.createShoppingList(listName)
                }
            }

            if let newListId = viewModel.newListId {
                Text("Shopping List ID: \(newListId)")
            }

            // Удалить список
            TextField("Shopping List ID to Remove", text: $listIdToRemove)
                .padding()
                .keyboardType(.numberPad)

            Button("Remove/Restore Shopping List") {
                if let listId = Int(listIdToRemove) {
                    viewModel.removeShoppingList(listId)
                }
            }

            // Добавить товар
            TextField("Item Name", text: $itemNameToAdd)
                .padding()
                .textFieldStyle(RoundedBorderTextFieldStyle())

            TextField("Quantity", text: $quantityToAdd)
                .padding()
                .keyboardType(.numberPad)
                .textFieldStyle(RoundedBorderTextFieldStyle())

            Button("Add To Shopping List") {
                if let quantity = Int(quantityToAdd), !itemNameToAdd.isEmpty {
                    viewModel.addToShoppingList(itemNameToAdd, quantity)
                }
            }

            // Список покупок
            if let currentItems = viewModel.currentItems {
                List(currentItems) { item in
                    HStack {
                        Text("\(item.name) - \(item.quantity)")
                        Spacer()
                        Button("Cross Off") {
                            viewModel.crossItOff(item.id)
                        }
                        Button("Remove Item") {
                            viewModel.removeFromList(item.id)
                        }
                    }
                }
            }

            // Кнопка для получения всех списков
            Button("Load Shopping Lists") {
                if let key = viewModel.testKey, !key.isEmpty {
                    viewModel.getAllMyShopLists(key)
                }
            }

            // Показ списков покупок
            if let allShoppingLists = viewModel.allShoppingLists {
                List(allShoppingLists) { list in
                    VStack(alignment: .leading) {
                        Text("List ID: \(list.id)")
                        Text("Name: \(list.name)")
                        Button("Get Items") {
                            viewModel.getShoppingList(list.id)
                        }
                    }
                }
            }
        }
        .padding()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}