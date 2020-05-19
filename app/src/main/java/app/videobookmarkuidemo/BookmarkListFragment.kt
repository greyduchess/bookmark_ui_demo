package app.thumbnailbookmark.ui

import android.graphics.Rect
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.videobookmarkuidemo.MainActivityViewModel
import app.videobookmarkuidemo.BookmarkListFragmentViewModel
import app.videobookmarkuidemo.db.Bookmark
import app.videobookmarkuidemo.R
import app.videobookmarkuidemo.databinding.BookmarkBinding
import app.videobookmarkuidemo.databinding.FolderBinding
import app.videobookmarkuidemo.databinding.FragmentBookmarkListBinding
import java.io.Serializable

class BookmarkListFragment : Fragment() {

    class Arg(val folderId: Int) : Serializable

    companion object {
        fun newInstance(folderId: Int): BookmarkListFragment {
            val f = BookmarkListFragment().also {
                it.arguments = Bundle().also { it.putSerializable("arg", Arg(folderId = folderId)) }
            }
            return f
        }
    }

    lateinit var folderAdapter: BookmarkListAdapter
    lateinit var viewModel: BookmarkListFragmentViewModel
    lateinit var mainActivityViewModel: MainActivityViewModel

    fun getArg(): Arg {
        return requireArguments().getSerializable("arg") as Arg
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        folderAdapter = BookmarkListAdapter()
        folderAdapter.setHasStableIds(true)

        setHasOptionsMenu(true)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentBookmarkListBinding.inflate(inflater, container, false)
        binding.recyclerView.let {
            it.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            it.adapter = folderAdapter
            it.addItemDecoration(BookmarkItemDecorator(resources.getDimensionPixelSize(R.dimen.movehere_height_half)))
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProviders.of(this).get(BookmarkListFragmentViewModel::class.java)
        viewModel.startLoading(getArg().folderId)

        mainActivityViewModel =
            ViewModelProviders.of(requireActivity()).get(MainActivityViewModel::class.java)
        mainActivityViewModel.selectedBookmarkNum.observe(viewLifecycleOwner, Observer {
            folderAdapter.notifyDataSetChanged()
        })

        viewModel.bookmarkListLiveData.observe(viewLifecycleOwner, Observer { bookmarks ->
            folderAdapter.setData(bookmarks)
        })
        viewModel.folderInfoLiveData.observe(viewLifecycleOwner, Observer { bookmark ->
            mainActivityViewModel.setCurrentFolder(bookmark)
        })
    }

    inner class BookmarkListAdapter() :

        RecyclerView.Adapter<BookmarkListAdapterViewHolder>() {

        val TYPE_FOLDER: Int = 0
        val TYPE_BOOKMARK: Int = 1
        val TYPE_BOTTOM_PADDING: Int = 2

        var bookmarks: List<Bookmark>? = null

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): BookmarkListAdapterViewHolder {
            when (viewType) {
                TYPE_FOLDER -> {
                    val binding = FolderBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                    return BookmarkListAdapterFolderViewHolder(binding)
                }
                TYPE_BOOKMARK -> {
                    val binding = BookmarkBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                    return BookmarkListAdapterUrlViewHolder(binding)
                }
                TYPE_BOTTOM_PADDING -> {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.bottom_padding, parent, false)
                    return BookmarkListAdapterBottomPaddingViewHolder(view)
                }
                else -> throw RuntimeException("unknown type")
            }
        }

        override fun getItemViewType(position: Int): Int {
            if (position >= bookmarks?.size ?: 0) {
                return TYPE_BOTTOM_PADDING
            }
            var idx = position
            if (bookmarks!!.get(idx).isFolder) {
                return TYPE_FOLDER
            } else {
                return TYPE_BOOKMARK
            }
        }

        fun getBookmarkIdx(position: Int): Int {
            var idx = position
            return idx
        }

