package app.videobookmarkuidemo.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "bookmark")
data class Bookmark(
    @PrimaryKey(autoGenerate = true) // autoincrement
    val id: Int,
    val parentId: Int, // 0 is root
    val isFolder: Boolean,
    val sortOrder: Double,
    val title: String?
) : Serializable
