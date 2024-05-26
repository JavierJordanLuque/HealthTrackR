package com.javierjordanluque.healthtrackr.ui.treatments.calendar.appointments;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.Location;
import com.javierjordanluque.healthtrackr.models.MedicalAppointment;
import com.javierjordanluque.healthtrackr.models.Treatment;
import com.javierjordanluque.healthtrackr.ui.MainActivity;
import com.javierjordanluque.healthtrackr.ui.OnToolbarChangeListener;
import com.javierjordanluque.healthtrackr.util.NavigationUtils;
import com.javierjordanluque.healthtrackr.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;
import com.javierjordanluque.healthtrackr.util.notifications.MedicalAppointmentNotification;
import com.javierjordanluque.healthtrackr.util.notifications.NotificationScheduler;

public class MedicalAppointmentFragment extends Fragment {
    private OnToolbarChangeListener listener;
    private Treatment treatment;
    private MedicalAppointment medicalAppointment;
    private ImageView imageViewStatus;
    private TextView textViewStatus;
    private TextView textViewDateTime;
    private TextView textViewSubject;
    private TextView textViewLocation;
    private LinearLayout layoutLocation;
    private TextView textViewLatitude;
    private TextView textViewLongitude;

    public MedicalAppointmentFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_medical_appointment, container, false);

        imageViewStatus = fragmentView.findViewById(R.id.imageViewStatus);
        textViewStatus = fragmentView.findViewById(R.id.textViewStatus);

        textViewDateTime = fragmentView.findViewById(R.id.textViewDateTime);
        textViewSubject = fragmentView.findViewById(R.id.textViewSubject);
        textViewLocation = fragmentView.findViewById(R.id.textViewLocation);
        layoutLocation = fragmentView.findViewById(R.id.layoutLocation);
        textViewLatitude = fragmentView.findViewById(R.id.textViewLatitude);
        textViewLongitude = fragmentView.findViewById(R.id.textViewLongitude);

        ImageButton imageButtonDirections = fragmentView.findViewById(R.id.imageButtonDirections);
        imageButtonDirections.setOnClickListener(view -> {
            if (medicalAppointment.getLocation() == null) {
                showNoLocationDialog();
            } else {
                NavigationUtils.openGoogleMaps(requireActivity(), medicalAppointment.getLocation());
            }
        });

        FloatingActionButton buttonModifyMedicalAppointment = fragmentView.findViewById(R.id.buttonModifyMedicalAppointment);
        buttonModifyMedicalAppointment.setOnClickListener(view -> {
            if (treatment.isFinished()) {
                ((MainActivity) requireActivity()).showTreatmentFinishedDialog();
            } else {
                Intent intent = new Intent(requireActivity(), ModifyMedicalAppointmentActivity.class);
                intent.putExtra(Treatment.class.getSimpleName(), treatment.getId());
                intent.putExtra(MedicalAppointment.class.getSimpleName(), medicalAppointment.getId());
                startActivity(intent);
            }
        });

        FloatingActionButton buttonMedicalAppointmentNotification = fragmentView.findViewById(R.id.buttonMedicalAppointmentNotification);
        buttonMedicalAppointmentNotification.setOnClickListener(view -> {
            if (!medicalAppointment.isPending()) {
                showNoMedicalAppointmentNotificationDialog();
            } else {
                Intent intent = new Intent(requireActivity(), ModifyMedicalAppointmentNotificationActivity.class);
                intent.putExtra(Treatment.class.getSimpleName(), treatment.getId());
                intent.putExtra(MedicalAppointment.class.getSimpleName(), medicalAppointment.getId());
                startActivity(intent);
            }
        });

        FloatingActionButton buttonDeleteMedicalAppointment = fragmentView.findViewById(R.id.buttonDeleteMedicalAppointment);
        buttonDeleteMedicalAppointment.setOnClickListener(view -> showDeleteMedicalAppointmentConfirmationDialog());

        return fragmentView;
    }

    public void showNoLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setMessage(R.string.medical_appointment_dialog_message_no_location);
        builder.setPositiveButton(R.string.dialog_positive_ok, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    public void showNoMedicalAppointmentNotificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setMessage(R.string.error_no_medical_appointment_notification);
        builder.setPositiveButton(R.string.dialog_positive_ok, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showDeleteMedicalAppointmentConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setMessage(getString(R.string.medical_appointment_dialog_message_delete))
                .setPositiveButton(getString(R.string.button_delete), (dialog, id) -> {
                    try {
                        treatment.removeAppointment(requireActivity(), medicalAppointment);

                        Toast.makeText(requireActivity(), getString(R.string.medical_appointment_toast_confirmation_delete), Toast.LENGTH_SHORT).show();
                        requireActivity().onBackPressed();
                    } catch (DBFindException | DBDeleteException exception) {
                        ExceptionManager.advertiseUI(requireActivity(), exception.getMessage());
                    }
                })
                .setNegativeButton(getString(R.string.dialog_negative_cancel), (dialog, id) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public void onResume() {
        super.onResume();

        treatment = ((MainActivity) requireActivity()).getTreatmentFromBundle(getArguments());
        ((MainActivity) requireActivity()).setTreatmentLayoutStatus(treatment, imageViewStatus, textViewStatus);

        medicalAppointment = ((MainActivity) requireActivity()).getMedicalAppointmentFromBundle(treatment, getArguments());

        ((MainActivity) requireActivity()).currentFragment = this;
        ((MainActivity) requireActivity()).showBackButton(true);
        if (listener != null)
            listener.onTitleChanged(treatment.getTitle());

        String subject = medicalAppointment.getSubject();
        if (subject != null) {
            textViewSubject.setText(subject);
        } else {
            textViewSubject.setText(R.string.unspecified);
        }

        textViewDateTime.setText(((MainActivity) requireActivity()).showFormattedDateTime(medicalAppointment.getDateTime()));

        Location location = medicalAppointment.getLocation();
        if (location != null) {
            if (location.getPlace() != null) {
                layoutLocation.setVisibility(View.GONE);
                textViewLocation.setVisibility(View.VISIBLE);
                textViewLocation.setText(location.getPlace());
            } else {
                layoutLocation.setVisibility(View.VISIBLE);
                textViewLocation.setVisibility(View.GONE);
                textViewLatitude.setText(String.valueOf(location.getLatitude()));
                textViewLongitude.setText(String.valueOf(location.getLongitude()));
            }
        } else {
            layoutLocation.setVisibility(View.GONE);
            textViewLocation.setVisibility(View.VISIBLE);
            textViewLocation.setText(R.string.unspecified);
        }

        checkNotification();
    }

    private void checkNotification() {
        try {
            MedicalAppointmentNotification notification = medicalAppointment.getNotification(requireActivity());

            if (notification != null) {
                PendingIntent pendingIntent = NotificationScheduler.buildPendingIntent(requireActivity(), notification.getId(), true, true);
                if (pendingIntent == null)
                    medicalAppointment.removeNotification(requireActivity(), notification);
            }
        } catch (DBFindException | DBDeleteException exception) {
            ExceptionManager.advertiseUI(requireActivity(), exception.getMessage());
        }
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
