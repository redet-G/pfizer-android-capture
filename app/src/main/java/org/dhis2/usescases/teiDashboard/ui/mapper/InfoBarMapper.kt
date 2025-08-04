package org.dhis2.usescases.teiDashboard.ui.mapper

import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.SyncProblem
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.teiDashboard.DashboardEnrollmentModel
import org.dhis2.usescases.teiDashboard.ui.model.InfoBarType
import org.dhis2.usescases.teiDashboard.ui.model.InfoBarUiModel
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItemColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.ethcaledar.toEthiopianDateString

class InfoBarMapper(
    val resourceManager: ResourceManager,
) {
    fun map(
        infoBarType: InfoBarType,
        item: DashboardEnrollmentModel,
        actionCallback: () -> Unit,
        showInfoBar: Boolean = false,
    ): InfoBarUiModel {
        android.util.Log.d("InfoBarMapper", "map called with showInfoBar = $item, infoBarType = $item")


        with(item.currentEnrollment) {
            return InfoBarUiModel(
                type = infoBarType,
                currentEnrollment = this,
                text = getText(infoBarType, item, this.aggregatedSyncState()!!, this.status()!!),
                textColor = getTextColor(
                    infoBarType,
                    this.aggregatedSyncState()!!,
                    this.status()!!,
                ),
                icon = { GetIcon(infoBarType, this.aggregatedSyncState()!!, this.status()!!) },
                actionText = getActionText(infoBarType, this.aggregatedSyncState()!!),
                onActionClick = actionCallback,
                backgroundColor = getBackgroundColor(
                    infoBarType,
                    this.aggregatedSyncState()!!,
                    this.status()!!,
                ),
                showInfoBar = showInfoBar,
            )
        }
    }


    private fun getText(
        infoBarType: InfoBarType,
        item: DashboardEnrollmentModel,
        state: State,
        enrollmentStatus: EnrollmentStatus,
    ): String {
        return when (infoBarType) {
            InfoBarType.SYNC -> {
                if (state == State.TO_UPDATE) {
                    resourceManager.getString(R.string.not_synced)
                } else if (state == State.WARNING) {
                    resourceManager.getString(R.string.sync_warning)
                } else {
                    resourceManager.getString(R.string.sync_error)
                }
            }

            InfoBarType.FOLLOW_UP -> resourceManager.getString(R.string.marked_follow_up)

            InfoBarType.ENROLLMENT_STATUS -> {
                val status = enrollmentStatus.name
                val enrollmentDate =toEthiopianDateString(item.currentEnrollment.enrollmentDate())
                val incidentDate = toEthiopianDateString(item.currentEnrollment.incidentDate())
                android.util.Log.d("EthiopianDateDebug", "EnrollmentDatess: $enrollmentDate, IncidentDate: $incidentDate")
                "Status: $status, Enrollment Date: $enrollmentDate, Incident Date: $incidentDate"
            }
        }
    }

    @Composable
    private fun GetIcon(
        infoBarType: InfoBarType,
        enrollmentState: State,
        enrollmentStatus: EnrollmentStatus,
    ) {
        when (infoBarType) {
            InfoBarType.SYNC -> {
                if (enrollmentState == State.TO_UPDATE) {
                    Icon(Icons.Outlined.Sync, contentDescription = "not synced", tint = TextColor.OnSurfaceLight)
                } else if (enrollmentState == State.WARNING) {
                    Icon(Icons.Outlined.SyncProblem, contentDescription = "sync warning", tint = AdditionalInfoItemColor.WARNING.color)
                } else {
                    Icon(Icons.Outlined.SyncProblem, contentDescription = "sync error", tint = AdditionalInfoItemColor.ERROR.color)
                }
            }

            InfoBarType.FOLLOW_UP -> {
                Icon(Icons.Filled.Flag, contentDescription = "follow up", tint = Color(0xFFFAAD14))
            }

            InfoBarType.ENROLLMENT_STATUS -> {
                if (enrollmentStatus == EnrollmentStatus.COMPLETED) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "enrollment complete", tint = AdditionalInfoItemColor.SUCCESS.color)
                } else {
                    Icon(Icons.Filled.Block, contentDescription = "enrollment cancelled", tint = TextColor.OnSurfaceLight)
                }
            }
        }
    }

    private fun getActionText(infoBarType: InfoBarType, state: State): String? {
        return when (infoBarType) {
            InfoBarType.SYNC -> {
                if (state == State.TO_UPDATE) {
                    resourceManager.getString(R.string.sync)
                } else {
                    resourceManager.getString(R.string.sync_retry)
                }
            }

            InfoBarType.FOLLOW_UP -> resourceManager.getString(R.string.remove)
            InfoBarType.ENROLLMENT_STATUS -> null
        }
    }

    private fun getTextColor(infoBarType: InfoBarType, state: State, enrollmentStatus: EnrollmentStatus): Color {
        return when (infoBarType) {
            InfoBarType.SYNC -> {
                if (state == State.TO_UPDATE) {
                    TextColor.OnSurfaceLight
                } else if (state == State.WARNING) {
                    AdditionalInfoItemColor.WARNING.color
                } else {
                    AdditionalInfoItemColor.ERROR.color
                }
            }

            InfoBarType.FOLLOW_UP -> TextColor.OnSurfaceLight
            InfoBarType.ENROLLMENT_STATUS -> {
                if (enrollmentStatus == EnrollmentStatus.COMPLETED) {
                    AdditionalInfoItemColor.SUCCESS.color
                } else {
                    TextColor.OnSurfaceLight
                }
            }
        }
    }

    private fun getBackgroundColor(infoBarType: InfoBarType, state: State, enrollmentStatus: EnrollmentStatus): Color {
        return when (infoBarType) {
            InfoBarType.SYNC -> {
                if (state == State.TO_UPDATE) {
                    Color(0xFFEFF6FA)
                } else if (state == State.WARNING) {
                    AdditionalInfoItemColor.WARNING.color.copy(alpha = 0.1f)
                } else {
                    AdditionalInfoItemColor.ERROR.color.copy(alpha = 0.1f)
                }
            }

            InfoBarType.FOLLOW_UP -> Color(0xFFEFF6FA)
            InfoBarType.ENROLLMENT_STATUS -> {
                if (enrollmentStatus == EnrollmentStatus.COMPLETED) {
                    AdditionalInfoItemColor.SUCCESS.color.copy(alpha = 0.1f)
                } else {
                    Color(0xFFEFF6FA)
                }
            }
        }
    }
}
