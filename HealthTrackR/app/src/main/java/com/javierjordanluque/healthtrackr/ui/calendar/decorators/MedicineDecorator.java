package com.javierjordanluque.healthtrackr.ui.calendar.decorators;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.javierjordanluque.healthtrackr.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.Collection;
import java.util.HashSet;

public class MedicineDecorator implements DayViewDecorator {
    private final Context context;
    private final HashSet<CalendarDay> dates;

    public MedicineDecorator(Context context, Collection<CalendarDay> dates) {
        this.context = context;
        this.dates = new HashSet<>(dates);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new DotSpan(10, ContextCompat.getColor(context, R.color.calendar_medicationLegend)));
    }
}
