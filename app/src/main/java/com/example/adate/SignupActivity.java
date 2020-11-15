package com.example.adate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.adate.model.UserModel;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

public class SignupActivity extends AppCompatActivity {

    private static final int PICK_REQUEST = 101;
    private EditText editTextEmail;
    private EditText editTextName;
    private EditText editTextPassword;
    private Button signupButton;
    private ImageView profileImage;
    private Uri imageUri;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference mUser;
    private StorageReference profileImageRef;


    public SignupActivity() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editTextEmail = findViewById(R.id.signupActivity_et_email);
        editTextName = findViewById(R.id.signupActivity_et_name);
        editTextPassword = findViewById(R.id.signupActivity_et_password);
        signupButton = findViewById(R.id.signupActivity_button_signup);
        profileImage = findViewById(R.id.signupActivity_image_profile);

        mUser = FirebaseDatabase.getInstance().getReference().child("users");
        firebaseAuth = FirebaseAuth.getInstance();
        profileImageRef = FirebaseStorage.getInstance().getReference().child("profileImages");

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_REQUEST);
            }
        });
        
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString();
                String name = editTextName.getText().toString();
                String password = editTextPassword.getText().toString();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(name) || TextUtils.isEmpty(password) || imageUri == null) {
                    Toast.makeText(SignupActivity.this, "Please Fill All Fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                signUp(email, name, password);

            }
        });
    }

    public void signUp(String email, String name, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            String uid = task.getResult().getUser().getUid();

                            profileImageRef.child(uid).putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if(task.isSuccessful()) {
                                        profileImageRef.child(uid).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                String profileImageUrl = uri.toString();
                                                UserModel userModel = new UserModel();
                                                userModel.userName = name;
                                                userModel.profileImageUrl = profileImageUrl;
                                                userModel.uid = firebaseAuth.getCurrentUser().getUid();
                                                mUser.child(uid).setValue(userModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        SignupActivity.this.finish();
                                                    }
                                                });

                                            }
                                        });

                                    }

                                }
                            });

                            Toast.makeText(SignupActivity.this, "user created",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignupActivity.this, MainActivity.class));
                        } else {
                            Toast.makeText(SignupActivity.this, "failed " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == PICK_REQUEST && resultCode == RESULT_OK) {
            imageUri = data.getData();//이미지 경로
            profileImage.setImageURI(imageUri);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}