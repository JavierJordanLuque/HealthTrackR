package com.javierjordanluque.healthtrackr.ui.treatments.guidelines;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.textfield.TextInputLayout;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.Guideline;
import com.javierjordanluque.healthtrackr.models.Multimedia;
import com.javierjordanluque.healthtrackr.models.Treatment;
import com.javierjordanluque.healthtrackr.models.enumerations.MultimediaType;
import com.javierjordanluque.healthtrackr.ui.BaseActivity;
import com.javierjordanluque.healthtrackr.util.PermissionManager;
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
    private final List<String> newImageURIStrings = new ArrayList<>();
    private final List<String> newVideoURIStrings = new ArrayList<>();
    private static final String STATE_IMAGES_URI = "state_images_uri";
    private static final String STATE_VIDEOS_URI = "state_videos_uri";
    private AlertDialog addImageDialog;
    private AlertDialog addVideoDialog;
    private MultimediaType onRequestReadMultimediaPermissionMultimediaType;

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
            ArrayList<String> savedImagesURI = savedInstanceState.getStringArrayList(STATE_IMAGES_URI);
            if (savedImagesURI != null)
                newImageURIStrings.addAll(savedImagesURI);

            ArrayList<String> savedVideosURI = savedInstanceState.getStringArrayList(STATE_VIDEOS_URI);
            if (savedVideosURI != null)
                newVideoURIStrings.addAll(savedVideosURI);
        }
        setMultimediaLayouts();

        Button buttonAddImage = findViewById(R.id.buttonAddImage);
        buttonAddImage.setOnClickListener(this::addImage);


        Button buttonAddVideo = findViewById(R.id.buttonAddVideo);
        buttonAddVideo.setOnClickListener(this::addVideo);

        Button buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(this::modifyGuideline);
    }

    private void setMultimediaLayouts() {
        try {
            currentMultimedias = new ArrayList<>(guideline.getMultimedias(this));
            for (Multimedia multimedia : currentMultimedias) {
                ConstraintLayout layoutMultimediaModification;

                layoutMultimediaModification = (ConstraintLayout) LayoutInflater.from(this).inflate(R.layout.layout_multimedia_modification,
                        multimedia.getType().equals(MultimediaType.IMAGE)? linearLayoutImages : linearLayoutVideos, false);

                TextView textViewURI = layoutMultimediaModification.findViewById(R.id.textViewURI);
                textViewURI.setText(multimedia.getUri().toString());

                ImageButton imageButtonRemove = layoutMultimediaModification.findViewById(R.id.imageButtonRemove);
                imageButtonRemove.setOnClickListener(view -> {
                    currentMultimedias.remove(multimedia);
                    ((ViewGroup) layoutMultimediaModification.getParent()).removeView(layoutMultimediaModification);
                });

                textViewURI.setOnClickListener(view -> {
                    if (multimedia.getType().equals(MultimediaType.IMAGE)) {
                        openImage(multimedia.getUri().toString(), imageButtonRemove);
                    } else if (multimedia.getType().equals(MultimediaType.VIDEO)) {
                        openVideo(multimedia.getUri().toString(), imageButtonRemove);
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

        for (String uriString : newImageURIStrings)
            insertNewMultimediaInLayout(uriString, MultimediaType.IMAGE, newImageURIStrings, linearLayoutImages);
        for (String uriString : newVideoURIStrings)
            insertNewMultimediaInLayout(uriString, MultimediaType.VIDEO, newVideoURIStrings, linearLayoutVideos);
    }

    private void insertNewMultimediaInLayout(String uriString, MultimediaType multimediaType, List<String> newURIStrings, LinearLayout linearLayout) {
        ConstraintLayout layoutMultimediaModification;

        layoutMultimediaModification = (ConstraintLayout) LayoutInflater.from(this).inflate(R.layout.layout_multimedia_modification, linearLayout, false);

        TextView textViewURI = layoutMultimediaModification.findViewById(R.id.textViewURI);
        textViewURI.setText(uriString);

        ImageButton imageButtonRemove = layoutMultimediaModification.findViewById(R.id.imageButtonRemove);
        imageButtonRemove.setOnClickListener(view -> {
            newURIStrings.remove(uriString);
            ((ViewGroup) layoutMultimediaModification.getParent()).removeView(layoutMultimediaModification);
        });

        textViewURI.setOnClickListener(view -> {
            if (multimediaType.equals(MultimediaType.IMAGE)) {
                openImage(uriString, imageButtonRemove);
            } else if (multimediaType.equals(MultimediaType.VIDEO)) {
                openVideo(uriString, imageButtonRemove);
            }
        });

        linearLayout.addView(layoutMultimediaModification);
    }

    private void openImage(String uriString, ImageButton imageButtonRemove) {
        Intent intent = new Intent(Intent.ACTION_VIEW);

        if (isValidURL(uriString)) {
            intent.setData(Uri.parse(uriString));
        } else {
            String filePath = getPathFromUri(Uri.parse(uriString), MultimediaType.IMAGE);
            File file = new File(filePath);

            if (file.exists()) {
                Uri uriForFile = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
                intent.setDataAndType(uriForFile, "image/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.guidelines_dialog_message_image_not_found)
                        .setPositiveButton(R.string.dialog_positive_ok, (dialog, id) -> {
                            imageButtonRemove.performClick();
                            dialog.dismiss();
                        });
                builder.create().show();
            }
        }

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, R.string.guidelines_toast_no_app_open_image, Toast.LENGTH_LONG).show();
        }
    }

    private void openVideo(String uriString, ImageButton imageButtonRemove) {
        Intent intent = new Intent(Intent.ACTION_VIEW);

        if (isValidURL(uriString)) {
            intent.setData(Uri.parse(uriString));
        } else {
            String filePath = getPathFromUri(Uri.parse(uriString), MultimediaType.VIDEO);
            File file = new File(filePath);

            if (file.exists()) {
                Uri uriForFile = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
                intent.setDataAndType(uriForFile, "video/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.guidelines_dialog_message_video_not_found)
                        .setPositiveButton(R.string.dialog_positive_ok, (dialog, id) -> {
                            imageButtonRemove.performClick();
                            dialog.dismiss();
                        });
                builder.create().show();
            }
        }

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, R.string.guidelines_toast_no_app_open_video, Toast.LENGTH_LONG).show();
        }
    }

    private String getPathFromUri(Uri uri, MultimediaType multimediaType) {
        String filePath = null;
        String[] projection = null;

        if (multimediaType.equals(MultimediaType.IMAGE)) {
            projection = new String[]{MediaStore.Images.Media.DATA};
        } else if (multimediaType.equals(MultimediaType.VIDEO)) {
            projection = new String[]{MediaStore.Video.Media.DATA};
        }

        try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = 0;

                if (multimediaType.equals(MultimediaType.IMAGE)) {
                    columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                } else if (multimediaType.equals(MultimediaType.VIDEO)){
                    columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                }
                filePath = cursor.getString(columnIndex);
            }
        }

        return filePath;
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
                        for (String uri : newImageURIStrings)
                            new Multimedia(this, guideline, MultimediaType.IMAGE, Uri.parse(uri));
                        for (String uri : newVideoURIStrings)
                            new Multimedia(this, guideline, MultimediaType.VIDEO, Uri.parse(uri));

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

        addImageDialog = builder.create();

        buttonSelectImageFromDevice.setOnClickListener(buttonView -> {
            if (PermissionManager.hasReadMultimediaPermission(this)) {
                selectImageFromDevice();
            } else {
                onRequestReadMultimediaPermissionMultimediaType = MultimediaType.IMAGE;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PermissionManager.REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE);
            }
        });

        addImageDialog.show();

        addImageDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String url = editTextURL.getText().toString().trim();
            if (!isValidURL(url)) {
                textViewURLError.setVisibility(View.VISIBLE);
                imageViewURLError.setVisibility(View.VISIBLE);
            } else {
                insertNewMultimediaInLayout(url, MultimediaType.IMAGE, newImageURIStrings, linearLayoutImages);
                addImageDialog.dismiss();
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

        addVideoDialog = builder.create();

        buttonSelectVideoFromDevice.setOnClickListener(buttonView -> {
            if (PermissionManager.hasReadMultimediaPermission(this)) {
                selectVideoFromDevice();
            } else {
                onRequestReadMultimediaPermissionMultimediaType = MultimediaType.VIDEO;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PermissionManager.REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE);
            }
        });

        addVideoDialog.show();

        addVideoDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String url = editTextURL.getText().toString().trim();
            if (!isValidURL(url)) {
                textViewURLError.setVisibility(View.VISIBLE);
                imageViewURLError.setVisibility(View.VISIBLE);
            } else {
                insertNewMultimediaInLayout(url, MultimediaType.VIDEO, newVideoURIStrings, linearLayoutVideos);
                addVideoDialog.dismiss();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionManager.REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (onRequestReadMultimediaPermissionMultimediaType.equals(MultimediaType.IMAGE)) {
                    selectImageFromDevice();
                } else if (onRequestReadMultimediaPermissionMultimediaType.equals(MultimediaType.VIDEO)) {
                    selectVideoFromDevice();
                }
            } else {
                showReadMultimediaPermissionDeniedDialog();
            }
        }
    }

    private void selectImageFromDevice() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);

        addImageDialog.dismiss();
    }

    private void selectVideoFromDevice() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        pickVideoLauncher.launch(intent);

        addVideoDialog.dismiss();
    }

    private void showReadMultimediaPermissionDeniedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.guidelines_dialog_denied_read_multimedia_permission))
                .setPositiveButton(getString(R.string.snackbar_action_more), (dialog, id) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                    readMultimediaPermissionLauncher.launch(intent);
                })
                .setNegativeButton(getString(R.string.dialog_negative_cancel), (dialog, id) -> dialog.dismiss());
        builder.show();
    }

    private final ActivityResultLauncher<Intent> readMultimediaPermissionLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            if (onRequestReadMultimediaPermissionMultimediaType.equals(MultimediaType.IMAGE)) {
                selectImageFromDevice();
            } else if (onRequestReadMultimediaPermissionMultimediaType.equals(MultimediaType.VIDEO)) {
                selectVideoFromDevice();
            }
        }
    });

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Uri imageURI = result.getData().getData();
            if (imageURI != null) {
                getContentResolver().takePersistableUriPermission(imageURI, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                String imageUriString = imageURI.toString();
                newImageURIStrings.add(imageUriString);
                insertNewMultimediaInLayout(imageUriString, MultimediaType.IMAGE, newImageURIStrings, linearLayoutImages);
            }
        }
    });

    private final ActivityResultLauncher<Intent> pickVideoLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Uri videoURI = result.getData().getData();
            if (videoURI != null) {
                getContentResolver().takePersistableUriPermission(videoURI, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                String videoURIString = videoURI.toString();
                newVideoURIStrings.add(videoURIString);
                insertNewMultimediaInLayout(videoURIString, MultimediaType.VIDEO, newVideoURIStrings, linearLayoutVideos);
            }
        }
    });

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
        outState.putStringArrayList(STATE_IMAGES_URI, new ArrayList<>(newImageURIStrings));
        outState.putStringArrayList(STATE_VIDEOS_URI, new ArrayList<>(newVideoURIStrings));
    }

    @Override
    protected int getMenu() {
        return R.menu.toolbar_menu;
    }
}
