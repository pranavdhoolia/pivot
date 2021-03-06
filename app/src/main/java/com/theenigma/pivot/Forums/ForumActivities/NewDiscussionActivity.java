package com.theenigma.pivot.Forums.ForumActivities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.theenigma.pivot.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;
import io.reactivex.annotations.NonNull;

import static com.theenigma.pivot.Forums.ForumFragment.collectionReference;
import static com.theenigma.pivot.Forums.ForumFragment.i_d;


public class NewDiscussionActivity extends AppCompatActivity {
    private ImageView setUpImage;
    private Uri postImageUri = null;
    private Button newPostBtn;
    private EditText editText;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String current_user_id;
    private Bitmap compressedImageFile;
    private String downloadUrl;
    private TextView discuss_txt;
    private String Category;
    private ProgressDialog progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_discussion);

        Animation fade = AnimationUtils.loadAnimation(this, R.anim.fade_scale);

        setUpImage = findViewById(R.id.new_post_image);
        discuss_txt = findViewById(R.id.discuss_text);

        discuss_txt.setAnimation(fade);

        newPostBtn = findViewById(R.id.post_btn);
        editText = findViewById(R.id.topic);
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        //SET PROGRESS DIALOG.
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);//you can cancel it by pressing back button
        progressBar.setMessage("File UPLOADING ...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setMax(100);//sets the maximum value 100

        Intent intent = getIntent();
        Category = intent.getStringExtra("Category");
        i_d = intent.getStringExtra("ID");


        current_user_id = firebaseAuth.getCurrentUser().getUid();
        //if the build if it is greater than Marshmellow. If it is permission is asked from the user to access storage
        setUpImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (ContextCompat.checkSelfPermission(NewDiscussionActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        //if permission not there permission is asked
                        Toast.makeText(NewDiscussionActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(NewDiscussionActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                    } else {

                        BringImagePicker();

                    }

                } else {

                    BringImagePicker();

                }

            }

        });

        //data is stored in storage folder thread image
        // If the image is put successfully.
        // Details of the image. The upload uri, user uid, timestamp etc are stored in a collection thread in firestore
        //Compressed image is stored

        newPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String content = editText.getText().toString();

                if (!TextUtils.isEmpty(content) && postImageUri != null) {

                    final String randomName = UUID.randomUUID().toString();

                    progressBar.setProgress(0);
                    progressBar.show();

                    // PHOTO UPLOAD
                    File newImageFile = new File(postImageUri.getPath());
                    try {

                        compressedImageFile = new Compressor(NewDiscussionActivity.this)
                                .setMaxHeight(720)
                                .setMaxWidth(720)
                                .setQuality(50)
                                .compressToBitmap(newImageFile);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] imageData = baos.toByteArray();

                    // PHOTO UPLOAD
                    final StorageReference FilePath = storageReference.child("post_images").child(randomName + ".jpg");
                    FilePath.putBytes(imageData).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {


                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                Toast.makeText(NewDiscussionActivity.this, "Cant Upload", Toast.LENGTH_SHORT).show();
                            }

                            return FilePath.getDownloadUrl();
                        }
                    })

                            .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    downloadUrl = task.getResult().toString();
                                    Map<String, Object> postMap = new HashMap<>();
                                    postMap.put("image_url", downloadUrl);
                                    postMap.put("content", content);
                                    postMap.put("user_id", current_user_id);
                                    postMap.put("timestamp", FieldValue.serverTimestamp());
                                    postMap.put("question", 0);


                                    if (Category.equals("General") || Category.equals("Alumni")) {
                                        Log.i("LOL", "SUCC");
                                        collectionReference = firebaseFirestore.collection("General").document(Category).collection("Posts");
                                    } else {
                                        Log.i("LOL", "SUC");
                                        collectionReference = firebaseFirestore.collection("Specific").document(i_d).collection("Subjects").document(Category).collection("Posts");

                                    }


                                    collectionReference.add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override

                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            if (task.isSuccessful()) {
                                                finish();
                                                Toast.makeText(NewDiscussionActivity.this, "Post was added", Toast.LENGTH_LONG).show();
                                                //     Intent mainIntent = new Intent(getApplicationContext(), ForumFragment.class);
                                                progressBar.setProgress(100);
                                                progressBar.hide();
                                                //     startActivity(mainIntent);
                                                //    finish();

                                            } else {
                                                Toast.makeText(NewDiscussionActivity.this, "Post was not added", Toast.LENGTH_LONG).show();

                                            }

                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(NewDiscussionActivity.this, "Something went wrong.", Toast.LENGTH_LONG).show();

                                    progressBar.hide();
                                }
                            });
                }
                else
                {
                    if(!TextUtils.isEmpty(content))
                    Toast.makeText(NewDiscussionActivity.this, "Enter Text", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(NewDiscussionActivity.this, "Upload an image", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private void BringImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMinCropResultSize(512, 512)
                .setAspectRatio(1, 1)
                .start(NewDiscussionActivity.this);
    }

    //used to get data from another app's activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                //ones the image is returned the URI is stored in the memory.
                postImageUri = result.getUri();
                setUpImage.setImageURI(postImageUri);
                //setting the uri to our image
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }
    }
}
