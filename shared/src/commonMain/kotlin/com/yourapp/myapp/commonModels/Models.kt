package com.yourapp.myapp.commonModels

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemModel(
    val name: String,
    val id: Int,
    @SerialName("is_crossed") var isCrossedOff: Boolean,
    val created: Int,
    val listId: Int? = null// Теперь обязательное поле
)

@Serializable
data class ShopListModel(
    @SerialName("key") val key: String? = null,
    val id: Int,
    val name: String,
    )


@Serializable
data class RemoveShoppingListResponse(
    val success: Boolean,
    val new_value: Boolean
)

@Serializable
data class ResponseModel(
    val success: Boolean,
    @SerialName("item_list") val itemList: List<ItemModel>? = null,
    @SerialName("shop_list") val shopList: List<ShopListModel>? = null,
    @SerialName("list_id") val listId: Int? = null
)
