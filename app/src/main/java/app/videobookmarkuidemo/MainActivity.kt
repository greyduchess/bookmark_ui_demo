package app.videobookmarkuidemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import app.thumbnailbookmark.ui.BookmarkListFragment
import app.videobookmarkuidemo.databinding.ActivityMainBinding
import app.videobookmarkuidemo.db.AppDatabase
import app.videobookmarkuidemo.db.Bookmark
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        insertSampleDataIfDbEmpty()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        if (savedInstanceState == null) {
            val f = BookmarkListFragment.newInstance(0)
            supportFragmentManager.beginTransaction().add(R.id.fragment_container, f).commit()
        }

        setSupportActionBar(binding.toolbar)

        supportActionBar!!.title = ""

        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
        viewModel.currentFolder.observe(this, Observer {
            if (it == null) {
                binding.toolbar.title = "Bookmarks"
                binding.toolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp)
                binding.toolbar.setNavigationOnClickListener {
                }
            } else {
                binding.toolbar.title = it.title
                binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
                binding.toolbar.setNavigationOnClickListener {
                    supportFragmentManager.popBackStack()
                }
            }
        })

        binding.bottomToolbar.visibility = View.GONE
        binding.bottomToolbar.setNavigationOnClickListener { v -> onBottonToolbarNavigationClicked() }

        viewModel.selectedBookmarkNum.observe(this, Observer { num ->
            if (num == 0) {
                binding.bottomToolbar.visibility = View.GONE
            } else {
                binding.bottomToolbar.visibility = View.VISIBLE
                binding.bottomToolbar.title = "${num} selected"
            }
        })
    }

    private fun onBottonToolbarNavigationClicked() {
        viewModel.clearSelection()
    }

    private fun insertSampleDataIfDbEmpty() {
        GlobalScope.launch {
            val dao = AppDatabase.getInstance(application).bookmarkDao()
            if (dao.getAllSync(0).isEmpty()) {
                dao.insert(Bookmark(
                    id = 1,
                    parentId = 0,
                    title = "folder 1",
                    sortOrder = 0.0,
                    isFolder = true
                ))
                dao.insert(Bookmark(
                    id = 2,
                    parentId = 0,
                    title = "folder 2",
                    sortOrder = 1.0,
                    isFolder = true
                ))
                dao.insert(Bookmark(
                    id = 3,
                    parentId = 0,
                    title = "folder 3",
                    sortOrder = 2.0,
                    isFolder = true
                ))
                dao.insert(Bookmark(
                    id = 4,
                    parentId = 0,
                    title = "bookmark A",
                    sortOrder = 3.0,
                    isFolder = false
                ))
                dao.insert(Bookmark(
                    id = 5,
                    parentId = 0,
                    title = "bookmark B",
                    sortOrder = 4.0,
                    isFolder = false
                ))
                dao.insert(Bookmark(
                    id = 6,
                    parentId = 0,
                    title = "bookmark C",
                    sortOrder = 5.0,
                    isFolder = false
                ))
                dao.insert(Bookmark(
                    id = 7,
                    parentId = 0,
                    title = "bookmark D",
                    sortOrder = 6.0,
                    isFolder = false
                ))
            }

        }
    }
}
