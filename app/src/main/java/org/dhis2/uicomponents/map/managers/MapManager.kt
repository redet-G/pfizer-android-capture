package org.dhis2.uicomponents.map.managers

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import org.dhis2.uicomponents.map.camera.initCameraToViewAllElements
import org.dhis2.uicomponents.map.carousel.CarouselAdapter
import org.dhis2.uicomponents.map.layer.MapLayerManager

abstract class MapManager(val mapView: MapView) {

    lateinit var map: MapboxMap
    lateinit var mapLayerManager: MapLayerManager
    var markerViewManager: MarkerViewManager? = null
    var symbolManager: SymbolManager? = null
    var onMapClickListener: MapboxMap.OnMapClickListener? = null
    var carouselAdapter: CarouselAdapter? = null
    val style: Style?
        get() = map.style

    fun init() {
        mapView.getMapAsync {
            this.map = it
            map.setStyle(Style.MAPBOX_STREETS) { loadDataForStyle() }
            onMapClickListener?.let { mapClickListener ->
                map.addOnMapClickListener(mapClickListener)
            }
            markerViewManager = MarkerViewManager(mapView, map)
        }
        mapLayerManager = MapLayerManager().apply {
            styleChangeCallback = { loadDataForStyle() }
        }
    }

    abstract fun loadDataForStyle()
    abstract fun setSource()
    abstract fun setLayer()

    fun setSymbolManager(featureCollection: FeatureCollection) {
        symbolManager = SymbolManager(
            mapView, map, style!!, null,
            GeoJsonOptions().withTolerance(0.4f)
        ).apply {
            iconAllowOverlap = true
            textAllowOverlap = true
            iconIgnorePlacement = true
            textIgnorePlacement = true
            symbolPlacement = "line-center"
            create(featureCollection)
        }
    }

    fun initCameraPosition(
        boundingBox: BoundingBox
    ) {
        val bounds = LatLngBounds.Builder()
            .include(pointToLatLn(boundingBox.northeast()))
            .include(pointToLatLn(boundingBox.southwest()))
            .build()
        map.initCameraToViewAllElements(
            mapView.context, bounds
        )
    }

    fun pointToLatLn(point: Point): LatLng {
        return LatLng(point.latitude(), point.longitude())
    }

    fun findFeatureFor(featureUidProperty: String): Feature? {
        if (!isMapReady()) return null
        return mapLayerManager.getLayers().mapNotNull { mapLayer ->
            mapLayer.findFeatureWithUid(featureUidProperty)
        }.firstOrNull()
    }

    fun isMapReady() = ::map.isInitialized && style?.isFullyLoaded ?: false

    fun onStart() {
        mapView.onStart()
    }

    fun onResume() {
        mapView.onResume()
    }

    fun onPause() {
        mapView.onPause()
    }

    fun onDestroy() {
        markerViewManager?.onDestroy()
        symbolManager?.onDestroy()
    }

    abstract fun findFeature(source: String, propertyName: String, propertyValue: String): Feature?
    abstract fun findFeature(propertyValue: String): Feature?
}
