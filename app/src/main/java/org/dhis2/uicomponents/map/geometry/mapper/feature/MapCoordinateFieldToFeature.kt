package org.dhis2.uicomponents.map.geometry.mapper.feature

import com.mapbox.geojson.Feature
import org.dhis2.data.dhislogic.CoordinateAttributeInfo
import org.dhis2.data.dhislogic.CoordinateDataElementInfo
import org.dhis2.uicomponents.map.geometry.mapper.MapGeometryToFeature
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection.Companion.EVENT
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection.Companion.FIELD_NAME
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection.Companion.STAGE
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection.Companion.TEI

class MapCoordinateFieldToFeature(private val mapGeometryToFeature: MapGeometryToFeature) {

    fun map(coordinateDataElementInfo: CoordinateDataElementInfo): Feature? {
        return mapGeometryToFeature.map(
            coordinateDataElementInfo.geometry,
            hashMapOf(
                FIELD_NAME to coordinateDataElementInfo.dataElement.displayFormName()!!,
                EVENT to coordinateDataElementInfo.event.uid()!!,
                STAGE to coordinateDataElementInfo.stage.displayName()!!
            ).apply {
                coordinateDataElementInfo.enrollment?.let { enrollment ->
                    put(
                        TEI,
                        enrollment.trackedEntityInstance()!!
                    )
                }
            }
        )
    }

    fun map(coordinateAttributeInfo: CoordinateAttributeInfo): Feature? {
        return mapGeometryToFeature.map(
            coordinateAttributeInfo.geometry,
            hashMapOf(
                FIELD_NAME to coordinateAttributeInfo.attribute.displayFormName()!!,
                TEI to coordinateAttributeInfo.tei.uid()!!
            )
        )
    }
}
