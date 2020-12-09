package org.dhis2.data.forms.dataentry.fields;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;

import autovalue.shaded.org.checkerframework$.checker.nullness.qual.$NonNull;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;

public interface FieldViewModelFactory {

    @NonNull
    FieldViewModel create(@NonNull String id,
                          @NonNull String label,
                          @NonNull ValueType valueType,
                          @NonNull Boolean mandatory,
                          @Nullable String optionSet,
                          @Nullable String value,
                          @Nullable String programStageSection,
                          @Nullable Boolean AllowFutureDate,
                          @NonNull Boolean editable,
                          @Nullable ProgramStageSectionRenderingType renderingType,
                          @Nullable String description,
                          @Nullable ValueTypeDeviceRendering fieldRendering,
                          @Nullable Integer optionCount,
                          @NonNull ObjectStyle objectStyle,
                          @Nullable String fieldMask,
                          FlowableProcessor<RowAction> processor,
                          List<Option> options);

    @Nullable
    FieldViewModel createForAttribute(@$NonNull TrackedEntityAttribute trackedEntityAttribute,
                                      @Nullable ProgramTrackedEntityAttribute programTrackedEntityAttribute,
                                      @Nullable String value,
                                      boolean editable);

    @NonNull
    FieldViewModel createSingleSection(String singleSectionName);

    @NonNull
    FieldViewModel createSection(String sectionUid, String sectionName, String description,
                                 boolean isOpen, int totalFields, int completedFields, String rendering);

    @NonNull
    FieldViewModel createClosingSection();

    @NonNull
    Flowable<String> sectionProcessor();

    @NonNull
    Flowable<RowAction> fieldProcessor();
}