        fun setData(bookmarks: List<Bookmark>) {
            this.bookmarks = bookmarks
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int {
            return (bookmarks?.size ?: 0) + 1
        }

        override fun getItemId(position: Int): Long {
            when (getItemViewType(position)) {
                TYPE_BOTTOM_PADDING -> {
                    return 0 // autoincrementのIDと重ならないID
                }
                else -> {
                    return bookmarks!!.get(getBookmarkIdx(position)).id.toLong()
                }
            }
        }

        override fun onBindViewHolder(holder: BookmarkListAdapterViewHolder, position: Int) {

            val isSelectionMode = mainActivityViewModel.isInSelectionMode()

            when (holder) {
                is BookmarkListAdapterFolderViewHolder -> {
                    val idx = getBookmarkIdx(position)
                    val bookmark = bookmarks!!.get(idx)
                    val isSelected = mainActivityViewModel.isSelected(bookmark)

                    val binding = holder.binding

                    binding.foreground.setOnClickListener { onShortClick(bookmark, idx) }
                    binding.foreground.setOnLongClickListener { onLongClick(bookmark, idx); true }
                    if (isSelected) {
                        binding.foreground.setBackgroundResource(R.drawable.bookmark_background_selected)
                    } else {
                        binding.foreground.setBackgroundResource(R.drawable.bookmark_background_notselected)
                    }
                    binding.text.text = bookmark.title
                    binding.insert.insert.visibility =
                        if (isSelectionMode) View.VISIBLE else View.INVISIBLE
                    binding.insert.insert.setOnClickListener { onMoveHereClicked(position); }
                }
                is BookmarkListAdapterUrlViewHolder -> {
                    val idx = getBookmarkIdx(position)
                    val bookmark = bookmarks!!.get(idx)
                    val isSelected = mainActivityViewModel.isSelected(bookmark)

                    val binding = holder.binding

                    binding.foreground.setOnClickListener { onShortClick(bookmark, idx) }
                    binding.foreground.setOnLongClickListener { onLongClick(bookmark, idx); true }
                    if (isSelected) {
                        binding.foreground.setBackgroundResource(R.drawable.bookmark_background_selected)
                    } else {
                        binding.foreground.setBackgroundResource(R.drawable.bookmark_background_notselected)
                    }
                    binding.text.text = bookmark.title

                    binding.insert.insert.visibility =
                        if (isSelectionMode) View.VISIBLE else View.INVISIBLE
                    binding.insert.insert.setOnClickListener { onMoveHereClicked(position); }
                }
                is BookmarkListAdapterBottomPaddingViewHolder -> {
                    holder.moveHereButton.visibility =
                        if (isSelectionMode) View.VISIBLE else View.INVISIBLE
                    holder.moveHereButton.setOnClickListener { onMoveHereClicked(position); }
                }
            }
        }

        private fun onShortClick(bookmark: Bookmark, position: Int) {
            if (bookmark.isFolder) {
                if (mainActivityViewModel.isSelected(bookmark)) {
                    // selectされているフォルダの中には入れない。clickで選択解除
                    mainActivityViewModel.toggleSelection(bookmark)
                    //notifyItemChanged(position)
                } else {
                    fragmentManager?.let {
                        val f = BookmarkListFragment.newInstance(bookmark.id)
                        it.beginTransaction().replace(R.id.fragment_container, f)
                            .addToBackStack(null).commit()
                    }

                }
            } else {
                if (mainActivityViewModel.isInSelectionMode()) {
                    mainActivityViewModel.toggleSelection(bookmark)
                    //notifyItemChanged(position)
                } else {
                    mainActivityViewModel.toggleSelection(bookmark)
                    //mainActivityViewModel.openExternalBrowser(bookmark.url)
                }
            }
        }

        private fun onLongClick(bookmark: Bookmark, position: Int) {
            mainActivityViewModel.toggleSelection(bookmark)
        }

        private fun onMoveHereClicked(position: Int) {
            // positionの上に挿入する。
            val low: Double
            val high: Double
            when {
                itemCount == 1 -> {
                    // paddingしかなく、bookmarkやfolderが一つもない
                    low = 0.0
                    high = 1.0
                }
                position == itemCount - 1 -> {
                    // 最後のpadding部をクリック
                    low = bookmarks!!.get(position - 1).sortOrder
                    high = low + 1
                }
                position == 0 -> {
                    high = bookmarks!!.get(position).sortOrder
                    low = high - 1
                }
                else -> {
                    low = bookmarks!!.get(position - 1).sortOrder
                    high = bookmarks!!.get(position).sortOrder
                }
            }
            mainActivityViewModel.moveSelected(getArg().folderId, low, high)
        }

    }

}

class BookmarkItemDecorator(val offset: Int): RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == 0) {
            outRect.set(0, 0, 0, 0)
        } else {
            outRect.set(0, -offset, 0, 0)
        }
    }
}


open class BookmarkListAdapterViewHolder(val root: View) : RecyclerView.ViewHolder(root) {
}

class BookmarkListAdapterFolderViewHolder(val binding: FolderBinding) :
    BookmarkListAdapterViewHolder(binding.root)

class BookmarkListAdapterUrlViewHolder(val binding: BookmarkBinding) :
    BookmarkListAdapterViewHolder(binding.root)

class BookmarkListAdapterBottomPaddingViewHolder(root: View) : BookmarkListAdapterViewHolder(root) {
    val moveHereButton = root.findViewById<View>(R.id.insert)
}

