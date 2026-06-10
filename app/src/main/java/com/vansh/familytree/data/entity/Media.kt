package com.vansh.familytree.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "media",
    foreignKeys = [
        ForeignKey(
            entity = Member::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("memberId")]
)
data class Media(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val memberId: String,
    val uri: String,
    val type: MediaType,
    val title: String? = null,
    val isProfilePhoto: Boolean = false
)

enum class MediaType {
    PHOTO, DOCUMENT, AUDIO, VIDEO
}
