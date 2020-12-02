package org.dhis2.uicomponents.map.managers

import android.annotation.SuppressLint
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager
import org.dhis2.uicomponents.map.camera.initCameraToViewAllElements
import org.dhis2.uicomponents.map.carousel.CarouselAdapter
import org.dhis2.uicomponents.map.layer.MapLayerManager

abstract class MapManager(val mapView: MapView) {

    var map: MapboxMap? = null
    lateinit var mapLayerManager: MapLayerManager
    var markerViewManager: MarkerViewManager? = null
    var symbolManager: SymbolManager? = null
    var onMapClickListener: MapboxMap.OnMapClickListener? = null
    var onMapLongClickListener: MapboxMap.OnMapLongClickListener? = null
    var carouselAdapter: CarouselAdapter? = null
    var style: Style? = null
    var permissionsManager: PermissionsManager? = null

    fun init(
        onInitializationFinished: () -> Unit = {},
        onMissingPermission: (PermissionsManager?) -> Unit
    ) {
        if (style == null) {
            mapView.getMapAsync { mapLoaded ->
                this.map = mapLoaded
                map?.setStyle(Style.MAPBOX_STREETS) { styleLoaded ->
                    this.style = styleLoaded
                    mapLayerManager = MapLayerManager(mapLoaded).apply {
                        styleChangeCallback = { newStyle ->
                            style = newStyle
                            mapLayerManager.clearLayers()
                            loadDataForStyle()
                            setSource()
                        }
                    }
                    onMapClickListener?.let { mapClickListener ->
                        map?.addOnMapClickListener(mapClickListener)
                    }
                    onMapLongClickListener?.let {
                        map.addOnMapLongClickListener(it)
                    }
                    markerViewManager = MarkerViewManager(mapView, map)
                    loadDataForStyle()
                    enableLocationComponent(styleLoaded, onMissingPermission)
                    onInitializationFinished()
                }
            }
        } else {
            onInitializationFinished()
        }
    }

    abstract fun loadDataForStyle()
    abstract fun setSource()
    abstract fun setLayer()

    fun initCameraPosition(
        boundingBox: BoundingBox
    ) {
        val bounds = LatLngBounds.Builder()
            .include(pointToLatLn(boundingBox.northeast()))
            .include(pointToLatLn(boundingBox.southwest()))
            .build()
        map?.initCameraToViewAllElements(
            mapView.context, bounds
        )
    }

    fun pointToLatLn(point: Point): LatLng {
        return LatLng(point.latitude(), point.longitude())
    }

    fun isMapReady() = map != null && style?.isFullyLoaded ?: false

    fun onStart() {
        mapView.onStart()
        map?.locationComponent?.onStart()
    }

    fun onResume() {
        mapView.onResume()
    }

    fun onPause() {
        mapView.onPause()
    }

    @SuppressLint("MissingPermission")
    fun onDestroy() {
        markerViewManager?.onDestroy()
        symbolManager?.onDestroy()
        map?.locationComponent?.onStop()
    }

    abstract fun findFeature(source: String, propertyName: String, propertyValue: String): Feature?
    open fun findFeatures(
        source: String,
        propertyName: String,
        propertyValue: String
    ): List<Feature>? {
        return emptyList()
    }

    abstract fun findFeature(propertyValue: String): Feature?
    open fun findFeatures(propertyValue: String): List<Feature>? {
        return emptyList()
    }

    open fun getLayerName(source: String): String {
        return source
    }

    open fun markFeatureAsSelected(point: LatLng, layer: String? = null): Feature? { return null }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(
        style: Style,
        onMissingPermission: (PermissionsManager?) -> Unit
    ) {
        map?.locationComponent?.apply {
            if (PermissionsManager.areLocationPermissionsGranted(mapView.context)) {
                activateLocationComponent(
                    LocationComponentActivationOptions.builder(
                        mapView.context,
                        style
                    ).build()
                )
                isLocationComponentEnabled = true
            } else {
                permissionsManager = PermissionsManager(object : PermissionsListener {
                    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {}

                    override fun onPermissionResult(granted: Boolean) {
                        if (granted) {
                            enableLocationComponent(style, onMissingPermission)
                        }
                    }
                })
                onMissingPermission(permissionsManager)
            }
        }
    }
}
