package com.cbs.demospike

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.DetailsSupportFragment
import androidx.leanback.app.DetailsSupportFragmentBackgroundController
import androidx.leanback.widget.*
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.cbs.demospike.VideoDetailsFragmentArgs.Companion.fromBundle
import java.util.*

/**
 * A wrapper fragment for leanback details screens.
 * It shows a detailed view of video and its metadata plus related videos.
 */
class VideoDetailsFragment : DetailsSupportFragment() {

    private var mSelectedMovie: Movie? = null

    private lateinit var mDetailsBackground: DetailsSupportFragmentBackgroundController
    private lateinit var mPresenterSelector: ClassPresenterSelector
    private lateinit var mAdapter: ArrayObjectAdapter

    private val item by lazy {
        fromBundle(requireArguments()).item
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate DetailsFragment")
        super.onCreate(savedInstanceState)
        mDetailsBackground = DetailsSupportFragmentBackgroundController(this)
        if (item >= MovieList.list.size) {
            requireActivity().redirectToMain()
            return
        }
        mSelectedMovie = MovieList.list.firstOrNull { it.id == item }
        if (mSelectedMovie != null) {
            mPresenterSelector = ClassPresenterSelector()
            mAdapter = ArrayObjectAdapter(mPresenterSelector)
            setupDetailsOverviewRow()
            setupDetailsOverviewRowPresenter()
            setupRelatedMovieListRow()
            adapter = mAdapter
            initializeBackground(mSelectedMovie)
            onItemViewClickedListener = ItemViewClickedListener()
        } else {
            requireActivity().redirectToMain()
        }
    }

    override fun onResume() {
        super.onResume()
        mSelectedMovie?.let {
            requireActivity().intent?.data?.run {
                if (this.toString().contains(PLAY)) {
                    playMovie(it)
                    requireActivity().intent?.data = null
                }
            }
        }
    }

    private fun initializeBackground(movie: Movie?) {
        mDetailsBackground.enableParallax()
        Glide.with(activity)
            .load(movie?.backgroundImageUrl)
            .asBitmap()
            .centerCrop()
            .error(R.drawable.default_background)
            .into<SimpleTarget<Bitmap>>(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(
                    bitmap: Bitmap,
                    glideAnimation: GlideAnimation<in Bitmap>
                ) {
                    mDetailsBackground.coverBitmap = bitmap
                    mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size())
                }
            })
    }

    private fun setupDetailsOverviewRow() {
        val row = DetailsOverviewRow(mSelectedMovie)

        row.imageDrawable =
            ContextCompat.getDrawable(requireContext(), R.drawable.default_background)
        val width = convertDpToPixel(requireContext(), DETAIL_THUMB_WIDTH)
        val height = convertDpToPixel(requireContext(), DETAIL_THUMB_HEIGHT)

        Glide.with(activity)
            .load(mSelectedMovie?.cardImageUrl)
            .centerCrop()
            .error(R.drawable.default_background)
            .into<SimpleTarget<GlideDrawable>>(object : SimpleTarget<GlideDrawable>(width, height) {
                override fun onResourceReady(
                    resource: GlideDrawable,
                    glideAnimation: GlideAnimation<in GlideDrawable>
                ) {
                    row.imageDrawable = resource
                    mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size())
                }
            })


        val actionAdapter = ArrayObjectAdapter()

        actionAdapter.add(
            Action(
                ACTION_WATCH_TRAILER,
                resources.getString(R.string.watch_trailer_1),
                resources.getString(R.string.watch_trailer_2)
            )
        )
        actionAdapter.add(
            Action(
                ACTION_RENT,
                resources.getString(R.string.rent_1),
                resources.getString(R.string.rent_2)
            )
        )
        actionAdapter.add(
            Action(
                ACTION_BUY,
                resources.getString(R.string.buy_1),
                resources.getString(R.string.buy_2)
            )
        )
        row.actionsAdapter = actionAdapter

        mAdapter.add(row)
    }

    private fun setupDetailsOverviewRowPresenter() {
        // Set detail background.
        val detailsPresenter = FullWidthDetailsOverviewRowPresenter(DetailsDescriptionPresenter())

        detailsPresenter.backgroundColor =
            ContextCompat.getColor(requireContext(), R.color.selected_background)

        // Hook up transition element.
        val sharedElementHelper = FullWidthDetailsOverviewSharedElementHelper()
        sharedElementHelper.setSharedElementEnterTransition(
            activity, SHARED_ELEMENT_NAME
        )
        detailsPresenter.setListener(sharedElementHelper)
        detailsPresenter.isParticipatingEntranceTransition = true

        detailsPresenter.onActionClickedListener = OnActionClickedListener { action ->
            if (action.id == ACTION_WATCH_TRAILER) {
                mSelectedMovie?.let {
                    playMovie(it)
                }
            } else {
                Toast.makeText(activity, action.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        mPresenterSelector.addClassPresenter(DetailsOverviewRow::class.java, detailsPresenter)
    }

    private fun playMovie(mSelectedMovie: Movie) {
        //VideoPlayer runs on separate Activity
        val intent = Intent(activity, PlaybackActivity::class.java)
        intent.putExtra(MOVIE, mSelectedMovie)
        startActivity(intent)
    }

    private fun setupRelatedMovieListRow() {
        val subcategories = arrayOf(getString(R.string.related_movies))
        val list = MovieList.list

        Collections.shuffle(list)
        val listRowAdapter = ArrayObjectAdapter(CardPresenter())
        for (j in 0 until NUM_COLS) {
            listRowAdapter.add(list[j % 5])
        }

        val header = HeaderItem(0, subcategories[0])
        mAdapter.add(ListRow(header, listRowAdapter))
        mPresenterSelector.addClassPresenter(ListRow::class.java, ListRowPresenter())
    }

    private fun convertDpToPixel(context: Context, dp: Int): Int {
        val density = context.applicationContext.resources.displayMetrics.density
        return Math.round(dp.toFloat() * density)
    }

    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(
            itemViewHolder: Presenter.ViewHolder?,
            item: Any?,
            rowViewHolder: RowPresenter.ViewHolder,
            row: Row
        ) {
            if (item is Movie) {
                val action = VideoDetailsFragmentDirections.actionDetailSelf(item.id)
                requireView().findNavController().navigate(action)
            }
        }
    }

    companion object {
        private val TAG = "VideoDetailsFragment"

        private val ACTION_WATCH_TRAILER = 1L
        private val ACTION_RENT = 2L
        private val ACTION_BUY = 3L

        private val DETAIL_THUMB_WIDTH = 274
        private val DETAIL_THUMB_HEIGHT = 274

        private val NUM_COLS = 10

        const val SHARED_ELEMENT_NAME = "hero"
        const val MOVIE = "Movie"

        const val PLAY = "play"
    }
}

fun FragmentActivity.redirectToMain() {
    this.finish()
    val intent = Intent(this, MainNavigationActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    startActivity(intent)
}
