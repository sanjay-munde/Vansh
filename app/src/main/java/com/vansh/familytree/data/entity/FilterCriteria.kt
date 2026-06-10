package com.vansh.familytree.data.entity

data class FilterCriteria(
    val query: String = "",
    val isLiving: Boolean? = null,
    val gender: Gender? = null,
    val placeOfBirth: String? = null
)
