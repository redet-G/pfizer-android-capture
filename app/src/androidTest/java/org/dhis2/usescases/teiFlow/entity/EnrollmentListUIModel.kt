package org.dhis2.usescases.teiFlow.entity

data class EnrollmentListUIModel(
    val program: String,
    val orgUnit: String,
    val pastEnrollmentDate: String,
    val currentEnrollmentDate: String
)