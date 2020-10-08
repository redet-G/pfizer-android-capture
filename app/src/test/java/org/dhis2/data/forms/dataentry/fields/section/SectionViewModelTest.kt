package org.dhis2.data.forms.dataentry.fields.section

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SectionViewModelTest {

    @Test
    fun shouldShowDescriptionIconWhenTitleIsEllipsizedAndDescriptionIsNull() {
        val sectionViewModel = givenSectionViewModelWithNullDescription()
        assertTrue(sectionViewModel.hasToShowDescriptionIcon(true))
    }

    @Test
    fun shouldShowDescriptionIconWhenTitleIsEllipsizedAndDescriptionIsEmpty() {
        val sectionViewModel = givenSectionViewModelWithEmptyDescription()
        assertTrue(sectionViewModel.hasToShowDescriptionIcon(true))
    }

    @Test
    fun shouldShowDescriptionIconWhenTitleIsEllipsizedAndDHasDescription() {
        val sectionViewModel = givenSectionViewModelWithDescription()
        assertTrue(sectionViewModel.hasToShowDescriptionIcon(true))
    }

    @Test
    fun shouldNotShowDescriptionIconWhenTitleIsNotEllipsizedAndDescriptionIsNull() {
        val sectionViewModel = givenSectionViewModelWithNullDescription()
        assertFalse(sectionViewModel.hasToShowDescriptionIcon(false))
    }

    @Test
    fun shouldNotShowDescriptionIconWhenTitleIsNotEllipsizedAndDescriptionIsEmpty() {
        val sectionViewModel = givenSectionViewModelWithEmptyDescription()
        assertFalse(sectionViewModel.hasToShowDescriptionIcon(false))
    }

    @Test
    fun shouldShowDescriptionIconWhenTitleIsNotEllipsizedAndHasDescription() {
        val sectionViewModel = givenSectionViewModelWithDescription()
        assertTrue(sectionViewModel.hasToShowDescriptionIcon(false))
    }

    private fun givenSectionViewModelWithNullDescription() = SectionViewModel.create(
        "",
        "",
        null,
        false,
        1,
        1,
        ""
    )

    private fun givenSectionViewModelWithEmptyDescription() = SectionViewModel.create(
        "",
        "",
        "",
        false,
        1,
        1,
        ""
    )

    private fun givenSectionViewModelWithDescription() = SectionViewModel.create(
        "",
        "",
        "This is a description",
        false,
        1,
        1,
        ""
    )
}
