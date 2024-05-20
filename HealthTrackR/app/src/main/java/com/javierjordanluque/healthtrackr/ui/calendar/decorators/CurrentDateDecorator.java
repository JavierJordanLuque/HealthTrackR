package com.javierjordanluque.healthtrackr.ui.calendar.decorators;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.javierjordanluque.healthtrackr.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Objects;

public class CurrentDateDecorator implements DayViewDecorator {
    private final Context context;

    public CurrentDateDecorator(Context context) {
        this.context = context;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return CalendarDay.today().equals(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.calendar_current_date_selector)));
    }
}

