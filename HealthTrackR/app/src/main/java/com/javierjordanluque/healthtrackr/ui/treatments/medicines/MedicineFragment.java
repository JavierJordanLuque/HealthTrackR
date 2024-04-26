package com.javierjordanluque.healthtrackr.ui.treatments.medicines;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.Medicine;
import com.javierjordanluque.healthtrackr.models.Treatment;
import com.javierjordanluque.healthtrackr.models.enumerations.AdministrationRoute;
import com.javierjordanluque.healthtrackr.ui.MainActivity;
import com.javierjordanluque.healthtrackr.ui.OnToolbarChangeListener;
import com.javierjordanluque.healthtrackr.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

public class MedicineFragment extends Fragment {
    private OnToolbarChangeListener listener;
    private Treatment treatment;
    private Medicine medicine;
    private ImageView imageViewStatus;
    private TextView textViewStatus;
    private TextView textViewName;
    private TextView textViewActiveSubstance;
    private TextView textViewAdministrationRoute;
    private TextView textViewDose;
    private TextView textViewInitialDosingTime;
    private TextView textViewDosingFrequency;
    private TextView textViewNextDose;
    private Handler handler;

    public MedicineFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_medicine, container, false);

        imageViewStatus = fragmentView.findViewById(R.id.imageViewStatus);
        textViewStatus = fragmentView.findViewById(R.id.textViewStatus);

        textViewName = fragmentView.findViewById(R.id.textViewName);
        textViewActiveSubstance = fragmentView.findViewById(R.id.textViewActiveSubstance);
        textViewActiveSubstance = fragmentView.findViewById(R.id.textViewActiveSubstance);
        textViewAdministrationRoute = fragmentView.findViewById(R.id.textViewAdministrationRoute);
        textViewDose = fragmentView.findViewById(R.id.textViewDose);
        textViewInitialDosingTime = fragmentView.findViewById(R.id.textViewInitialDosingTime);
        textViewDosingFrequency = fragmentView.findViewById(R.id.textViewDosingFrequency);
        textViewNextDose = fragmentView.findViewById(R.id.textViewNextDose);

        FloatingActionButton buttonModifyMedicine = fragmentView.findViewById(R.id.buttonModifyMedicine);
        buttonModifyMedicine.setOnClickListener(view -> {
            if (treatment.isFinished()) {
                ((MainActivity) requireActivity()).showTreatmentFinishedDialog();
            } else {
                Intent intent = new Intent(requireActivity(), ModifyMedicineActivity.class);
                intent.putExtra(Treatment.class.getSimpleName(), treatment.getId());
                intent.putExtra(Medicine.class.getSimpleName(), medicine.getId());
                startActivity(intent);
            }
        });

        FloatingActionButton buttonMedicineNotifications = fragmentView.findViewById(R.id.buttonMedicineNotifications);
        buttonMedicineNotifications.setOnClickListener(view -> {
            if (medicine.calculateNextDose() == null) {
                showNoMedicineNotificationsDialog();
            } else {
                Intent intent = new Intent(requireActivity(), ModifyMedicineNotificationsActivity.class);
                intent.putExtra(Treatment.class.getSimpleName(), treatment.getId());
                intent.putExtra(Medicine.class.getSimpleName(), medicine.getId());
                startActivity(intent);
            }
        });

        FloatingActionButton buttonDeleteMedicine = fragmentView.findViewById(R.id.buttonDeleteMedicine);
        buttonDeleteMedicine.setOnClickListener(view -> showDeleteMedicineConfirmationDialog());

        return fragmentView;
    }

    public void showNoMedicineNotificationsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setMessage(R.string.error_no_medicine_notifications);
        builder.setPositiveButton(R.string.dialog_positive_ok, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showDeleteMedicineConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setMessage(getString(R.string.medicines_dialog_message_delete))
                .setPositiveButton(getString(R.string.dialog_positive_delete), (dialog, id) -> {
                    try {
                        treatment.removeMedicine(requireActivity(), medicine);

                        Toast.makeText(requireActivity(), getString(R.string.medicines_toast_confirmation_delete), Toast.LENGTH_SHORT).show();
                        requireActivity().onBackPressed();
                    } catch (DBDeleteException exception) {
                        ExceptionManager.advertiseUI(requireActivity(), exception.getMessage());
                    }
                })
                .setNegativeButton(getString(R.string.dialog_negative_cancel), (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    @Override
    public void onResume() {
        super.onResume();

        treatment = ((MainActivity) requireActivity()).getTreatmentFromBundle(getArguments());
        ((MainActivity) requireActivity()).setTreatmentLayoutStatus(treatment, imageViewStatus, textViewStatus);

        medicine = ((MainActivity) requireActivity()).getMedicineFromBundle(treatment, getArguments());

        ((MainActivity) requireActivity()).currentFragment = this;
        ((MainActivity) requireActivity()).showBackButton(true);
        if (listener != null)
            listener.onTitleChanged(treatment.getTitle());

        textViewName.setText(medicine.getName());

        String activeSubstance = medicine.getActiveSubstance();
        if (activeSubstance != null) {
            textViewActiveSubstance.setText(activeSubstance);
        } else {
            textViewActiveSubstance.setText(R.string.unspecified);
        }

        AdministrationRoute administrationRoute = medicine.getAdministrationRoute();
        String[] administrationRouteOptions = getResources().getStringArray(R.array.medicines_array_administration_route);
        String administrationRouteString = administrationRouteOptions[administrationRoute.ordinal()];
        textViewAdministrationRoute.setText(administrationRouteString);

        Integer dose = medicine.getDose();
        if (dose != null) {
            String doseString = dose + getString(R.string.medicines_mg);
            textViewDose.setText(doseString);
        } else {
            textViewDose.setText(R.string.unspecified);
        }

        textViewInitialDosingTime.setText(((MainActivity) requireActivity()).showFormattedDateTime(medicine.getInitialDosingTime()));

        int dosageFrequencyHours = medicine.getDosageFrequencyHours();
        int dosageFrequencyMinutes = medicine.getDosageFrequencyMinutes();
        if (dosageFrequencyHours != 0 || dosageFrequencyMinutes != 0) {
            String dosingFrequencyString = getString(R.string.medicines_each) + " " +
                    (dosageFrequencyHours > 0? (dosageFrequencyHours + " " + getString(R.string.medicines_hours) + " ") : "") +
                    dosageFrequencyMinutes + " " + getString(R.string.medicines_minutes);
            textViewDosingFrequency.setText(dosingFrequencyString);
        } else {
            textViewDosingFrequency.setText(R.string.medicines_single_dose);
        }


        updateNextDose(true);
    }

    private void updateNextDose(boolean isFirst) {
        ZonedDateTime nextDose = medicine.calculateNextDose();
        if (nextDose != null) {
            ZonedDateTime now = ZonedDateTime.now();
            textViewNextDose.setText(((MainActivity) requireActivity()).formatTimeDifference(Duration.between(now, nextDose).toMillis()));

            long delayMillis = isFirst ? TimeUnit.SECONDS.toMillis(30 - now.getSecond() % 30) : TimeUnit.MINUTES.toMillis(1);

            handler.postDelayed(() -> updateNextDose(false), delayMillis);
        } else {
            textViewNextDose.setText(R.string.medicines_none);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnToolbarChangeListener)
            listener = (OnToolbarChangeListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        handler.removeCallbacksAndMessages(null);
    }
}