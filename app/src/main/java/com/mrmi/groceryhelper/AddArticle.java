package com.mrmi.groceryhelper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.joestelmach.natty.Parser;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddArticle extends AppCompatActivity {

    //Request codes for intents
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;

    private String[] cameraPermission, storagePermission;

    private CropImageView cropImageView;
    private TextView actualDetectedDateTextView;

    private Bitmap cropped; //Cropped image bitmap

    private ArticleList articleListClass;

    private String datePattern; //Selected date pattern (MM/dd or dd/MM)

    private Uri finalUri;

    private EditText articleName;
    private TextView articleCategory;
    private Button detectButton;
    private ImageButton cameraButton, galleryButton, pickDateButton, cropButton, saveArticleButton;

    private Spinner articleCategorySpinner;

    private ArrayList<String> userCategoryList; //List of categories which the user has manually made

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_article);

        Settings.loadLocale(this);

        initialiseViews();
        initialiseObjects();
        initialiseListeners();
    }

    //Launch main activity on back pressed
    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
    }

    private void initialiseViews() {
        detectButton = findViewById(R.id.detectButton);
        actualDetectedDateTextView = findViewById(R.id.actualDetectedDateTextView);
        cropImageView = findViewById(R.id.cropImageView);
        cameraButton = findViewById(R.id.cameraButton);
        galleryButton = findViewById(R.id.galleryButton);
        pickDateButton = findViewById(R.id.pickDateButton);
        cropButton = findViewById(R.id.cropButton);
        saveArticleButton = findViewById(R.id.saveButton);
        articleName = findViewById(R.id.articleNameEditText);
        articleCategory = findViewById(R.id.articleCategoryTextView);
        articleCategorySpinner = findViewById(R.id.articleCategorySpinner);
    }

    private void initialiseObjects() {
        cameraPermission = new String[]{Manifest.permission.CAMERA};
        storagePermission = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        articleListClass = new ArticleList(this);

        //Get the date pattern
        datePattern = articleListClass.getDatePattern();

        sharedPreferences = this.getSharedPreferences("Shared preferences", MODE_PRIVATE);

        List<String> allCategories = Arrays.asList(this.getResources().getStringArray(R.array.category_names));
        loadCategoryList();
        allCategories.addAll(userCategoryList);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, allCategories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        articleCategorySpinner.setAdapter(adapter);
        articleCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                articleCategory.setText(parentView.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) { }
        });
    }

    private void loadCategoryList() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString("Categories", null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        userCategoryList = gson.fromJson(json, type);

        if (userCategoryList == null) {
            userCategoryList = new ArrayList<>();
        }
    }

    private void initialiseListeners() {
        //Capture the image using the camera
        cameraButton.setOnClickListener(v -> {
            if (!hasCameraPermission()) {
                requestCameraPermission();
            } else {
                takePictureUsingCamera();
            }
        });

        //Select the image from the gallery
        galleryButton.setOnClickListener(v -> {
            if (!hasStoragePermission()) {
                requestStoragePermission();
            } else {
                pickImageFromGallery();
            }
        });

        //Crops the image in the current activity
        cropButton.setOnClickListener(v -> {
            cropped = cropImageView.getCroppedImage();
            if (cropped != null)
                cropImageView.setImageBitmap(cropped);
        });

        //Manually set the article's expiration date
        pickDateButton.setOnClickListener(v -> showDatePickDialog());

        //Detects text from the image
        detectButton.setOnClickListener(v -> detectTextFromImage());

        //Add an article to the articles ArrayList
        saveArticleButton.setOnClickListener(v -> {
            //if (detectedDateTextView.getText().toString().equals(getString((R.string.detected_date)))) {
            if(actualDetectedDateTextView.getText().toString().equals("")) {
                Toast.makeText(this, getString(R.string.toast_select_article_expiration_date), Toast.LENGTH_LONG).show();
            } else if (articleName.getText().toString().equals("")) {
                Toast.makeText(this, getString(R.string.toast_input_article_name), Toast.LENGTH_LONG).show();
            } else if (articleCategory.getText().toString().equals("")) {
                Toast.makeText(this, getString(R.string.toast_select_article_category), Toast.LENGTH_LONG).show();
            } else {
                try {
                    //Always save the english category value in local storage in order to simplify translation:
                    //Get the value in the category_values array which has the same index as the inputted category in the current locale
                    List<String> categoryValues = Arrays.asList(this.getResources().getStringArray(R.array.category_values));

                    String articleCategoryVal = categoryValues.get(Arrays.asList(this.getResources().getStringArray(R.array.category_names)).indexOf((articleCategory.getText().toString())));
                    articleListClass.addArticleToList(new Article(articleName.getText().toString(), actualDetectedDateTextView.getText().toString(), articleCategoryVal));

                    //Go back to the main activity after adding the article
                    startActivity(new Intent(this, MainActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean hasStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED)
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }

    //Starts an intent to pick an image from the gallery
    private void pickImageFromGallery() {
        //Intent to pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void takePictureUsingCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                finalUri = FileProvider.getUriForFile(this, "com.mrmi.groceryhelper.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, finalUri);
                startActivityForResult(takePictureIntent, IMAGE_PICK_CAMERA_CODE);
            }
        }
    }

    //Creates a unique temporary image file
    private File createImageFile() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    //Handles permission requests
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted) {
                        takePictureUsingCamera();
                    } else {
                        Toast.makeText(this, getString(R.string.toast_camera_denied), Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean readStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (readStorageAccepted) {
                        pickImageFromGallery();
                    } else {
                        Toast.makeText(this,  getString(R.string.toast_storage_denied), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            //If the user took a picture using the camera
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                cropImageView.setImageUriAsync(finalUri);
            }
            //If the user picked an image from the gallery
            else if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                try {
                    //Get the Uri of the picked image
                    Uri imageUri = data.getData();

                    //Make a bitmap from the given Uri
                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                    //Set the crop image view's bitmap to the selected bitmap
                    cropImageView.setImageBitmap(imageBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //Shows the date picker dialog when the user wants to manually input the article's expiration date
    private void showDatePickDialog() {
        Calendar today = Calendar.getInstance();
        int tDay = today.get(Calendar.DAY_OF_MONTH), tMonth = today.get(Calendar.MONTH), tYear = today.get(Calendar.YEAR);

        //Because int months are indexed starting at 0 (January is 0)
        //Add zeroes if necessary to month and day values so the function DetectDateTextFromString() works properly
        DatePickerDialog dpd = new DatePickerDialog(this, (view, year, month, day) -> {
            month++; //Because int months are indexed starting at 0 (January is 0)

            //Add zeroes if necessary to month and day values so the function DetectDateTextFromString() works properly
            String dayStr = String.valueOf(day);
            if (day < 10)
                dayStr = '0' + dayStr;
            String monthStr = String.valueOf(month);
            if (month < 10)
                monthStr = '0' + monthStr;

            //DetectDateTextFromString(dayStr + "/" + monthStr + "/" + year);
            detectDateTextFromString(monthStr + "/" + dayStr + "/" + year);
            System.out.println("[MRMI]: Picked " + dayStr + "/" + monthStr + "/" + year);
        }, tDay, tMonth, tYear);
        dpd.show();
        dpd.updateDate(tYear, tMonth, tDay);
    }

    //Detects texts from cropped's bitmap and calls DetectDateTextFromString() for the detected text
    private void detectTextFromImage() {
        //Safety check, can't detect text on a null image
        if (cropped != null) {
            TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();

            if (!recognizer.isOperational()) {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    Frame frame = new Frame.Builder().setBitmap(cropped).build();
                    SparseArray<TextBlock> items = recognizer.detect(frame);

                    StringBuilder str = new StringBuilder();

                    //Add all detected TextBlocks' values to the String
                    for (int i = 0; i < items.size(); ++i) {
                        TextBlock currentItem = items.valueAt(i);
                        str.append(currentItem.getValue());
                        str.append("\n");
                    }

                    //Because natty can't detect dates that use the format 03.10.2003. (which use dots), convert all . to / so natty can detect 03/10/ 2003
                    System.out.println("[MRMI]: String being detected: " + str);
                    str = new StringBuilder(str.toString().replace(".", "/"));
                    System.out.println("[MRMI]: String being detected: " + str);

                    //If the user has selected the dd/MM date pattern, convert the date string into the MM/dd pattern so that the natty API can read it correctly
                    if (datePattern.equals("dd/MM")) {
                        //Split the date into 3 parts (month, day, year)
                        String[] splitDate = str.toString().split("/");
                        str = new StringBuilder(splitDate[1] + "/" + splitDate[0] + "/" + splitDate[2]);

                        for (String part : splitDate) {
                            System.out.println("[MRMI]: Part: " + part);
                        }
                        System.out.println("[MRMI]: String being detected: " + str);
                    }

                    detectDateTextFromString(str.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, getString(R.string.toast_date_fail), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //Detects a date from a String using the natty library
    @SuppressLint("SimpleDateFormat")
    private void detectDateTextFromString(String str) {
        //Parse all dates from a string into a list using natty's parser
        try {
            List<Date> dates = new Parser().parse(str).get(0).getDates();

            //Get the first parsed date (expiration date hopefully)
            Date expirationDate = dates.get(0);
            System.out.println("[MRMI]: Expiration date: " + expirationDate);

            //String which will be used in detectedDateTextView to display the detected date
            String finalDetectedDate;
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf;
            if (datePattern.equals("dd/MM")) {
                sdf = new SimpleDateFormat("dd/MM/yyyy");
            } else {
                sdf = new SimpleDateFormat("MM/dd/yyyy");
            }
            finalDetectedDate = sdf.format(expirationDate);

            actualDetectedDateTextView.setText(finalDetectedDate);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.toast_date_fail), Toast.LENGTH_SHORT).show();
        }
    }
}