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
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.joestelmach.natty.Parser;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AddArticle extends AppCompatActivity {

    //Request codes for intents
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;

    private String[] cameraPermission, storagePermission; //Required permissions

    private CropImageView cropImageView;
    private TextView detectedDateTextView;

    private Bitmap cropped; //Cropped image bitmap

    private ArticleList articleListClass;

    private String datePattern; //Selected date pattern (MM/dd or dd/MM)

    private Uri finalUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_article);

        //Reference the required buttons, text views, edit texts and permissions
        ImageButton cameraButton = findViewById(R.id.cameraButton), galleryButton = findViewById(R.id.galleryButton), pickDateButton = findViewById(R.id.pickDateButton), cropButton = findViewById(R.id.cropButton);
        Button saveArticleButton = findViewById(R.id.saveButton), detectButton = findViewById(R.id.detectButton);

        detectedDateTextView = findViewById(R.id.detectedDateTextView);
        detectedDateTextView.setText("Detected date: ");
        cropImageView = findViewById(R.id.cropImageView);

        cameraPermission = new String[]{Manifest.permission.CAMERA};
        storagePermission = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        final EditText articleName = findViewById(R.id.articleNameEditText);

        articleListClass = new ArticleList(this);

        //Get the date pattern
        datePattern = articleListClass.getDatePattern();

        //Capture the image using the camera
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!hasCameraPermission()) {
                    requestCameraPermission();
                } else {
                    takePictureUsingCamera();
                }
            }
        });

        //Select the image from the gallery
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!hasStoragePermission()) {
                    requestStoragePermission();
                } else {
                    pickImageFromGallery();
                }
            }
        });

        //Crops the image in the current activity
        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropped = cropImageView.getCroppedImage();
                if (cropped != null)
                    cropImageView.setImageBitmap(cropped);
            }
        });

        //Manually set the article's expiration date
        pickDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickDialog();
            }
        });

        //Detects text from the image
        detectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectTextFromImage();
            }
        });

        //Add an article to the articles ArrayList
        saveArticleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (detectedDateTextView.getText().toString().equals("Detected date: ")) {
                    Toast.makeText(AddArticle.this, "Please input the article's expiration date", Toast.LENGTH_LONG).show();
                } else if (articleName.getText().toString().equals("")) {
                    Toast.makeText(AddArticle.this, "Please input the article's name", Toast.LENGTH_LONG).show();
                } else {
                    try {
                        articleListClass.addArticleToList(new Article(articleName.getText().toString(), detectedDateTextView.getText().toString().substring(15)));

                        //Go back to the main activity after adding the article
                        startActivity(new Intent(AddArticle.this, MainActivity.class));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted) {
                        takePictureUsingCamera();
                    } else {
                        Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean readStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (readStorageAccepted) {
                        pickImageFromGallery();
                    } else {
                        Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
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
        DatePickerDialog dpd = new DatePickerDialog(AddArticle.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
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
            }
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
                    Toast.makeText(this, "Unable to detect date", Toast.LENGTH_SHORT).show();
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

            detectedDateTextView.setText("Detected date: " + finalDetectedDate);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to detect date from image", Toast.LENGTH_SHORT).show();
        }
    }
}