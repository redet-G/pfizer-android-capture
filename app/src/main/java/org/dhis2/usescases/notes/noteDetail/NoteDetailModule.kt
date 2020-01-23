package org.dhis2.usescases.notes.noteDetail

import dagger.Module
import dagger.Provides
import org.dhis2.data.dagger.PerActivity
import org.hisp.dhis.android.core.D2

@Module
class NoteDetailModule(val view: NoteDetailView, private val noteId: String? = null) {

    @Provides
    @PerActivity
    fun providesRepository(d2: D2): NoteDetailRepository = NoteDetailRepositoryImpl(d2)

    @Provides
    @PerActivity
    fun providesPresenter(
        repository: NoteDetailRepository): NoteDetailPresenter {
        return NoteDetailPresenter(view, noteId, repository)
    }
}