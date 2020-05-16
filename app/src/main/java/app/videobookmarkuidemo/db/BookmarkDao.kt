package app.videobookmarkuidemo.db

import androidx.lifecycle.LiveData
import androidx.room.*
import app.videobookmarkuidemo.db.Bookmark

@Dao
interface BookmarkDao {

    @Query("SELECT * FROM bookmark WHERE id=:id")
    fun get(id: Int): LiveData<Bookmark?>

    @Query("SELECT * FROM bookmark WHERE parentId=:parentId ORDER BY sortOrder ASC")
    fun getAll(parentId: Int): LiveData<List<Bookmark>>

    @Query("SELECT * FROM bookmark WHERE parentId=:parentId ORDER BY sortOrder ASC")
    fun getAllSync(parentId: Int): List<Bookmark>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(bookmark: Bookmark): Long

    @Query("UPDATE bookmark SET parentId=:parentId, sortOrder=:sortOrder WHERE id=:id")
    fun updateParentAndSortOrder(id: Int, parentId: Int, sortOrder: Double)

}