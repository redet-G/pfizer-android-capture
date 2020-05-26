package org.dhis2.uicomponents.map

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import org.dhis2.uicomponents.map.layer.LayerType
import org.dhis2.uicomponents.map.layer.MapLayerManager
import org.hisp.dhis.android.core.common.FeatureType
import java.util.HashMap

class TeiMapManager(
    val mapStyle: MapStyle
) : MapManager() {

    private lateinit var teiFeatureCollections: HashMap<String, FeatureCollection>

    companion object {
        const val TEIS_SOURCE_ID = "TEIS_SOURCE_ID"
        const val ENROLLMENT_SOURCE_ID = "ENROLLMENT_SOURCE_ID"
        const val TEI = "TEI"
        const val ENROLLMENT = "ENROLLMENT"
    }

    fun update(
        teiFeatureCollections: HashMap<String, FeatureCollection>,
        boundingBox: BoundingBox,
        featureType: FeatureType
    ) {
        this.featureType = featureType
        this.teiFeatureCollections = teiFeatureCollections
        (style?.getSource(TEIS_SOURCE_ID) as GeoJsonSource?)
            ?.setGeoJson(teiFeatureCollections[TEI])
            .also {
                (style?.getSource(ENROLLMENT_SOURCE_ID) as GeoJsonSource?)
                    ?.setGeoJson(teiFeatureCollections[ENROLLMENT])
            } ?: run {
            loadDataForStyle()
        }
        initCameraPosition(boundingBox)
    }

    override fun loadDataForStyle() {
        style?.apply {
            addImage(
                MapLayerManager.TEI_ICON_ID,
                TeiMarkers.getMarker(
                    mapView.context,
                    mapStyle.teiSymbolIcon,
                    mapStyle.teiColor
                )
            )
            addImage(
                MapLayerManager.ENROLLMENT_ICON_ID,
                TeiMarkers.getMarker(
                    mapView.context,
                    mapStyle.enrollmentSymbolIcon,
                    mapStyle.enrollmentColor
                )
            )
        }
        setSource()
        setLayer()
        teiFeatureCollections[TEI]?.let { setSymbolManager(it) }
    }

    override fun setSource() {
        style?.addSource(GeoJsonSource(TEIS_SOURCE_ID, teiFeatureCollections[TEI]))
        style?.addSource(GeoJsonSource(ENROLLMENT_SOURCE_ID, teiFeatureCollections[ENROLLMENT]))
    }

    override fun setLayer() {
        mapLayerManager.apply {
            init(map, featureType, mapStyle)
            handleLayer(LayerType.TEI_LAYER, true)
        }
    }
}
