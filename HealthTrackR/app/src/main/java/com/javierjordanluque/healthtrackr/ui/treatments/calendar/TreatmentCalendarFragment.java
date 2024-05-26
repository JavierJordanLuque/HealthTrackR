package com.javierjordanluque.healthtrackr.ui.treatments.calendar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
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
import com.javierjordanluque.healthtrackr.ui.calendar.decorators.CurrentDateDecorator;
import com.javierjordanluque.healthtrackr.ui.calendar.decorators.MedicalAppointmentDecorator;
import com.javierjordanluque.healthtrackr.ui.calendar.decorators.MedicineDecorator;
import com.javierjordanluque.healthtrackr.ui.calendar.decorators.TreatmentEndDateDecorator;
import com.javierjordanluque.healthtrackr.ui.calendar.decorators.TreatmentStartDateDecorator;
import com.javierjordanluque.healthtrackr.ui.treatments.calendar.appointments.AddMedicalAppointmentActivity;
import com.javierjordanluque.healthtrackr.ui.treatments.calendar.appointments.MedicalAppointmentFragment;
import com.javierjordanluque.healthtrackr.ui.treatments.medicines.MedicineFragment;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter;
import com.prolificinteractive.materialcalendarview.format.MonthArrayTitleFormatter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TreatmentCalendarFragment extends Fragment {
    private OnToolbarChangeListener listener;
    private Treatment treatment;
    private ImageView imageViewStatus;
    private TextView textViewStatus;
    private MaterialCalendarView calendarView;
    private LocalDate selectedDate;
    private TextView textViewSelectedDate;
    private ImageButton imageButtonLegend;
    private LinearLayout linearLayoutNoElements;
    private LinearLayout linearLayout;
    private MedicalAppointmentDecorator medicalAppointmentDecorator;
    private MedicineDecorator medicineDecorator;
    private Boolean pendingAppointmentsFilter;
    private Boolean passedAppointmentsFilter;
    private Boolean pendingMedicationsFilter;
    private Boolean passedMedicationsFilter;

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
        linearLayoutNoElements = fragmentView.findViewById(R.id.linearLayoutNoElements);
        linearLayout = fragmentView.findViewById(R.id.linearLayout);

        FloatingActionButton buttonAddMedicalAppointment = fragmentView.findViewById(R.id.buttonAddMedicalAppointment);
        buttonAddMedicalAppointment.setOnClickListener(view -> {
            if (treatment.isFinished()) {
                ((MainActivity) requireActivity()).showTreatmentFinishedDialog();
            } else {
                Intent intent = new Intent(requireActivity(), AddMedicalAppointmentActivity.class);
                intent.putExtra(Treatment.class.getSimpleName(), treatment.getId());

                ((MainActivity) requireActivity()).fragmentLauncher.launch(intent);
            }
        });

        imageButtonLegend = fragmentView.findViewById(R.id.imageButtonLegend);
        imageButtonLegend.setOnClickListener(view -> {
            View popupView = getLayoutInflater().inflate(R.layout.calendar_legend, null);
            PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
            popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            popupWindow.showAsDropDown(imageButtonLegend, -popupView.getMeasuredWidth(), -imageButtonLegend.getWidth() - 20);
        });

        ExtendedFloatingActionButton buttonFilterCalendar = fragmentView.findViewById(R.id.buttonFilterCalendar);
        buttonFilterCalendar.setOnClickListener(view -> {
            View popupView = getLayoutInflater().inflate(R.layout.filter_calendar, null);
            PopupWindow popupWindow = new PopupWindow(popupView, buttonFilterCalendar.getWidth(), LinearLayout.LayoutParams.WRAP_CONTENT, true);
            popupWindow.showAsDropDown(buttonFilterCalendar, 0, 0);

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

            CheckBox checkBoxPassedMedications = popupView.findViewById(R.id.checkBoxPassedMedications);
            if (passedMedicationsFilter != null) {
                checkBoxPassedMedications.setChecked(passedMedicationsFilter);
            } else {
                checkBoxPassedMedications.setChecked(true);
            }

            CheckBox checkBoxPendingMedications = popupView.findViewById(R.id.checkBoxPendingMedications);
            if (pendingMedicationsFilter != null) {
                checkBoxPendingMedications.setChecked(pendingMedicationsFilter);
            } else {
                checkBoxPendingMedications.setChecked(true);
            }

            Button buttonFilter = popupView.findViewById(R.id.buttonFilter);
            buttonFilter.setOnClickListener(v -> {
                passedAppointmentsFilter = checkBoxPassedAppointments.isChecked();
                pendingAppointmentsFilter = checkBoxPendingAppointments.isChecked();
                passedMedicationsFilter = checkBoxPassedMedications.isChecked();
                pendingMedicationsFilter = checkBoxPendingMedications.isChecked();

                try {
                    List<MedicalAppointment> filteredAppointments = treatment.filterAppointments(passedAppointmentsFilter, pendingAppointmentsFilter);
                    List<Medicine> filteredMedications = treatment.getMedicines(requireActivity());

                    showHighlightedMedicalAppointments(filteredAppointments);
                    showHighlightedMedications(filteredMedications);
                } catch (DBFindException exception) {
                    ExceptionManager.advertiseUI(requireActivity(), exception.getMessage());
                }

                popupWindow.dismiss();
            });

            Button buttonShowAllFilter = popupView.findViewById(R.id.buttonShowAllFilter);
            buttonShowAllFilter.setOnClickListener(v -> {
                try {
                    resetFilters();

                    List<MedicalAppointment> unfilteredAppointments = treatment.getAppointments(requireActivity());
                    List<Medicine> unfilteredMedications = treatment.getMedicines(requireActivity());

                    showHighlightedMedicalAppointments(unfilteredAppointments);
                    showHighlightedMedications(unfilteredMedications);
                } catch (DBFindException exception) {
                    ExceptionManager.advertiseUI(requireActivity(), exception.getMessage());
                }

                popupWindow.dismiss();
            });
        });

        return fragmentView;
    }

    private void setCalendarView() {
        calendarView.setTitleFormatter(new MonthArrayTitleFormatter(getResources().getTextArray(R.array.months)));
        calendarView.setWeekDayFormatter(new ArrayWeekDayFormatter(getResources().getTextArray(R.array.weekdays)));

        calendarView.setSelectedDate(CalendarDay.today());
        calendarView.addDecorator(new CurrentDateDecorator(requireActivity()));

        boolean isNightMode = ((MainActivity) requireActivity()).isNightMode();
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

        if (selectedDate == null)
            selectedDate = LocalDate.now();

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
            showHighlightedMedications(treatment.getMedicines(requireActivity()));
        } catch (DBFindException exception) {
            ExceptionManager.advertiseUI(requireActivity(), exception.getMessage());
        }

        showSelectedDateSchedule();
    }

    private void showHighlightedMedicalAppointments(List<MedicalAppointment> appointments) {
        Collection<CalendarDay> highlightedDates = new ArrayList<>();

        for (MedicalAppointment appointment : appointments)
            highlightedDates.add(CalendarDay.from(appointment.getDateTime().getYear(), appointment.getDateTime().getMonthValue(), appointment.getDateTime().getDayOfMonth()));

        if (medicalAppointmentDecorator != null)
            calendarView.removeDecorator(medicalAppointmentDecorator);

        medicalAppointmentDecorator = new MedicalAppointmentDecorator(requireActivity(), highlightedDates);
        calendarView.addDecorator(medicalAppointmentDecorator);
    }

    private void showHighlightedMedications(List<Medicine> medicines) {
        Collection<CalendarDay> highlightedDates = new ArrayList<>();

        boolean isPassedFilter = Boolean.TRUE.equals(passedMedicationsFilter);
        boolean isPendingFilter = Boolean.TRUE.equals(pendingMedicationsFilter);

        if (passedMedicationsFilter == null || pendingMedicationsFilter == null || isPassedFilter || isPendingFilter) {
            for (Medicine medicine : medicines) {
                ZonedDateTime cntDosingTime = medicine.getInitialDosingTime();
                ZonedDateTime currentDateTime = ZonedDateTime.now();
                ZonedDateTime endDate = medicine.getTreatment().getEndDate();
                LocalDate limitDate;

                if (passedMedicationsFilter == null || pendingMedicationsFilter == null || (isPassedFilter && isPendingFilter)) {
                    limitDate = endDate != null ? endDate.toLocalDate() : currentDateTime.toLocalDate().plusDays(30);
                } else if (isPassedFilter) {
                    if (cntDosingTime.isAfter(currentDateTime))
                        continue;

                    limitDate = endDate != null && endDate.toLocalDate().isBefore(currentDateTime.toLocalDate()) ? endDate.toLocalDate() : currentDateTime.toLocalDate();
                } else {
                    if (endDate == null || !endDate.toLocalDate().isBefore(currentDateTime.toLocalDate())) {
                        if (cntDosingTime.isBefore(currentDateTime)) {
                            Duration frequency = Duration.ofHours(medicine.getDosageFrequencyHours()).plusMinutes(medicine.getDosageFrequencyMinutes());

                            if (frequency.isZero())
                                continue;

                            long dosesElapsed = Duration.between(cntDosingTime, currentDateTime).toMinutes() / frequency.toMinutes();
                            cntDosingTime = cntDosingTime.plus(frequency.multipliedBy(dosesElapsed + 1));
                        }

                        limitDate = endDate != null ? endDate.toLocalDate() : currentDateTime.toLocalDate().plusDays(10);
                    } else {
                        continue;
                    }
                }

                Duration frequency = Duration.ofHours(medicine.getDosageFrequencyHours()).plusMinutes(medicine.getDosageFrequencyMinutes());

                while (!cntDosingTime.toLocalDate().isAfter(limitDate)) {
                    CalendarDay calendarDay = CalendarDay.from(cntDosingTime.getYear(), cntDosingTime.getMonthValue(), cntDosingTime.getDayOfMonth());
                    if (!highlightedDates.contains(calendarDay))
                        highlightedDates.add(calendarDay);

                    if (!frequency.isZero()) {
                        long dosesElapsed = Duration.between(cntDosingTime, cntDosingTime.plusDays(1).truncatedTo(ChronoUnit.DAYS)).toMinutes() / frequency.toMinutes();
                        cntDosingTime = cntDosingTime.plus(frequency.multipliedBy(dosesElapsed + 1));
                    } else {
                        break;
                    }
                }
            }

            if (medicineDecorator != null)
                calendarView.removeDecorator(medicineDecorator);

            medicineDecorator = new MedicineDecorator(requireActivity(), highlightedDates);
            calendarView.addDecorator(medicineDecorator);
        } else {
            if (medicineDecorator != null)
                calendarView.removeDecorator(medicineDecorator);
        }
    }

    private void showSelectedDateSchedule() {
        setTextViewSelectedDate();
        linearLayoutNoElements.setVisibility(View.VISIBLE);
        linearLayout.setVisibility(View.GONE);
        linearLayout.removeAllViews();

        if (!treatment.getStartDate().toLocalDate().isAfter(selectedDate) && (treatment.getEndDate() == null || !treatment.getEndDate().toLocalDate().isBefore(selectedDate))) {
            boolean hasStarted = treatment.getStartDate().toLocalDate().equals(selectedDate);
            boolean hasFinished = treatment.getEndDate() != null && treatment.getEndDate().toLocalDate().equals(selectedDate);

            try {
                List<MedicalAppointment> appointmentsToShow = new ArrayList<>();
                for (MedicalAppointment appointment : treatment.getAppointments(requireActivity()))
                    if (appointment.getDateTime().toLocalDate().equals(selectedDate))
                        appointmentsToShow.add(appointment);

                List<Medicine> medicinesToShow = new ArrayList<>();
                for (Medicine medicine : treatment.getMedicines(requireActivity())) {
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
                if (hasStarted || hasFinished || !appointmentsToShow.isEmpty() || !medicinesToShow.isEmpty()) {
                    View separatorView = new View(requireActivity());
                    separatorView.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            getResources().getDimensionPixelSize(R.dimen.view_separator_height)
                    ));
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) separatorView.getLayoutParams();
                    int margin = getResources().getDimensionPixelSize(R.dimen.view_separator_margin);
                    params.setMargins(margin, margin, margin, 0);
                    separatorView.setLayoutParams(params);
                    TypedValue value = new TypedValue();
                    requireActivity().getTheme().resolveAttribute(com.google.android.material.R.attr.colorOutlineVariant, value, true);
                    separatorView.setBackgroundResource(value.resourceId);
                    linearLayout.addView(separatorView);

                    linearLayoutNoElements.setVisibility(View.GONE);
                    linearLayout.setVisibility(View.VISIBLE);
                }

                if (hasStarted)
                    setLimitSelectedDate(R.string.calendar_treatment_start);
                if (!appointmentsToShow.isEmpty())
                    setAppointmentsSelectedDate(appointmentsToShow);
                if (!medicinesToShow.isEmpty())
                    setMedicationsSelectedDate(medicinesToShow);
                if (hasFinished)
                    setLimitSelectedDate(R.string.calendar_treatment_end);
            } catch (DBFindException exception) {
                ExceptionManager.advertiseUI(requireActivity(), exception.getMessage());
            }
        }
    }

    private void setTextViewSelectedDate() {
        LocalDate today = LocalDate.now();

        Locale locale = getResources().getConfiguration().getLocales().get(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale);

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

    private void setLimitSelectedDate(int text) {
        TextView textViewLimitDate = new TextView(requireActivity());
        textViewLimitDate.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) textViewLimitDate.getLayoutParams();
        params.setMargins(0, getResources().getDimensionPixelSize(R.dimen.text_view_margin_top), 0, 0);
        textViewLimitDate.setLayoutParams(params);

        TypedValue value = new TypedValue();
        requireActivity().getTheme().resolveAttribute(com.google.android.material.R.attr.textAppearanceTitleMedium, value, true);
        textViewLimitDate.setTextAppearance(value.data);
        textViewLimitDate.setText(text);
        linearLayout.addView(textViewLimitDate);
    }

    private void setAppointmentsSelectedDate(List<MedicalAppointment> appointmentsToShow) {
        TextView textViewAppointments = new TextView(requireActivity());
        textViewAppointments.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) textViewAppointments.getLayoutParams();
        params.setMargins(0, getResources().getDimensionPixelSize(R.dimen.text_view_margin_top), 0, 0);
        textViewAppointments.setLayoutParams(params);

        TypedValue value = new TypedValue();
        requireActivity().getTheme().resolveAttribute(com.google.android.material.R.attr.textAppearanceTitleMedium, value, true);
        textViewAppointments.setTextAppearance(value.data);
        textViewAppointments.setText(R.string.calendar_medical_appointments);
        linearLayout.addView(textViewAppointments);

        for (MedicalAppointment appointment : appointmentsToShow) {
            TextView textViewAppointment = new TextView(requireActivity());
            textViewAppointment.setLayoutParams(new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
            ));
            textViewAppointment.setText(appointment.getDateTime().toLocalTime().toString());
            textViewAppointment.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            ConstraintLayout.LayoutParams constraintParams = (ConstraintLayout.LayoutParams) textViewAppointment.getLayoutParams();
            constraintParams.matchConstraintMinWidth = getResources().getDimensionPixelSize(R.dimen.calendar_btn_min_width);
            constraintParams.matchConstraintMaxWidth = getResources().getDimensionPixelSize(R.dimen.calendar_btn_max_width);
            constraintParams.setMargins(
                    getResources().getDimensionPixelSize(R.dimen.calendar_btn_margin_start),
                    getResources().getDimensionPixelSize(R.dimen.calendar_btn_margin_top),
                    0,
                    0
            );
            textViewAppointment.setLayoutParams(constraintParams);
            int padding = getResources().getDimensionPixelSize(R.dimen.calendar_btn_padding);
            textViewAppointment.setPadding(padding, padding, padding, padding);

            textViewAppointment.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.calendar_schedule_appointment_container));
            value = new TypedValue();
            requireActivity().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, value, true);
            textViewAppointment.setForeground(ResourcesCompat.getDrawable(getResources(), value.resourceId, requireActivity().getTheme()));

            textViewAppointment.setFocusable(true);
            textViewAppointment.setClickable(true);
            textViewAppointment.setOnClickListener(view -> {
                    Fragment fragment = new MedicalAppointmentFragment();
                    Bundle bundle = new Bundle();
                    bundle.putLong(Treatment.class.getSimpleName(), appointment.getTreatment().getId());
                    bundle.putLong(MedicalAppointment.class.getSimpleName(), appointment.getId());
                    fragment.setArguments(bundle);
                    ((MainActivity) requireActivity()).replaceFragment(fragment);
            });

            linearLayout.addView(textViewAppointment);
        }
    }

    private void setMedicationsSelectedDate(List<Medicine> medicinesToShow) {
        TextView textViewMedicines = new TextView(requireActivity());
        textViewMedicines.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) textViewMedicines.getLayoutParams();
        params.setMargins(0, getResources().getDimensionPixelSize(R.dimen.text_view_margin_top), 0, 0);
        textViewMedicines.setLayoutParams(params);

        TypedValue value = new TypedValue();
        requireActivity().getTheme().resolveAttribute(com.google.android.material.R.attr.textAppearanceTitleMedium, value, true);
        textViewMedicines.setTextAppearance(value.data);
        textViewMedicines.setText(R.string.calendar_medicines);
        linearLayout.addView(textViewMedicines);

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
            params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, getResources().getDimensionPixelSize(R.dimen.calendar_btn_margin_top), 0, 0);
            constraintLayout.setLayoutParams(params);

            TextView textViewMedicine = new TextView(requireActivity());
            textViewMedicine.setId(View.generateViewId());
            textViewMedicine.setLayoutParams(new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
            ));
            textViewMedicine.setText(medicine.getName());
            textViewMedicine.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            ConstraintLayout.LayoutParams constraintParams = (ConstraintLayout.LayoutParams) textViewMedicine.getLayoutParams();
            constraintParams.matchConstraintMinWidth = getResources().getDimensionPixelSize(R.dimen.calendar_btn_min_width);
            constraintParams.matchConstraintMaxWidth = getResources().getDimensionPixelSize(R.dimen.calendar_btn_max_width);
            constraintParams.setMarginStart(getResources().getDimensionPixelSize(R.dimen.calendar_btn_margin_start));
            textViewMedicine.setLayoutParams(constraintParams);
            int padding = getResources().getDimensionPixelSize(R.dimen.calendar_btn_padding);
            textViewMedicine.setPadding(padding, padding, padding, padding);

            if (((MainActivity) requireActivity()).isNightMode())
                textViewMedicine.setTextColor(ContextCompat.getColor(requireActivity(), R.color.light_onPrimary));
            textViewMedicine.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.calendar_schedule_medication_container));
            value = new TypedValue();
            requireActivity().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, value, true);
            textViewMedicine.setForeground(ResourcesCompat.getDrawable(getResources(), value.resourceId, requireActivity().getTheme()));

            textViewMedicine.setFocusable(true);
            textViewMedicine.setClickable(true);
            textViewMedicine.setOnClickListener(view -> {
                Fragment fragment = new MedicineFragment();
                Bundle bundle = new Bundle();
                bundle.putLong(Treatment.class.getSimpleName(), medicine.getTreatment().getId());
                bundle.putLong(Medicine.class.getSimpleName(), medicine.getId());
                fragment.setArguments(bundle);
                ((MainActivity) requireActivity()).replaceFragment(fragment);
            });

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

            linearLayout.addView(constraintLayout);
        }
    }

    private void resetFilters() {
        passedAppointmentsFilter = true;
        pendingAppointmentsFilter = true;
        passedMedicationsFilter = true;
        pendingMedicationsFilter = true;
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
