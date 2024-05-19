package com.javierjordanluque.healthtrackr.ui.treatments.calendar;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.MedicalAppointment;
import com.javierjordanluque.healthtrackr.models.Medicine;
import com.javierjordanluque.healthtrackr.models.Treatment;
import com.javierjordanluque.healthtrackr.ui.MainActivity;
import com.javierjordanluque.healthtrackr.ui.OnToolbarChangeListener;
import com.javierjordanluque.healthtrackr.ui.calendar.decorators.MedicalAppointmentDecorator;
import com.javierjordanluque.healthtrackr.ui.calendar.decorators.MedicineDecorator;
import com.javierjordanluque.healthtrackr.ui.calendar.decorators.TreatmentEndDateDecorator;
import com.javierjordanluque.healthtrackr.ui.calendar.decorators.TreatmentStartDateDecorator;
import com.javierjordanluque.healthtrackr.ui.treatments.medicines.MedicineFragment;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TreatmentCalendarFragment extends Fragment {
    private OnToolbarChangeListener listener;
    private Treatment treatment;
    private ImageView imageViewStatus;
    private TextView textViewStatus;
    private MaterialCalendarView calendarView;
    private LocalDate selectedDate;
    private TextView textViewSelectedDate;
    private TextView textViewNoElements;
    private LinearLayout linearLayoutAppointments;
    private LinearLayout linearLayoutMedicines;
    private Boolean pendingAppointmentsFilter;
    private Boolean passedAppointmentsFilter;

    public TreatmentCalendarFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_treatment_calendar, container, false);

        imageViewStatus = fragmentView.findViewById(R.id.imageViewStatus);
        textViewStatus = fragmentView.findViewById(R.id.textViewStatus);

        calendarView = fragmentView.findViewById(R.id.calendarView);
        setCalendarView();
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            selectedDate = LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
            showSelectedDateSchedule();
        });

        textViewSelectedDate = fragmentView.findViewById(R.id.textViewSelectedDate);
        textViewNoElements = fragmentView.findViewById(R.id.textViewNoElements);
        linearLayoutAppointments = fragmentView.findViewById(R.id.linearLayoutAppointments);
        linearLayoutMedicines = fragmentView.findViewById(R.id.linearLayoutMedicines);

        FloatingActionButton buttonAddMedicalAppointment = fragmentView.findViewById(R.id.buttonAddMedicalAppointment);
        buttonAddMedicalAppointment.setOnClickListener(view -> {
            if (treatment.isFinished()) {
                ((MainActivity) requireActivity()).showTreatmentFinishedDialog();
            } else {
                /*
                Intent intent = new Intent(requireActivity(), AddMedicalAppointmentActivity.class);
                ((MainActivity) requireActivity()).fragmentLauncher.launch(intent);
                 */
            }
        });

        ExtendedFloatingActionButton buttonFilterCalendar = fragmentView.findViewById(R.id.buttonFilterCalendar);
        buttonFilterCalendar.setOnClickListener(view -> {
            View popupView = getLayoutInflater().inflate(R.layout.filter_appointments, null);
            PopupWindow popupWindow = new PopupWindow(popupView, buttonFilterCalendar.getWidth(), LinearLayout.LayoutParams.WRAP_CONTENT, true);

            int[] location = new int[2];
            buttonFilterCalendar.getLocationInWindow(location);

            popupWindow.showAtLocation(buttonFilterCalendar, Gravity.START | Gravity.TOP, location[0], location[1] + buttonFilterCalendar.getHeight());

            CheckBox checkBoxPassedAppointments = popupView.findViewById(R.id.checkBoxPassedAppointments);
            if (passedAppointmentsFilter != null) {
                checkBoxPassedAppointments.setChecked(passedAppointmentsFilter);
            } else {
                checkBoxPassedAppointments.setChecked(true);
            }

            CheckBox checkBoxPendingAppointments = popupView.findViewById(R.id.checkBoxPendingAppointments);
            if (pendingAppointmentsFilter != null) {
                checkBoxPendingAppointments.setChecked(pendingAppointmentsFilter);
            } else {
                checkBoxPendingAppointments.setChecked(true);
            }

            Button buttonFilter = popupView.findViewById(R.id.buttonFilter);
            buttonFilter.setOnClickListener(v -> {

                passedAppointmentsFilter = checkBoxPassedAppointments.isChecked();
                pendingAppointmentsFilter = checkBoxPendingAppointments.isChecked();

                List<MedicalAppointment> filteredAppointments = treatment.filterAppointments(passedAppointmentsFilter, pendingAppointmentsFilter);
                showHighlightedMedicalAppointments(filteredAppointments);

                popupWindow.dismiss();
            });

            Button buttonShowAllFilter = popupView.findViewById(R.id.buttonShowAllFilter);
            buttonShowAllFilter.setOnClickListener(v -> {
                try {
                    resetFilters();

                    List<MedicalAppointment> unfilteredAppointments = treatment.getAppointments(requireActivity());
                    showHighlightedMedicalAppointments(unfilteredAppointments);
                } catch (DBFindException exception) {
                    ExceptionManager.advertiseUI(requireActivity(), exception.getMessage());
                }

                popupWindow.dismiss();
            });
        });

        return fragmentView;
    }

    private void setCalendarView() {
        calendarView.setSelectedDate(org.threeten.bp.LocalDate.now());

        boolean isNightMode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        calendarView.setDateTextAppearance(isNightMode ? R.style.TextAppearance_HealthTrackR_Date_Dark : R.style.TextAppearance_HealthTrackR_Date_Light);
        calendarView.setLeftArrow(isNightMode ? R.drawable.ic_calendar_left_arrow_dark : R.drawable.ic_calendar_left_arrow_light);
        calendarView.setRightArrow(isNightMode ? R.drawable.ic_calendar_right_arrow_dark : R.drawable.ic_calendar_right_arrow_light);
    }

    @Override
    public void onResume() {
        super.onResume();

        treatment = ((MainActivity) requireActivity()).getTreatmentFromBundle(getArguments());
        ((MainActivity) requireActivity()).setTreatmentLayoutStatus(treatment, imageViewStatus, textViewStatus);

        ((MainActivity) requireActivity()).currentFragment = this;
        ((MainActivity) requireActivity()).showBackButton(true);
        if (listener != null)
            listener.onTitleChanged(treatment.getTitle());

        calendarView.state().edit()
                .setMinimumDate(CalendarDay.from(treatment.getStartDate().getYear(), treatment.getStartDate().getMonthValue(), treatment.getStartDate().getDayOfMonth()))
                .commit();
        calendarView.addDecorator(new TreatmentStartDateDecorator(requireActivity(), Collections.singletonList(CalendarDay.from(treatment.getStartDate().getYear(),
                treatment.getStartDate().getMonthValue(), treatment.getStartDate().getDayOfMonth()))));

        if (treatment.getEndDate() != null) {
            calendarView.state().edit()
                    .setMaximumDate(CalendarDay.from(treatment.getEndDate().getYear(), treatment.getEndDate().getMonthValue(), treatment.getEndDate().getDayOfMonth()))
                    .commit();
            calendarView.addDecorator(new TreatmentEndDateDecorator(requireActivity(), Collections.singletonList(CalendarDay.from(treatment.getEndDate().getYear(),
                    treatment.getEndDate().getMonthValue(), treatment.getEndDate().getDayOfMonth()))));
        }

        try {
            resetFilters();
            showHighlightedMedicalAppointments(treatment.getAppointments(requireActivity()));
            showHighlightedMedicinesDates(treatment.getMedicines(requireActivity()));
        } catch (DBFindException exception) {
            ExceptionManager.advertiseUI(requireActivity(), exception.getMessage());
        }

        if (selectedDate == null)
            selectedDate = LocalDate.now();
        showSelectedDateSchedule();
    }

    private void showHighlightedMedicinesDates(List<Medicine> medicines) {
        Collection<CalendarDay> highlightedDates = new ArrayList<>();

        for (Medicine medicine : medicines) {
            ZonedDateTime dosingTime = medicine.getInitialDosingTime();
            Duration frequency = Duration.ofHours(medicine.getDosageFrequencyHours()).plusMinutes(medicine.getDosageFrequencyMinutes());

            LocalDate cntDosingDate = dosingTime.toLocalDate();
            LocalDate endDate = treatment.getEndDate() != null ? treatment.getEndDate().toLocalDate() : LocalDate.now().plusDays(10);
            while (!cntDosingDate.isAfter(endDate)) {
                if (dosingTime.toLocalDate().isEqual(cntDosingDate)) {
                    highlightedDates.add(CalendarDay.from(cntDosingDate.getYear(), cntDosingDate.getMonthValue(), cntDosingDate.getDayOfMonth()));

                    if (!frequency.isZero()) {
                        cntDosingDate = cntDosingDate.plusDays(1);
                        long dosesElapsed = Duration.between(dosingTime, cntDosingDate.atStartOfDay(dosingTime.getZone())).toMinutes() / frequency.toMinutes();
                        dosingTime = dosingTime.plus(frequency.multipliedBy(dosesElapsed + 1));
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        }

        calendarView.addDecorator(new MedicineDecorator(requireActivity(), highlightedDates));
    }

    private void showHighlightedMedicalAppointments(List<MedicalAppointment> appointments) {
        Collection<CalendarDay> highlightedDates = new ArrayList<>();

        for (MedicalAppointment appointment : appointments)
            highlightedDates.add(CalendarDay.from(appointment.getDateTime().getYear(), appointment.getDateTime().getMonthValue(), appointment.getDateTime().getDayOfMonth()));

        calendarView.addDecorator(new MedicalAppointmentDecorator(requireActivity(), highlightedDates));
    }

    private void showSelectedDateSchedule() {
        setTextViewSelectedDate();
        textViewNoElements.setVisibility(View.VISIBLE);
        linearLayoutAppointments.setVisibility(View.GONE);
        linearLayoutMedicines.setVisibility(View.GONE);

        if (!treatment.getStartDate().toLocalDate().isAfter(selectedDate) && (treatment.getEndDate() == null || !treatment.getEndDate().toLocalDate().isBefore(selectedDate))) {
            try {
                setAppointmentsSelectedDate(treatment.getAppointments(requireActivity()));
                setMedicinesSelectedDate(treatment.getMedicines(requireActivity()));
            } catch (DBFindException exception) {
                ExceptionManager.advertiseUI(requireActivity(), exception.getMessage());
            }
        }
    }

    private void setTextViewSelectedDate() {
        LocalDate today = LocalDate.now();

        DateTimeFormatter formatter;
        if (selectedDate.getYear() == today.getYear()) {
            formatter = DateTimeFormatter.ofPattern("d '" + getString(R.string.calendar_of) + "' MMMM");
        } else {
            formatter = DateTimeFormatter.ofPattern("d '" + getString(R.string.calendar_of) + "' MMMM, yyyy");
        }
        String formattedDate = selectedDate.format(formatter);

        if (selectedDate.equals(today)) {
            formattedDate += " " + getString(R.string.calendar_today);
        } else if (selectedDate.equals(today.minusDays(1))) {
            formattedDate += " " + getString(R.string.calendar_yesterday);
        } else if (selectedDate.equals(today.plusDays(1))) {
            formattedDate += " " + getString(R.string.calendar_tomorrow);
        }

        textViewSelectedDate.setText(formattedDate);
    }

    private void setAppointmentsSelectedDate(List<MedicalAppointment> appointments) {
        linearLayoutAppointments.removeAllViews();
        List<MedicalAppointment> appointmentsToShow = new ArrayList<>();

        for (MedicalAppointment appointment : appointments) {
            if (appointment.getDateTime().toLocalDate().equals(selectedDate))
                appointmentsToShow.add(appointment);
        }

        if (!appointmentsToShow.isEmpty()) {
            textViewNoElements.setVisibility(View.GONE);
            linearLayoutAppointments.setVisibility(View.VISIBLE);

            TextView textViewAppointments = new TextView(requireActivity());
            textViewAppointments.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            TypedValue value = new TypedValue();
            requireActivity().getTheme().resolveAttribute(com.google.android.material.R.attr.textAppearanceTitleMedium, value, true);
            textViewAppointments.setTextAppearance(value.data);
            textViewAppointments.setText(R.string.calendar_medical_appointments);
            linearLayoutMedicines.addView(textViewAppointments);

            for (MedicalAppointment appointment : appointmentsToShow) {
                TextView textViewAppointment = new TextView(requireActivity());
                textViewAppointment.setLayoutParams(new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                ));
                textViewAppointment.setText(appointment.getDateTime().toLocalTime().toString());

                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) textViewAppointment.getLayoutParams();
                textViewAppointment.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.calendar_schedule_container));
                textViewAppointment.setClickable(true);
                textViewAppointment.setFocusable(true);
                TypedValue foregroundValue = new TypedValue();
                requireActivity().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, foregroundValue, true);
                textViewAppointment.setForeground(ResourcesCompat.getDrawable(getResources(), foregroundValue.resourceId, requireActivity().getTheme()));
                int padding = getResources().getDimensionPixelSize(R.dimen.calendar_btn_padding);
                textViewAppointment.setPadding(padding, padding, padding, padding);
                params.setMargins(getResources().getDimensionPixelSize(R.dimen.calendar_btn_margin_start), getResources().getDimensionPixelSize(R.dimen.calendar_btn_margin_top),
                        0, 0);
                textViewAppointment.setLayoutParams(params);

                linearLayoutAppointments.addView(textViewAppointment);
            }
        }
    }

    private void setMedicinesSelectedDate(List<Medicine> medicines) {
        linearLayoutMedicines.removeAllViews();
        List<Medicine> medicinesToShow = new ArrayList<>();

        for (Medicine medicine : medicines) {
            ZonedDateTime dosingTime = medicine.getInitialDosingTime();
            Duration frequency = Duration.ofHours(medicine.getDosageFrequencyHours()).plusMinutes(medicine.getDosageFrequencyMinutes());

            while (!dosingTime.toLocalDate().isAfter(selectedDate)) {
                if (dosingTime.toLocalDate().isEqual(selectedDate)) {
                    medicinesToShow.add(medicine);
                    break;
                } else if (!frequency.isZero()) {
                    long dosesElapsed = Duration.between(dosingTime, selectedDate.atStartOfDay(dosingTime.getZone())).toMinutes() / frequency.toMinutes();
                    dosingTime = dosingTime.plus(frequency.multipliedBy(dosesElapsed + 1));
                } else {
                    break;
                }
            }
        }

        if (!medicinesToShow.isEmpty()) {
            textViewNoElements.setVisibility(View.GONE);
            linearLayoutMedicines.setVisibility(View.VISIBLE);

            TextView textViewMedicines = new TextView(requireActivity());
            textViewMedicines.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            TypedValue value = new TypedValue();
            requireActivity().getTheme().resolveAttribute(com.google.android.material.R.attr.textAppearanceTitleMedium, value, true);
            textViewMedicines.setTextAppearance(value.data);
            textViewMedicines.setText(R.string.calendar_medicines);
            linearLayoutMedicines.addView(textViewMedicines);

            for (Medicine medicine : medicinesToShow) {
                List<String> dosingTimes = new ArrayList<>();

                ZonedDateTime dosingTime = medicine.getInitialDosingTime();
                Duration frequency = Duration.ofHours(medicine.getDosageFrequencyHours()).plusMinutes(medicine.getDosageFrequencyMinutes());

                while (!dosingTime.toLocalDate().isAfter(selectedDate)) {
                    if (dosingTime.toLocalDate().isEqual(selectedDate)) {
                        dosingTimes.add(dosingTime.toLocalTime().toString());

                        if (!frequency.isZero()) {
                            dosingTime = dosingTime.plusMinutes(frequency.toMinutes());
                        } else {
                            break;
                        }
                    } else if (!frequency.isZero()) {
                        long dosesElapsed = Duration.between(dosingTime, selectedDate.atStartOfDay(dosingTime.getZone())).toMinutes() / frequency.toMinutes();
                        dosingTime = dosingTime.plus(frequency.multipliedBy(dosesElapsed + 1));
                    } else {
                        break;
                    }
                }

                ConstraintLayout constraintLayout = new ConstraintLayout(requireActivity());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                layoutParams.setMargins(0, getResources().getDimensionPixelSize(R.dimen.calendar_btn_margin_top), 0, 0);
                constraintLayout.setLayoutParams(layoutParams);

                TextView textViewMedicine = new TextView(requireActivity());
                textViewMedicine.setId(View.generateViewId());
                textViewMedicine.setLayoutParams(new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                ));
                textViewMedicine.setText(medicine.getName());
                textViewMedicine.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) textViewMedicine.getLayoutParams();
                params.matchConstraintMinWidth = getResources().getDimensionPixelSize(R.dimen.calendar_btn_min_width);
                params.matchConstraintMaxWidth = getResources().getDimensionPixelSize(R.dimen.calendar_btn_max_width);
                textViewMedicine.setLayoutParams(params);

                textViewMedicine.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.calendar_schedule_container));
                textViewMedicine.setFocusable(true);
                textViewMedicine.setClickable(true);
                textViewMedicine.setOnClickListener(view -> {
                    Fragment fragment = new MedicineFragment();
                    Bundle bundle = new Bundle();
                    bundle.putLong(Treatment.class.getSimpleName(), treatment.getId());
                    bundle.putLong(Medicine.class.getSimpleName(), medicine.getId());
                    fragment.setArguments(bundle);
                    ((MainActivity) requireActivity()).replaceFragment(fragment);
                });

                TypedValue foregroundValue = new TypedValue();
                requireActivity().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, foregroundValue, true);
                textViewMedicine.setForeground(ResourcesCompat.getDrawable(getResources(), foregroundValue.resourceId, requireActivity().getTheme()));
                int padding = getResources().getDimensionPixelSize(R.dimen.calendar_btn_padding);
                textViewMedicine.setPadding(padding, padding, padding, padding);
                params.setMarginStart(getResources().getDimensionPixelSize(R.dimen.calendar_btn_margin_start));
                textViewMedicine.setLayoutParams(params);

                constraintLayout.addView(textViewMedicine);

                TextView textViewDosingTimes = new TextView(requireActivity());
                textViewDosingTimes.setId(View.generateViewId());
                textViewDosingTimes.setLayoutParams(new ConstraintLayout.LayoutParams(
                        0,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                ));
                textViewDosingTimes.setText(TextUtils.join(", ", dosingTimes));
                constraintLayout.addView(textViewDosingTimes);

                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(constraintLayout);
                constraintSet.connect(textViewMedicine.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                constraintSet.connect(textViewMedicine.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                constraintSet.connect(textViewMedicine.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
                constraintSet.connect(textViewDosingTimes.getId(), ConstraintSet.START, textViewMedicine.getId(), ConstraintSet.END, getResources().getDimensionPixelSize(R.dimen.calendar_btn_margin_start));
                constraintSet.connect(textViewDosingTimes.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                constraintSet.connect(textViewDosingTimes.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
                constraintSet.connect(textViewDosingTimes.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
                constraintSet.applyTo(constraintLayout);

                linearLayoutMedicines.addView(constraintLayout);
            }
        }
    }

    private void resetFilters() {
        passedAppointmentsFilter = true;
        pendingAppointmentsFilter = true;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnToolbarChangeListener)
            listener = (OnToolbarChangeListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
