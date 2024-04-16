package com.javierjordanluque.healthtrackr.ui.account;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.Allergy;
import com.javierjordanluque.healthtrackr.models.PreviousMedicalCondition;
import com.javierjordanluque.healthtrackr.models.User;
import com.javierjordanluque.healthtrackr.models.enumerations.BloodType;
import com.javierjordanluque.healthtrackr.models.enumerations.Gender;
import com.javierjordanluque.healthtrackr.ui.AuthenticationActivity;
import com.javierjordanluque.healthtrackr.ui.MainActivity;
import com.javierjordanluque.healthtrackr.ui.OnToolbarChangeListener;
import com.javierjordanluque.healthtrackr.util.AuthenticationService;
import com.javierjordanluque.healthtrackr.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AccountFragment extends Fragment {
    private OnToolbarChangeListener listener;
    private User user;
    private TextView textViewEmail;
    private TextView textViewFirstName;
    private TextView textViewLastName;
    private TextView textViewBirthDate;
    private TextView textViewGender;
    private TextView textViewBloodType;
    private TextView textViewAllergies;
    private TextView textViewPreviousMedicalConditions;

    public AccountFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_account, container, false);

        textViewEmail = fragmentView.findViewById(R.id.textViewEmail);
        textViewFirstName = fragmentView.findViewById(R.id.textViewFirstName);
        textViewLastName = fragmentView.findViewById(R.id.textViewLastName);
        textViewBirthDate = fragmentView.findViewById(R.id.textViewBirthDate);
        textViewGender = fragmentView.findViewById(R.id.textViewGender);
        textViewBloodType = fragmentView.findViewById(R.id.textViewBloodType);
        textViewAllergies = fragmentView.findViewById(R.id.textViewAllergies);
        textViewPreviousMedicalConditions = fragmentView.findViewById(R.id.textViewPreviousMedicalConditions);

        FloatingActionButton buttonModifyAccount = fragmentView.findViewById(R.id.buttonModifyAccount);
        buttonModifyAccount.setOnClickListener(view -> {
            Intent intent = new Intent(requireActivity(), ModifyAccountActivity.class);
            startActivity(intent);
        });

        Button buttonSignOut = fragmentView.findViewById(R.id.buttonSignOut);
        buttonSignOut.setOnClickListener(view -> ((MainActivity) requireActivity()).showSignOutConfirmationDialog());

        Button buttonDeleteAccount = fragmentView.findViewById(R.id.buttonDeleteAccount);
        buttonDeleteAccount.setOnClickListener(view -> showDeleteAccountConfirmationDialog());

        return fragmentView;
    }

    private void showDeleteAccountConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setMessage(getString(R.string.account_dialog_message_delete))
                .setPositiveButton(getString(R.string.dialog_positive_delete), (dialog, id) -> {
                    try {
                        user.deleteUser(requireActivity());
                        AuthenticationService.logout(requireActivity(), user);
                        AuthenticationService.clearCredentials(requireActivity());

                        Toast.makeText(requireActivity(), getString(R.string.account_toast_confirmation_delete), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(requireActivity(), AuthenticationActivity.class);
                        startActivity(intent);
                        requireActivity().finish();
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

        user = ((MainActivity) requireActivity()).sessionViewModel.getUserSession();

        ((MainActivity) requireActivity()).showBackButton(false);
        if (listener != null)
            listener.onTitleChanged(getString(R.string.account_title));

        textViewEmail.setText(user.getEmail());
        textViewFirstName.setText(user.getFirstName());
        textViewLastName.setText(user.getLastName());

        LocalDate birthDate = user.getBirthDate();
        if (birthDate != null) {
            textViewBirthDate.setText(((MainActivity) requireActivity()).showFormattedDate(birthDate));
        } else {
            textViewBirthDate.setText(R.string.unspecified);
        }

        Gender gender = user.getGender();
        if (gender != null) {
            String[] genderOptions = getResources().getStringArray(R.array.account_array_gender);
            String genderString = genderOptions[gender.ordinal()];
            textViewGender.setText(genderString);
        } else {
            textViewGender.setText(R.string.unspecified);
        }

        BloodType bloodType = user.getBloodType();
        if (bloodType != null) {
            String[] bloodTypeOptions = getResources().getStringArray(R.array.account_array_blood_type);
            String bloodTypeString = bloodTypeOptions[bloodType.ordinal()];
            textViewBloodType.setText(bloodTypeString);
        } else {
            textViewBloodType.setText(R.string.unspecified);
        }

        try {
            List<Allergy> allergies = user.getAllergies(requireActivity());

            if (allergies != null && !allergies.isEmpty()) {
                List<String> allergiesNames = new ArrayList<>();
                for (Allergy allergy : allergies)
                    allergiesNames.add(allergy.getName());

                textViewAllergies.setText(((MainActivity) requireActivity()).showFormattedList(allergiesNames));
            } else {
                textViewAllergies.setText(R.string.unspecified);
            }
        } catch (DBFindException exception) {
            ExceptionManager.advertiseUI(requireActivity(), exception.getMessage());
        }

        try {
            List<PreviousMedicalCondition> conditions = user.getConditions(requireActivity());

            if (conditions != null && !conditions.isEmpty()) {
                List<String> conditionsNames = new ArrayList<>();
                for (PreviousMedicalCondition condition : conditions)
                    conditionsNames.add(condition.getName());

                textViewPreviousMedicalConditions.setText(((MainActivity) requireActivity()).showFormattedList(conditionsNames));
            } else {
                textViewPreviousMedicalConditions.setText(R.string.unspecified);
            }
        } catch (DBFindException exception) {
            ExceptionManager.advertiseUI(requireActivity(), exception.getMessage());
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
    }
}
