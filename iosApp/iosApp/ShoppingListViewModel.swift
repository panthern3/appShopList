import Foundation
import shared

class ShoppingListViewModel: ObservableObject {
    @Published var testKey: String? = nil
    @Published var testKeyInput: String = ""
    @Published var errorMessage: String? = nil
    @Published var newListId: Int? = nil
    @Published var newListIdInput: String = ""
    @Published var allShoppingLists: [ShopListModel] = []
    @Published var operationStatus: String? = nil

    private let repository = ShoppingListRepository()

    func createTestKey() {
        repository.createTestKey { result, error in
            if let result = result {
                DispatchQueue.main.async {
                    self.testKey = result
                    self.errorMessage = nil
                }
            } else if let error = error {
                DispatchQueue.main.async {
                    self.errorMessage = error.localizedDescription
                }
            }
        }
    }

    func authenticate() {
        guard !testKeyInput.isEmpty else {
            errorMessage = "Test key is empty."
            return
        }

        repository.authenticate(key: testKeyInput) { success, error in
            DispatchQueue.main.async {
                if success {
                    self.errorMessage = nil
                } else {
                    self.errorMessage = error?.localizedDescription ?? "Authentication failed."
                }
            }
        }
    }

    func createShoppingList(name: String) {
        guard let key = testKey, !key.isEmpty else {
            errorMessage = "Test key is not available."
            return
        }

        repository.createShoppingList(key: key, name: name) { result, error in
            DispatchQueue.main.async {
                if let id = result {
                    self.newListId = id
                    self.errorMessage = nil
                } else if let error = error {
                    self.errorMessage = error.localizedDescription
                }
            }
        }
    }

    func removeShoppingList(listId: Int) {
        repository.removeShoppingList(listId: listId) { success, error in
            DispatchQueue.main.async {
                if success {
                    self.operationStatus = "List \(listId) successfully updated."
                } else {
                    self.operationStatus = error?.localizedDescription ?? "Failed to update list status."
                }
            }
        }
    }

    func addToShoppingList(itemName: String, quantity: Int) {
        guard let listId = Int(newListIdInput) else {
            errorMessage = "Invalid list ID."
            return
        }

        repository.addToShoppingList(listId: listId, itemName: itemName, quantity: quantity) { success, error in
            DispatchQueue.main.async {
                if success {
                    self.errorMessage = nil
                } else {
                    self.errorMessage = error?.localizedDescription ?? "Failed to add item."
                }
            }
        }
    }

    func getShoppingList(listId: Int) {
        repository.getShoppingList(listId: listId) { items, error in
            DispatchQueue.main.async {
                if let items = items {
                    self.errorMessage = nil
                } else if let error = error {
                    self.errorMessage = error.localizedDescription
                }
            }
        }
    }

    func getAllShoppingLists() {
        guard let key = testKey, !key.isEmpty else {
            errorMessage = "Test key is empty."
            return
        }

        repository.getAllMyShopLists(key: key) { lists, error in
            DispatchQueue.main.async {
                if let lists = lists {
                    self.allShoppingLists = lists
                } else if let error = error {
                    self.errorMessage = error.localizedDescription
                }
            }
        }
    }
}
