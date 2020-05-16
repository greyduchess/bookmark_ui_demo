package app.videobookmarkuidemo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import app.videobookmarkuidemo.db.AppDatabase
import app.videobookmarkuidemo.db.Bookmark


class BookmarkListFragmentViewModel(app: Application) : AndroidViewModel(app) {

    var folderId: Int? = null
    var started = false
    val bookmarkListLiveData = MediatorLiveData<List<Bookmark>>()
    val folderInfoLiveData = MediatorLiveData<Bookmark?>()

    fun startLoading(folderId: Int) {
        this.folderId = folderId
        if (started) {
            return
        }
        started = true

        bookmarkListLiveData.addSource(
            AppDatabase.getInstance(getApplication()).bookmarkDao().getAll(folderId)
        ) {
            bookmarkListLiveData.value = it
        }


        folderInfoLiveData.addSource(
            AppDatabase.getInstance(getApplication()).bookmarkDao().get(folderId)
        ) {
            folderInfoLiveData.value = it
        }
    }

}
