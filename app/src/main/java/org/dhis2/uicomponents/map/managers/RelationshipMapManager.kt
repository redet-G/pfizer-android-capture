package org.dhis2.uicomponents.map.managers

import androidx.appcompat.content.res.AppCompatResources
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import org.dhis2.R
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapEventToFeatureCollection
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapTeisToFeatureCollection
import org.dhis2.uicomponents.map.layer.LayerType
import org.dhis2.usescases.events.EXTRA_EVENT_UID
import org.hisp.dhis.android.core.common.FeatureType

class RelationshipMapManager : MapManager() {

    companion object {
        const val RELATIONSHIP_ICON = "RELATIONSHIP_ICON"
        const val RELATIONSHIP_ARROW = "RELATIONSHIP_ARROW"
        const val RELATIONSHIP_ARROW_BIDIRECTIONAL = "RELATIONSHIP_ARROW_BIDIRECTIONAL"
    }

    private lateinit var boundingBox: BoundingBox
    private lateinit var featureCollections: Map<String, FeatureCollection>

    fun update(
        featureCollections: Map<String, FeatureCollection>,
        boundingBox: BoundingBox,
        featureType: FeatureType
    ) {
        this.featureCollections = featureCollections
        this.featureType = featureType
        this.boundingBox = boundingBox
        if (isMapReady()) {
            when {
                mapLayerManager.mapLayers.isNotEmpty() -> updateStyleSources()
                else -> loadDataForStyle()
            }
        }
    }

    override fun loadDataForStyle() {
        style?.addImage(
            RELATIONSHIP_ARROW,
            BitmapUtils.getBitmapFromDrawable(
                AppCompatResources.getDrawable(
                    mapView.context,
                    R.drawable.ic_arrowhead
                )
            )!!,
            true
        )
        style?.addImage(
            RELATIONSHIP_ICON,
            BitmapUtils.getBitmapFromDrawable(
                AppCompatResources.getDrawable(
                    mapView.context,
                    R.drawable.map_marker
                )
            )!!,
            true
        )
        style?.addImage(
            RELATIONSHIP_ARROW_BIDIRECTIONAL,
            BitmapUtils.getBitmapFromDrawable(
                AppCompatResources.getDrawable(
                    mapView.context,
                    R.drawable.ic_arrowhead_bidirectional
                )
            )!!,
            true
        )
        setSource()
        setLayer()
    }

    private fun updateStyleSources() {
        setSource()
        mapLayerManager.updateLayers(LayerType.RELATIONSHIP_LAYER, featureCollections.keys.toList())
    }

    override fun setSource() {
        featureCollections.keys.forEach {
            style?.getSourceAs<GeoJsonSource>(it)?.setGeoJson(featureCollections[it])
                ?: style?.addSource(GeoJsonSource(it, featureCollections[it]))
        }
        initCameraPosition(boundingBox)
    }

    override fun setLayer() {
        mapLayerManager.initMap(map)
            .withFeatureType(featureType)
            .addLayers(LayerType.RELATIONSHIP_LAYER, featureCollections.keys.toList(), true)
            .addLayer(LayerType.SATELLITE_LAYER)
    }

    override fun findFeature(
        source: String,
        propertyName: String,
        propertyValue: String
    ): Feature? {
        return featureCollections[source]?.features()?.firstOrNull {
            it.getStringProperty(propertyName) == propertyValue
        }
    }

    override fun findFeature(propertyValue: String): Feature? {
        val mainProperties = arrayListOf(
            MapTeisToFeatureCollection.TEI_UID,
            MapTeisToFeatureCollection.ENROLLMENT_UID,
            MapRelationshipsToFeatureCollection.RELATIONSHIP_UID,
            MapEventToFeatureCollection.EVENT
        )
        var featureToReturn:Feature? = null
        for (source in featureCollections.keys){
            for(propertyLabel in mainProperties){
                val feature = findFeature(source, propertyLabel, propertyValue)
                if(feature!=null){
                    featureToReturn = feature
                    mapLayerManager.getLayer(source,true)?.setSelectedItem(featureToReturn)
                    break
                }
                if(featureToReturn!=null){
                    break
                }
            }
        }
        return featureToReturn
    }
}
