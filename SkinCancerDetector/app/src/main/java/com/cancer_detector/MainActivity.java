package com.cancer_detector;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import androidx.appcompat.app.AlertDialog;

import com.cancer_detector.interfaces.ApiConfig;
import com.cancer_detector.managers.AppConfig;
import com.cancer_detector.models.response_model;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


import kotlin.jvm.internal.Intrinsics;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    Uri picUri;

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    View loader;
    Button buttonGet;

    private final static int ALL_PERMISSIONS_RESULT = 107;
    private final static int IMAGE_RESULT = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loader = findViewById(R.id.loader);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.FROYO)
            @Override
            public void onClick(View view) {
                startActivityForResult(getPickImageChooserIntent(), IMAGE_RESULT);
            }
        });


        permissions.add(CAMERA);
        permissions.add(WRITE_EXTERNAL_STORAGE);
        permissions.add(READ_EXTERNAL_STORAGE);
        permissionsToRequest = findUnAskedPermissions(permissions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    public Intent getPickImageChooserIntent() {
        Uri outputFileUri = getCaptureImageOutputUri();
        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = getPackageManager();
        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

        Intent mainIntent = allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            Log.v("Intent is:", intent.toString());
            if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);

        //TODO: use an "if" statement with names from logv
//        allIntents.remove(1);
//        allIntents.remove(1);
//        allIntents.remove(2);

        Intent chooserIntent = Intent.createChooser(mainIntent, "Select source");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }


    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
//        File getImage = getExternalFilesDir("");
        File getImage = getExternalFilesDir("image/*");
        if (getImage != null) {
//            TODO timestampname
//            getoutputimage(getImage);
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "profile.png"));
        }
        return outputFileUri;
    }


    private static File getoutputimage(File getImage){
        String x = "x";
        return new File(getImage.getPath() + File.separator +
                "IMG_"+ x + ".jpg");
    }
    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            ImageView imageView = findViewById(R.id.imageView);
            if (requestCode == IMAGE_RESULT) {
                String filePath = getImageFilePath(data);
                if (filePath != null) {
                    Bitmap selectedImage = BitmapFactory.decodeFile(filePath);
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setImageBitmap(selectedImage);
                    //here TODO
                    Log.v("THIS IS 0", filePath);
                    uploadFile(filePath);
                }
            }
        }
    }

    private void uploadFile(String filePath) {
        loader.setVisibility(View.VISIBLE);
        // Map is used to multipart the file using okhttp3.RequestBody
        Log.v("THIS IS", filePath);
        File file = new File(filePath);
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("image", file.getName(), requestBody);
        RequestBody filename = RequestBody.create(MediaType.parse("multipart/form-data"), file.getName());
        ApiConfig getResponse = AppConfig.getRetrofit().create(ApiConfig.class);
        Call<response_model> call = getResponse.uploadFile(fileToUpload, filename);
        Log.v("upload", "upload");
        call.enqueue((Callback<response_model>)(new Callback<response_model>() {
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                Intrinsics.checkParameterIsNotNull(call, "call");
                Intrinsics.checkParameterIsNotNull(response, "response");
                if (response.isSuccessful()) {
                    Log.v("upload", "response succ");
                    response_model serverResponse = (response_model) response.body();
                    if (serverResponse.getPredictions()!=null) {
                        loader.setVisibility(View.GONE);
//                        Toast.makeText(getApplicationContext(), serverResponse.getPredictions().get(0).getLabel(), Toast.LENGTH_SHORT).show();
                        String result1 = serverResponse.getPredictions().get(0).getProbability().toString();
                        String result2 = serverResponse.getPredictions().get(1).getProbability().toString();
                        float result_1 = Float.parseFloat(result1) * 100;
                        float result_2 = Float.parseFloat(result2) * 100;
                        DecimalFormat df = new DecimalFormat("#.##");
                        ((TextView) findViewById(R.id.output_text3)).setVisibility(View.GONE);
                        ((ImageView) findViewById(R.id.arrow)).setVisibility(View.GONE);
                        if(result_1 > result_2){
                            ((TextView) findViewById(R.id.output_text1)).setText("Melanoma Probability: " + df.format(result_1) + "%");
                            ((TextView) findViewById(R.id.output_text1)).setVisibility(View.VISIBLE);
                            ((TextView) findViewById(R.id.output_text2)).setText("You should go see a doctor!");
                            ((TextView) findViewById(R.id.output_text2)).setTextColor(Color.parseColor("#e60000"));
                        }
                        else{
                            ((TextView) findViewById(R.id.output_text1)).setText("Non-Melanoma Probability:" + df.format(result_2) + "%");
                            ((TextView) findViewById(R.id.output_text1)).setVisibility(View.VISIBLE);
                            ((TextView) findViewById(R.id.output_text2)).setText("You're probably fine!");
                            ((TextView) findViewById(R.id.output_text2)).setTextColor(Color.parseColor("#33cc33"));
                        }
                        ((TextView) findViewById(R.id.output_text2)).setVisibility(View.VISIBLE);
//                        ((TextView) findViewById(R.id.output_text1)).setText("Melanoma Probability: " + df.format(result_1) + "%");
//                        ((TextView) findViewById(R.id.output_text1)).setVisibility(View.VISIBLE);
//                        ((TextView) findViewById(R.id.output_text2)).setText("You're probably fine!");
//                        ((TextView) findViewById(R.id.output_text2)).setVisibility(View.VISIBLE);
//                        ((TextView) findViewById(R.id.output_text3)).setText("Non-Melanoma Probability:" + df.format(result_2) + "%");
//                        ((TextView) findViewById(R.id.output_text3)).setVisibility(View.VISIBLE);
//                        ((TextView) findViewById(R.id.output_text4)).setText(df.format(result_2) + "%");
//                        ((TextView) findViewById(R.id.output_text4)).setVisibility(View.VISIBLE);


                    } else {
                        loader.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "response null",Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.v("Response 1", "wasnt successfull");
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.v("Response 2", "wasnt successfull");
            }
        }));
    }

    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    private String getImageFromFilePath(Intent data) {
        boolean isCamera = data == null || data.getData() == null;
        if (isCamera) return getCaptureImageOutputUri().getPath();
        else return getPathFromURI(data.getData());
    }

    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    public String getImageFilePath(Intent data) {
        return getImageFromFilePath(data);
    }

    private String getPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("pic_uri", picUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // get the file url
        picUri = savedInstanceState.getParcelable("pic_uri");
    }

    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();
        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                }

                break;
        }


    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}