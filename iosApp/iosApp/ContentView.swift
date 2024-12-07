import SwiftUI
import shared

struct ContentView: View {
    @StateObject private var viewModel = ShoppingListViewModel() // iOS ViewModel

    @State private var listName: String = ""
    @State private var itemNameToAdd: String = ""
    @State private var quantityToAdd: String = ""
    @State private var listIdToRemove: String = ""
    @State private var listIdToLoad: String = ""

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 16) {
                    Text("Shopping List App")
                        .font(.headline)

                    if let errorMessage = viewModel.errorMessage {
                        Text(errorMessage)
                            .foregroundColor(.red)
                    }

                    // 1. Create Test Key
                    Button("Create Test Key") {
                        viewModel.createTestKey()
                    }

                    if let testKey = viewModel.testKey {
                        Text("Test Key: \(testKey)")
                    }

                    // 2. Authenticate
                    TextField("Enter Test Key", text: $viewModel.testKeyInput)
                        .textFieldStyle(RoundedBorderTextFieldStyle())

                    Button("Authenticate") {
                        viewModel.authenticate()
                    }

                    // 3. Create Shopping List
                    TextField("Enter Shopping List Name", text: $listName)
                        .textFieldStyle(RoundedBorderTextFieldStyle())

                    Button("Create Shopping List") {
                        viewModel.createShoppingList(name: listName)
                    }

                    if let newListId = viewModel.newListId {
                        Text("Shopping List ID: \(newListId)")
                    }

                    // 4. Remove/Restore Shopping List
                    TextField("Shopping List ID to Remove/Restore", text: $listIdToRemove)
                        .textFieldStyle(RoundedBorderTextFieldStyle())

                    Button("Remove/Restore Shopping List") {
                        if let listId = Int(listIdToRemove) {
                            viewModel.removeShoppingList(listId: listId)
                        }
                    }

                    if let operationStatus = viewModel.operationStatus {
                        Text(operationStatus)
                            .foregroundColor(operationStatus.contains("successfully") ? .green : .red)
                    }

                    // 5. Add to Shopping List
                    TextField("List ID", text: $viewModel.newListIdInput)
                        .textFieldStyle(RoundedBorderTextFieldStyle())

                    TextField("Item Name", text: $itemNameToAdd)
                        .textFieldStyle(RoundedBorderTextFieldStyle())

                    TextField("Quantity", text: $quantityToAdd)
                        .keyboardType(.numberPad)
                        .textFieldStyle(RoundedBorderTextFieldStyle())

                    Button("Add To Shopping List") {
                        if let quantity = Int(quantityToAdd) {
                            viewModel.addToShoppingList(itemName: itemNameToAdd, quantity: quantity)
                        }
                    }

                    // 6. Get Shopping List
                    TextField("Shopping List ID to Load", text: $listIdToLoad)
                        .textFieldStyle(RoundedBorderTextFieldStyle())

                    Button("Get Shopping List") {
                        if let listId = Int(listIdToLoad) {
                            viewModel.getShoppingList(listId: listId)
                        }
                    }

                    // Display Shopping Lists
                    ForEach(viewModel.allShoppingLists, id: \.id) { list in
                        ShoppingListRow(shopList: list) {
                            viewModel.getShoppingList(listId: list.id)
                        }
                    }
                }
                .padding()
            }
            .navigationBarTitle("Shopping List")
        }
    }
}

struct ShoppingListRow: View {
    let shopList: ShopListModel
    let onGetItems: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("List ID: \(shopList.id)")
            Text("Name: \(shopList.name)")
            Button("Get Items") {
                onGetItems()
            }
            .padding(.vertical, 4)
        }
        .padding()
        .background(Color.gray.opacity(0.1))
        .cornerRadius(8)
    }
}
