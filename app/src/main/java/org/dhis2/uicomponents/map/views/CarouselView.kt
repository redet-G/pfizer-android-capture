package org.dhis2.uicomponents.map.views

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.mapbox.geojson.Feature
import kotlin.math.abs
import org.dhis2.uicomponents.map.camera.centerCameraOnFeature
import org.dhis2.uicomponents.map.camera.centerCameraOnFeatures
import org.dhis2.uicomponents.map.carousel.CarouselAdapter
import org.dhis2.uicomponents.map.carousel.CarouselBinder
import org.dhis2.uicomponents.map.carousel.CarouselLayoutManager
import org.dhis2.uicomponents.map.geometry.isPoint
import org.dhis2.uicomponents.map.managers.MapManager

class CarouselView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    var carouselEnabled: Boolean = true
    private var carouselAdapter: CarouselAdapter? = null
    private val CAROUSEL_SMOOTH_THREADSHOLD = 10
    private val CAROUSEL_PROXIMITY_THREADSHOLD = 3
    private lateinit var mapManager: MapManager

    init {
        layoutManager = CarouselLayoutManager(context, HORIZONTAL, false)
        itemAnimator = DefaultItemAnimator()
        LinearSnapHelper().attachToRecyclerView(this)
    }

    fun setAdapter(adapter: CarouselAdapter?) {
        super.setAdapter(adapter)
        this.carouselAdapter = adapter
    }

    fun attachToMapManager(
        manager: MapManager
    ) {
        mapManager = manager
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (
                    newState == SCROLL_STATE_DRAGGING ||
                    newState == SCROLL_STATE_SETTLING &&
                    carouselEnabled
                ) {
                    showNavFab(null, false)
                }
                if (newState == SCROLL_STATE_IDLE && carouselEnabled) {
                    mapManager.requestMapLayerManager()?.selectFeature(null)
                    val features = mapManager.findFeatures(currentItem())
                    if (features != null && features.isNotEmpty() && features.size > 1) {
                        mapManager.map?.centerCameraOnFeatures(features)
                        showNavFab(features.firstOrNull(), true)
                    } else {
                        val feature = mapManager.findFeature(currentItem())
                        if (feature == null) {
                            showNavFab(feature, false)
                        } else {
                            mapManager.map?.centerCameraOnFeature(feature)
                            showNavFab(feature, true)
                        }
                    }
                }
            }
        })
    }

    fun selectFirstItem() {
        if (mapManager.style?.isFullyLoaded == true) {
            carouselAdapter?.let { adapter ->
                if (adapter.itemCount > 0) {
                    val feature = mapManager.findFeature(currentItem())
                    if (feature != null) {
                        mapManager.mapLayerManager.selectFeature(feature)
                        showNavFab(feature, true)
                    } else {
                        showNavFab(feature, false)
                    }
                }
            }
        }
    }

    private fun getVisiblePosition(): Int {
        var visiblePosition =
            (layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
        if (visiblePosition == -1) {
            visiblePosition = (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        }
        if (visiblePosition == -1) {
            visiblePosition = 0
        }
        return visiblePosition
    }

    fun currentItem(): String {
        return carouselAdapter?.getUidProperty(getVisiblePosition()) ?: ""
    }

    fun scrollToFeature(feature: Feature) {
        if (carouselEnabled) {
            val initialPosition = (layoutManager as LinearLayoutManager)
                .findFirstCompletelyVisibleItemPosition()
            val endPosition = carouselAdapter?.indexOfFeature(feature) ?: -1

            if (initialPosition == -1 || endPosition == -1) {
                return
            }

            when {
                abs(endPosition - initialPosition) < CAROUSEL_SMOOTH_THREADSHOLD ->
                    smoothScrollToPosition(endPosition)
                endPosition > initialPosition -> {
                    smoothScrollToPosition(initialPosition + CAROUSEL_PROXIMITY_THREADSHOLD)
                    scrollToPosition(endPosition - CAROUSEL_PROXIMITY_THREADSHOLD)
                    smoothScrollToPosition(endPosition)
                }
                else -> {
                    smoothScrollToPosition(initialPosition - CAROUSEL_PROXIMITY_THREADSHOLD)
                    scrollToPosition(endPosition + CAROUSEL_PROXIMITY_THREADSHOLD)
                    smoothScrollToPosition(endPosition)
                }
            }
        }
    }

    fun setEnabledStatus(enabled: Boolean) {
        this.carouselEnabled = enabled
        (layoutManager as CarouselLayoutManager).setEnabled(enabled)
    }

    private fun showNavigateTo() {
        val visiblePosition = getVisiblePosition()
        findViewHolderForAdapterPosition(visiblePosition - 1)?.let { it as CarouselBinder<*> }
            ?.hideNavigateButton()
        findViewHolderForAdapterPosition(visiblePosition)?.let { it as CarouselBinder<*> }
            ?.showNavigateButton()
        findViewHolderForAdapterPosition(visiblePosition + 1)?.let { it as CarouselBinder<*> }
            ?.hideNavigateButton()
    }

    private fun hideNavigateTo() {
        val visiblePosition = getVisiblePosition()
        findViewHolderForAdapterPosition(visiblePosition - 1)?.let { it as CarouselBinder<*> }
            ?.hideNavigateButton()
        findViewHolderForAdapterPosition(visiblePosition)?.let { it as CarouselBinder<*> }
            ?.hideNavigateButton()
        findViewHolderForAdapterPosition(visiblePosition + 1)?.let { it as CarouselBinder<*> }
            ?.hideNavigateButton()
    }

    private fun showNavFab(
        feature: Feature?,
        found: Boolean
    ) {
        if (found && feature?.isPoint() == true) {
            showNavigateTo()
        } else {
            hideNavigateTo()
        }
    }
}
