package org.dhis2.uicomponents

import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import org.dhis2.R
import org.dhis2.utils.ColorUtils
import org.dhis2.utils.maps.MapLayerManager
import org.dhis2.utils.maps.MarkerUtils.getMarker
import org.dhis2.utils.maps.initDefaultCamera
import org.hisp.dhis.android.core.common.FeatureType
import java.util.HashMap

open class MapManager(val mapView: MapView) {

    var map: MapboxMap? = null
    var changingStyle = false
    var featureType: FeatureType? = null
    var mapStyle: MapStyle? = null
    var markerViewManager: MarkerViewManager? = null
    protected var symbolManager: SymbolManager? = null
    var onMapClickListener: MapboxMap.OnMapClickListener? = null


    /*fun setStyle(
        teiFeatureCollections: HashMap<String, FeatureCollection>,
        boundingBox: BoundingBox
    ) {
        if (map == null) {
            mapView.getMapAsync {
                map = it
                map?.setStyle(
                    Style.MAPBOX_STREETS
                ) { style: Style ->
                    loadDataForStyle(
                        style,
                        teiFeatureCollections,
                        boundingBox
                    )
                }
            }
        } else if (changingStyle) {
            map?.setStyle(
                Style.MAPBOX_STREETS
            ) { style: Style ->
                loadDataForStyle(
                    style,
                    teiFeatureCollections,
                    boundingBox
                )
            }
        } else {
            (map?.style?.getSource("teis") as GeoJsonSource?)?.setGeoJson(teiFeatureCollections["TEI"])
            (map?.style?.getSource("enrollments") as GeoJsonSource?)?.setGeoJson(
                teiFeatureCollections["ENROLLMENT"]
            )
            if (boundingBox.north() != 0.0 && boundingBox.east() != 0.0 && boundingBox.south() != 0.0 && boundingBox.west() != 0.0) {
                val bounds = LatLngBounds.from(
                    boundingBox.north(),
                    boundingBox.east(),
                    boundingBox.south(),
                    boundingBox.west()
                )
                map?.initDefaultCamera(mapView.context, bounds)
            } else {
                map?.easeCamera(CameraUpdateFactory.zoomTo(map!!.minZoomLevel))
            }
        }
    }

    fun setStyle(
        featureCollection: FeatureCollection,
        boundingBox: BoundingBox
    ) {
        if (map == null) {
            mapView.getMapAsync {
                map = it
                map?.setStyle(
                    Style.MAPBOX_STREETS
                ) { style: Style ->
                    onMapClickListener?.let { map?.addOnMapClickListener(it) }

                    style.addImage(
                        "ICON_ID",
                        BitmapFactory.decodeResource(
                            mapView.resources,
                            R.drawable.mapbox_marker_icon_default
                        )
                    )
                    setSource(style, featureCollection)
                    setEventLayer(style)

                    initCameraPosition(map!!, boundingBox)

                    markerViewManager = MarkerViewManager(mapView, map)
                    symbolManager = SymbolManager(
                        mapView, map!!, style, null,
                        GeoJsonOptions().withTolerance(0.4f)
                    )

                    symbolManager?.iconAllowOverlap = true
                    symbolManager?.textAllowOverlap = true
                    symbolManager?.create(featureCollection)
                }
            }
        } else {
            (map!!.style!!.getSource("events") as GeoJsonSource?)!!.setGeoJson(featureCollection)
            initCameraPosition(map!!, boundingBox)
        }
    }

    private fun initCameraPosition(
        map: MapboxMap,
        bbox: BoundingBox
    ) {
        val bounds =
            LatLngBounds.from(bbox.north(), bbox.east(), bbox.south(), bbox.west())
        map.initDefaultCamera(mapView.context, bounds)
    }

    private fun loadDataForStyle(
        style: Style,
        teiFeatureCollection: HashMap<String, FeatureCollection>,
        boundingBox: BoundingBox
    ) {
        MapLayerManager.run {
            when {
                !changingStyle -> {
                    init(style, "teis", featureType ?: FeatureType.NONE)
                    instance().setEnrollmentLayerData(
                        ColorUtils.getColorFrom(
                            mapStyle?.programColor,
                            ColorUtils.getPrimaryColor(
                                mapView.context,
                                ColorUtils.ColorType.PRIMARY
                            )
                        ),
                        ColorUtils.getPrimaryColor(
                            mapView.context,
                            ColorUtils.ColorType.PRIMARY_DARK
                        ),
                        featureType ?: FeatureType.NONE
                    )
                    *//*instance().showEnrollmentLayer().observe(this,
                        Observer { show: Boolean -> if (show) presenter.getEnrollmentMapData() }
                    )*//*
                }
                else -> instance().updateStyle(style)
            }
        }
        onMapClickListener?.let { map?.addOnMapClickListener(it) }
        style.addImage(
            "ICON_ID",
            getMarker(
                mapView.context,
                mapStyle?.teiSymbolIcon ?: ContextCompat.getDrawable(
                    mapView.context,
                    R.drawable.mapbox_marker_icon_default
                )!!,
                mapStyle?.teiColor ?: ColorUtils.getPrimaryColor(
                    mapView.context,
                    ColorUtils.ColorType.PRIMARY
                )
            )
        )
        style.addImage(
            "ICON_ENROLLMENT_ID",
            getMarker(mapView.context, mapStyle?.enrollmentSymbolIcon!!, mapStyle?.enrollmentColor!!)
        )
        setSource(style, teiFeatureCollection)
        setLayer(style)
        val bounds = LatLngBounds.from(
            boundingBox.north(),
            boundingBox.east(),
            boundingBox.south(),
            boundingBox.west()
        )
        map?.initDefaultCamera(mapView.context, bounds)
        if(markerViewManager == null) {
            markerViewManager = MarkerViewManager(mapView, map)
        }
        if (symbolManager == null) {
            symbolManager = SymbolManager(
                mapView, map!!, style, null,
                GeoJsonOptions().withTolerance(0.4f)
            ).apply {
                iconAllowOverlap = true
                textAllowOverlap = true
                iconIgnorePlacement = true
                textIgnorePlacement = true
                symbolPlacement = "line-center"
                teiFeatureCollection["TEI"]?.let { it -> create(it) }
            }

        }
    }
    private fun setSource(
        style: Style,
        featCollectionMap: HashMap<String, FeatureCollection>
    ) {
        style.addSource(GeoJsonSource("teis", featCollectionMap["TEI"]))
        style.addSource(GeoJsonSource("enrollments", featCollectionMap["ENROLLMENT"]))
    }

    private fun setSource(
        style: Style,
        featureCollection: FeatureCollection
    ) {
        style.addSource(GeoJsonSource("events", featureCollection))
    }

    private fun setLayer(style: Style) {
        val symbolLayer = SymbolLayer("POINT_LAYER", "teis").withProperties(
            PropertyFactory.iconImage(
                Expression.get(
                    "teiImage"
                )
            ),
            PropertyFactory.iconOffset(
                arrayOf(
                    0f,
                    -25f
                )
            ),
            PropertyFactory.iconAllowOverlap(true),
            PropertyFactory.textAllowOverlap(true)
        )
        symbolLayer.setFilter(
            Expression.eq(
                Expression.literal(
                    "\$type"
                ), Expression.literal("Point")
            )
        )
        style.addLayer(symbolLayer)
        if (featureType != FeatureType.POINT) {
            style.addLayerBelow(
                FillLayer("POLYGON_LAYER", "teis")
                    .withProperties(
                        PropertyFactory.fillColor(
                            ColorUtils.getPrimaryColorWithAlpha(
                                mapView.context,
                                ColorUtils.ColorType.PRIMARY_LIGHT,
                                150f
                            )
                        )
                    )
                    .withFilter(
                        Expression.eq(
                            Expression.literal(
                                "\$type"
                            ), Expression.literal("Polygon")
                        )
                    ),
                "POINT_LAYER"
            )
            style.addLayerAbove(
                LineLayer("POLYGON_BORDER_LAYER", "teis")
                    .withProperties(
                        PropertyFactory.lineColor(
                            ColorUtils.getPrimaryColor(
                                mapView.context,
                                ColorUtils.ColorType.PRIMARY_DARK
                            )
                        ),
                        PropertyFactory.lineWidth(2f)
                    )
                    .withFilter(
                        Expression.eq(
                            Expression.literal(
                                "\$type"
                            ), Expression.literal("Polygon")
                        )
                    ),
                "POLYGON_LAYER"
            )
        }
    }

    private fun setEventLayer(style: Style) {
        val symbolLayer = SymbolLayer("POINT_LAYER", "events")
            .withProperties(
                PropertyFactory.iconImage("ICON_ID"),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconOffset(
                    arrayOf(
                        0f,
                        -9f
                    )
                )
            )
        symbolLayer.minZoom = 0f
        style.addLayer(symbolLayer)
        if (featureType != FeatureType.POINT) style.addLayerBelow(
            FillLayer("POLYGON_LAYER", "events").withProperties(
                PropertyFactory.fillColor(
                    ColorUtils.getPrimaryColorWithAlpha(
                        mapView.context,
                        ColorUtils.ColorType.PRIMARY_LIGHT,
                        150f
                    )
                )
            ), "settlement-label"
        )
    }*/

    fun onDestroy() {
        markerViewManager?.onDestroy()
        symbolManager?.onDestroy()
    }
}