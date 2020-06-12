package org.dhis2.uicomponents.map.model

import java.util.Date
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.ProgramStage

data class EventUiComponentModel(
    val stage: ProgramStage,
    val event: Event,
    val lastUpdated: Date?
)
