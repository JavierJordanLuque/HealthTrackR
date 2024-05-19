package com.javierjordanluque.healthtrackr.ui.calendar.decorators;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.javierjordanluque.healthtrackr.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public class TreatmentStartDateDecorator implements DayViewDecorator {
    private final Context context;
    private final HashSet<CalendarDay> dates;

    public TreatmentStartDateDecorator(Context context, Collection<CalendarDay> dates) {
        this.context = context;
        this.dates = new HashSet<>(dates);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.calendar_treatment_start_selector)));
    }
}
