package app.videobookmarkuidemo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.videobookmarkuidemo.db.AppDatabase
import app.videobookmarkuidemo.db.Bookmark
import app.videobookmarkuidemo.db.BookmarkDao
import kotlinx.coroutines.*

class MainActivityViewModel(app: Application) : AndroidViewModel(app) {

    private val _currentFolder = MutableLiveData<Bookmark>()
    val currentFolder: LiveData<Bookmark> // nullならroot。
        get() = _currentFolder
    private var _selectedBookmark = arrayListOf<Int>()
    val selectedBookmarkNum = MutableLiveData<Int>()

    fun setCurrentFolder(bookmark: Bookmark?) {
        _currentFolder.value = bookmark
    }

    fun isInSelectionMode(): Boolean {
        return _selectedBookmark.size > 0
    }

    fun toggleSelection(bookmark: Bookmark) {
        if (!_selectedBookmark.remove(bookmark.id)) {
            _selectedBookmark.add(bookmark.id)
        }
        selectedBookmarkNum.value = _selectedBookmark.size
    }

    fun isSelected(bookmark: Bookmark): Boolean {
        return _selectedBookmark.contains(bookmark.id)
    }

    fun clearSelection() {
        _selectedBookmark.clear()
        selectedBookmarkNum.value = _selectedBookmark.size
    }

    fun moveSelected(parentId: Int, sortOrderLow: Double, sortOrderHigh: Double) {
        val bookmarks = arrayListOf<Int>()
        bookmarks.addAll(_selectedBookmark)
        GlobalScope.launch {
            bookmarks.size.let { size ->
                if (size > 0) {
                    // low                  high
                    //  |----|---|---|---|---|
                    // lowとhighの間に3つ入れる時。3+2等分する。
                    val delta = (sortOrderHigh - sortOrderLow) / (size + 2)
                    AppDatabase.getInstance(getApplication()).let { db->
                        db.runInTransaction {
                            for ((idx, id) in bookmarks.withIndex()) {
                                db.bookmarkDao().updateParentAndSortOrder(id, parentId, sortOrderLow + delta * (idx + 1))
                            }
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    clearSelection()
                }
            }

        }
    }
}