package com.javierjordanluque.healthtrackr.ui.treatments.guidelines;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;

import com.google.android.material.textfield.TextInputLayout;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.Guideline;
import com.javierjordanluque.healthtrackr.models.Multimedia;
import com.javierjordanluque.healthtrackr.models.Treatment;
import com.javierjordanluque.healthtrackr.models.enumerations.MultimediaType;
import com.javierjordanluque.healthtrackr.ui.BaseActivity;
import com.javierjordanluque.healthtrackr.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBUpdateException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ModifyGuidelineActivity extends BaseActivity {
    private Treatment treatment;
    private Guideline guideline;
    private TextInputLayout layoutTitle;
    private TextInputLayout layoutDescription;
    private EditText editTextTitle;
    private EditText editTextDescription;
    private Spinner spinnerNumOrder;
    private LinearLayout linearLayoutImages;
    private LinearLayout linearLayoutVideos;
    private List<Multimedia> currentMultimedias;
    private final List<String> newImagesPath = new ArrayList<>();
    private final List<String> newVideosPath = new ArrayList<>();
    private static final String STATE_IMAGES_PATH = "state_images_path";
    private static final String STATE_VIDEOS_PATH = "state_videos_path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_guideline);
        setUpToolbar(getString(R.string.guidelines_app_bar_title_modify));
        showBackButton(true);

        treatment = getTreatmentFromIntent(getIntent());
        guideline = getGuidelineFromIntent(treatment, getIntent());

        layoutTitle = findViewById(R.id.layoutTitle);
        editTextTitle = findViewById(R.id.editTextTitle);
        setEditTextListener(layoutTitle, editTextTitle);
        editTextTitle.setText(guideline.getTitle());

        layoutDescription = findViewById(R.id.layoutDescription);
        editTextDescription = findViewById(R.id.editTextDescription);
        setEditTextListener(layoutDescription, editTextDescription);
        String description = guideline.getDescription();
        if (description != null)
            editTextDescription.setText(description);

        configureNumOrderSpinner();

        linearLayoutImages = findViewById(R.id.linearLayoutImages);
        linearLayoutVideos = findViewById(R.id.linearLayoutVideos);
        if (savedInstanceState != null) {
            ArrayList<String> savedImagesPath = savedInstanceState.getStringArrayList(STATE_IMAGES_PATH);
            if (savedImagesPath != null)
                newImagesPath.addAll(savedImagesPath);

            ArrayList<String> savedVideosPath = savedInstanceState.getStringArrayList(STATE_VIDEOS_PATH);
            if (savedVideosPath != null)
                newVideosPath.addAll(savedVideosPath);
        }
        setMultimediaLayouts();

        Button buttonAddImage = findViewById(R.id.buttonAddImage);
        buttonAddImage.setOnClickListener(this::addImage);

        Button buttonAddVideo = findViewById(R.id.buttonAddVideo);
        buttonAddVideo.setOnClickListener(this::addVideo);

        Button buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(this::modifyGuideline);
    }

    private void addImage(View view) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_image, null);

        TextView textViewURLError = dialogView.findViewById(R.id.textViewURLError);
        ImageView imageViewURLError = dialogView.findViewById(R.id.imageViewURLError);
        EditText editTextURL = dialogView.findViewById(R.id.editTextURL);
        editTextURL.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textViewURLError.setVisibility(View.INVISIBLE);
                imageViewURLError.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        Button buttonSelectImageFromDevice = dialogView.findViewById(R.id.buttonSelectImageFromDevice);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setMessage(R.string.guidelines_dialog_message_add_image);
        builder.setPositiveButton(getString(R.string.button_add),
                (dialog, which) -> {
                });
        builder.setNegativeButton(getString(R.string.dialog_negative_cancel), (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        buttonSelectImageFromDevice.setOnClickListener(buttonView -> {
            imagePickerLauncher.launch("image/*");
            dialog.dismiss();
        });

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String url = editTextURL.getText().toString().trim();
            if (!isValidURL(url)) {
                textViewURLError.setVisibility(View.VISIBLE);
                imageViewURLError.setVisibility(View.VISIBLE);
            } else {
                insertNewMultimediaInLayout(url, MultimediaType.IMAGE, newImagesPath, linearLayoutImages);
                dialog.dismiss();
            }
        });
    }

    private void addVideo(View view) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_video, null);

        TextView textViewURLError = dialogView.findViewById(R.id.textViewURLError);
        ImageView imageViewURLError = dialogView.findViewById(R.id.imageViewURLError);
        EditText editTextURL = dialogView.findViewById(R.id.editTextURL);
        editTextURL.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textViewURLError.setVisibility(View.INVISIBLE);
                imageViewURLError.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        Button buttonSelectVideoFromDevice = dialogView.findViewById(R.id.buttonSelectVideoFromDevice);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setMessage(R.string.guidelines_dialog_message_add_video);
        builder.setPositiveButton(getString(R.string.button_add),
                (dialog, which) -> {
                });
        builder.setNegativeButton(getString(R.string.dialog_negative_cancel), (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        buttonSelectVideoFromDevice.setOnClickListener(buttonView -> {
            videoPickerLauncher.launch("video/*");
            dialog.dismiss();
        });

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String url = editTextURL.getText().toString().trim();
            if (!isValidURL(url)) {
                textViewURLError.setVisibility(View.VISIBLE);
                imageViewURLError.setVisibility(View.VISIBLE);
            } else {
                insertNewMultimediaInLayout(url, MultimediaType.VIDEO, newVideosPath, linearLayoutVideos);
                dialog.dismiss();
            }
        });
    }

    private void setMultimediaLayouts() {
        try {
            currentMultimedias = new ArrayList<>(guideline.getMultimedias(this));
            for (Multimedia multimedia : currentMultimedias) {
                ConstraintLayout layoutMultimediaModification;

                layoutMultimediaModification = (ConstraintLayout) LayoutInflater.from(this).inflate(R.layout.layout_multimedia_modification,
                        multimedia.getType().equals(MultimediaType.IMAGE)? linearLayoutImages : linearLayoutVideos, false);

                TextView textViewPath = layoutMultimediaModification.findViewById(R.id.textViewPath);
                textViewPath.setText(multimedia.getPath());

                ImageButton imageButtonRemove = layoutMultimediaModification.findViewById(R.id.imageButtonRemove);
                imageButtonRemove.setOnClickListener(view -> {
                    currentMultimedias.remove(multimedia);
                    ((ViewGroup) layoutMultimediaModification.getParent()).removeView(layoutMultimediaModification);
                });

                textViewPath.setOnClickListener(view -> {
                    if (multimedia.getType().equals(MultimediaType.IMAGE)) {
                        openImage(multimedia.getPath(), imageButtonRemove);
                    } else {
                        openVideo(multimedia.getPath(), imageButtonRemove);
                    }
                });

                if (multimedia.getType().equals(MultimediaType.IMAGE)) {
                    linearLayoutImages.addView(layoutMultimediaModification);
                } else {
                    linearLayoutVideos.addView(layoutMultimediaModification);
                }
            }
        } catch (DBFindException exception) {
            ExceptionManager.advertiseUI(this, exception.getMessage());
        }

        for (String path : newImagesPath)
            insertNewMultimediaInLayout(path, MultimediaType.IMAGE, newImagesPath, linearLayoutImages);
        for (String path : newVideosPath)
            insertNewMultimediaInLayout(path, MultimediaType.VIDEO, newVideosPath, linearLayoutVideos);
    }

    private void insertNewMultimediaInLayout(String path, MultimediaType multimediaType, List<String> newPaths, LinearLayout linearLayout) {
        ConstraintLayout layoutMultimediaModification;

        layoutMultimediaModification = (ConstraintLayout) LayoutInflater.from(this).inflate(R.layout.layout_multimedia_modification, linearLayout, false);

        TextView textViewPath = layoutMultimediaModification.findViewById(R.id.textViewPath);
        textViewPath.setText(path);

        ImageButton imageButtonRemove = layoutMultimediaModification.findViewById(R.id.imageButtonRemove);
        imageButtonRemove.setOnClickListener(view -> {
            newPaths.remove(path);
            ((ViewGroup) layoutMultimediaModification.getParent()).removeView(layoutMultimediaModification);
        });

        textViewPath.setOnClickListener(view -> {
            if (multimediaType.equals(MultimediaType.IMAGE)) {
                openImage(path, imageButtonRemove);
            } else {
                openVideo(path, imageButtonRemove);
            }
        });

        linearLayout.addView(layoutMultimediaModification);
    }

    private void openImage(String path, ImageButton imageButtonRemove) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);

            if (isValidURL(path)) {
                intent.setData(Uri.parse(path));
            } else {
                File file = new File(path);
                Uri uri = FileProvider.getUriForFile(this, "com.javierjordanluque.healthtrackr.file-provider", file);
                intent.setDataAndType(uri, "image/*");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            startActivity(intent);
        } catch (Exception exception) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.guidelines_dialog_message_image_not_found)
                    .setPositiveButton(R.string.dialog_positive_ok, (dialog, id) -> {
                        imageButtonRemove.performClick();
                        dialog.dismiss();
                    });
            builder.create().show();
        }
    }

    private void openVideo(String path, ImageButton imageButtonRemove) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);

            if (isValidURL(path)) {
                intent.setData(Uri.parse(path));
            } else {
                File file = new File(path);
                Uri uri = FileProvider.getUriForFile(this, "com.javierjordanluque.healthtrackr.file-provider", file);
                intent.setDataAndType(uri, "video/*");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            startActivity(intent);
        } catch (Exception exception) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.guidelines_dialog_message_video_not_found)
                    .setPositiveButton(R.string.dialog_positive_ok, (dialog, id) -> {
                        imageButtonRemove.performClick();
                        dialog.dismiss();
                    });
            builder.create().show();
        }
    }

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    result -> {
                        if (result != null) {
                            String path = getPathFromUri(result, MultimediaType.IMAGE);
                            if (path != null) {
                                newImagesPath.add(path);
                                insertNewMultimediaInLayout(path, MultimediaType.IMAGE, newImagesPath, linearLayoutImages);
                            }
                        }
                    });

    private final ActivityResultLauncher<String> videoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    result -> {
                        if (result != null) {
                            String path = getPathFromUri(result, MultimediaType.VIDEO);
                            if (path != null) {
                                newVideosPath.add(path);
                                insertNewMultimediaInLayout(path, MultimediaType.VIDEO, newVideosPath, linearLayoutVideos);
                            }
                        }
                    });

    private String getPathFromUri(Uri uri, MultimediaType multimediaType) {
        String[] projection = { multimediaType.equals(MultimediaType.IMAGE)? MediaStore.Images.Media.DATA : MediaStore.Video.Media.DATA };
        try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(projection[0]);
                return cursor.getString(column_index);
            }
        }

        return null;
    }

    private void modifyGuideline(View view) {
        hideKeyboard(this);

        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        boolean validTitle = isValidTitle(title);
        boolean validDescription = isValidDescription(description);

        if (!validTitle || !validDescription) {
            if (!validTitle)
                layoutTitle.setError(getString(R.string.error_invalid_guideline_title));
            if (!validDescription)
                layoutDescription.setError(getString(R.string.error_invalid_guideline_description));

            return;
        }

        if (description.isEmpty())
            description = null;

        int numOrder = getNumOrderFromSpinner();

        List<Multimedia> multimediasToDelete = new ArrayList<>();
        try {
            for (Multimedia multimedia : guideline.getMultimedias(this)) {
                if (!currentMultimedias.contains(multimedia))
                    multimediasToDelete.add(multimedia);
            }
        } catch (DBFindException exception) {
            ExceptionManager.advertiseUI(this, exception.getMessage());
        }

        showModifyGuidelineConfirmationDialog(title, description, numOrder, multimediasToDelete);
    }

    private void showModifyGuidelineConfirmationDialog(String title, String description, int numOrder, List<Multimedia> multimediasToDelete) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.dialog_message_save))
                .setPositiveButton(getString(R.string.button_save), (dialog, id) -> {
                    try {
                        for (Multimedia multimedia : multimediasToDelete)
                            guideline.removeMultimedia(this, multimedia);
                        for (String path : newImagesPath)
                            new Multimedia(this, guideline, MultimediaType.IMAGE, path);
                        for (String path : newVideosPath)
                            new Multimedia(this, guideline, MultimediaType.VIDEO, path);

                        guideline.modifyGuideline(this, title, description, numOrder);

                        Toast.makeText(this, getString(R.string.toast_confirmation_save), Toast.LENGTH_SHORT).show();
                        finish();
                    } catch (DBDeleteException | DBInsertException | DBFindException | DBUpdateException exception) {
                        ExceptionManager.advertiseUI(this, exception.getMessage());
                    }
                })
                .setNegativeButton(getString(R.string.dialog_negative_cancel), (dialog, id) -> dialog.dismiss());
        builder.show();
    }

    private int getNumOrderFromSpinner() {
        return Integer.parseInt(spinnerNumOrder.getSelectedItem().toString());
    }

    private void configureNumOrderSpinner() {
        spinnerNumOrder = findViewById(R.id.spinnerNumOrder);

        try {
            List<Guideline> guidelines = treatment.getGuidelines(this);

            String[] numOrderOptions = new String[guidelines.size()];
            for (int i = 0; i < numOrderOptions.length; i++)
                numOrderOptions[i] = String.valueOf(i + 1);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, numOrderOptions);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerNumOrder.setAdapter(adapter);
            spinnerNumOrder.setSelection(guideline.getNumOrder() - 1);
        } catch (DBFindException exception) {
            ExceptionManager.advertiseUI(this, exception.getMessage());
        }
    }

    private boolean isValidTitle(String title) {
        return !title.isEmpty();
    }

    private boolean isValidDescription(String description) {
        return description.length() <= 300;
    }

    private boolean isValidURL(String url) {
        return !url.isEmpty() && (url.startsWith("https://") || url.startsWith("http://"));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(STATE_IMAGES_PATH, new ArrayList<>(newImagesPath));
        outState.putStringArrayList(STATE_VIDEOS_PATH, new ArrayList<>(newVideosPath));
    }

    @Override
    protected int getMenu() {
        return R.menu.toolbar_menu;
    }
}