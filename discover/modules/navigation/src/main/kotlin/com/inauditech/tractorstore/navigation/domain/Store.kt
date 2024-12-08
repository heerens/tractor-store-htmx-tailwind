package com.inauditech.tractorstore.navigation.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

data class Store(
    val id: StoreId,
    val name: String,
    val street: String,
    val city: String,
    val image: String,
)

data class StoreId
    @JsonCreator
    constructor(
        @get:JsonValue val value: String,
    )
