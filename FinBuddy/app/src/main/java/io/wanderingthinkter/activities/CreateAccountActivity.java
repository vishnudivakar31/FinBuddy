package io.wanderingthinkter.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.transition.Slide;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import io.wanderingthinkter.R;
import io.wanderingthinkter.models.CurrentUser;
import io.wanderingthinkter.models.UserModel;

import static android.view.Gravity.RIGHT;
import static io.wanderingthinkter.util.Constants.IMAGE_STORAGE;
import static io.wanderingthinkter.util.Constants.USER_COLLECTION;

public class CreateAccountActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int CAMERA_ACTION_CODE = 0;
    private static final int GALLERY_ACTION_CODE = 1;
    private ImageView profilePictureButton;
    private EditText nameET, emailET, passwordET, confirmPasswordEt;
    private String imageUrl;
    private AlertDialog alertDialog;
    private ProgressBar progressBar;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection(USER_COLLECTION);
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAnimation();
        setContentView(R.layout.activity_create_account_acitivity);

        profilePictureButton = findViewById(R.id.create_account_profile_picture);
        nameET = findViewById(R.id.create_account_name);
        emailET = findViewById(R.id.create_account_email);
        passwordET = findViewById(R.id.create_account_password);
        confirmPasswordEt = findViewById(R.id.create_account_confirm_password);
        Button createAccountBtn = findViewById(R.id.create_account_button);
        progressBar = findViewById(R.id.create_account_progressbar);

        profilePictureButton.setOnClickListener(this);
        createAccountBtn.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = auth.getCurrentUser();
    }

    @SuppressLint("RtlHardcoded")
    public void setAnimation() {
        Slide slide = new Slide();
        slide.setSlideEdge(RIGHT);
        slide.setDuration(400);
        slide.setInterpolator(new DecelerateInterpolator());
        getWindow().setExitTransition(slide);
        getWindow().setEnterTransition(slide);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.create_account_button:
                createAccount();
                break;
            case R.id.create_account_profile_picture:
                setAccountProfilePicture();
                break;
            case R.id.image_capture_camera:
                getImageFromCamera();
                break;
            case R.id.image_capture_gallery:
                getImageFromGallery();
                break;
        }
    }

    private void getImageFromGallery() {
        Intent pickPicture = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPicture, GALLERY_ACTION_CODE);
        alertDialog.dismiss();
    }

    private void getImageFromCamera() {
        Intent cameraPicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(cameraPicture.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraPicture, CAMERA_ACTION_CODE);
        }
        alertDialog.dismiss();
    }

    private void setAccountProfilePicture() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(CreateAccountActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.image_capture_layout, null);
        view.findViewById(R.id.image_capture_camera).setOnClickListener(this);
        view.findViewById(R.id.image_capture_gallery).setOnClickListener(this);
        dialogBuilder.setView(view)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void createAccount() {
        String username = nameET.getText().toString().trim();
        String email = emailET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();
        String confirmPassword = confirmPasswordEt.getText().toString().trim();
        if(!TextUtils.isEmpty(username) && !TextUtils.isEmpty(email) &&
                !TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirmPassword)) {
            if(!password.equals(confirmPassword)) {
                showSnackBar(R.string.password_mismatch);
                return;
            }
            if(TextUtils.isEmpty(imageUrl)) {
                createAccountWithEmailAndPassword(username, email, password, null);
            } else {
                createAccountWithImage(username, email, password, imageUrl);
            }
        } else {
            showSnackBar(R.string.all_fields_are_mandatory);
        }
    }

    private void createAccountWithImage(final String username, final String email, final String password, String imageUrl) {
        progressBar.setVisibility(View.VISIBLE);
        if(!TextUtils.isEmpty(imageUrl)) {
            final StorageReference filePath = storageReference
                    .child(IMAGE_STORAGE)
                    .child("my_image_" + Timestamp.now().getNanoseconds());
            filePath.putFile(Uri.parse(imageUrl))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    createAccountWithEmailAndPassword(username, email,
                                            password, uri.toString());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    showSnackBar(R.string.error_message);
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            showSnackBar(R.string.error_message);
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        }
    }

    private void createAccountWithEmailAndPassword(final String username, String email, String password, String imageUrl) {
        progressBar.setVisibility(View.VISIBLE);
        final UserModel userModel = new UserModel(username, email, imageUrl, Timestamp.now());
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            currentUser = auth.getCurrentUser();
                            assert currentUser != null;
                            userModel.setUserId(currentUser.getUid());
                            collectionReference
                                    .add(userModel)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            CurrentUser user = CurrentUser.getInstance();
                                            user.setUserId(currentUser.getUid());
                                            user.setUsername(username);
                                            Intent intent = new Intent(CreateAccountActivity.this,
                                                    HomeActivity.class);
                                            ActivityOptions options =
                                                    ActivityOptions.makeSceneTransitionAnimation(CreateAccountActivity.this);
                                            startActivity(intent,options.toBundle());
                                            progressBar.setVisibility(View.GONE);
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            showSnackBar(R.string.error_message);
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    });
                        } else {
                            showSnackBar(R.string.error_message);
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showSnackBar(R.string.error_message);
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }


    private void showSnackBar(int textID) {
        Snackbar.make(findViewById(R.id.create_account_activity), textID,
                Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GALLERY_ACTION_CODE:
                    assert data != null;
                    Uri imageUri = data.getData();
                    profilePictureButton.setImageURI(imageUri);
                    assert imageUri != null;
                    imageUrl = imageUri.toString();
                    break;
                case CAMERA_ACTION_CODE:
                    assert data != null;
                    Bundle extras = data.getExtras();
                    assert extras != null;
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    profilePictureButton.setImageBitmap(imageBitmap);
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    assert imageBitmap != null;
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    imageUrl = MediaStore.Images.Media.insertImage(getContentResolver(),
                            imageBitmap, "my_image" + Timestamp.now().getNanoseconds(), null);
            }
        }
    }
}
