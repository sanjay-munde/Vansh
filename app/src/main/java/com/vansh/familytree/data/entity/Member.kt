package com.vansh.familytree.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "members")
data class Member(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val firstName: String,
    val middleName: String? = null,
    val lastName: String,
    val nickname: String? = null,
    val gender: Gender,
    val dateOfBirth: Long? = null,
    val placeOfBirth: String? = null,
    val isLiving: Boolean = true,
    val dateOfDeath: Long? = null,
    val biography: String? = null
)

enum class Gender {
    MALE, FEMALE, OTHER
}
