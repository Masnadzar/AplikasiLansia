package com.alya.aplikasilansia;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.ui.AppBarConfiguration;

import com.alya.aplikasilansia.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private AppBarConfiguration appBarConfiguration;
    private ActivityLoginBinding binding;
    private EditText emailInput, passwordInput;
    private ImageView viewPasswordBtn;
    private Button loginBtn, loginGoogleBtn;
    private TextView createAccBtn, forgotPassBtn;
    private FirebaseAuth mAuth;
    private static final String TAG = "LoginActivity";
    private LoginViewModel loginViewModel;
    private RegisterViewModel registerViewModel;
    GoogleSignInClient mGoogleSignInClient;
//    private static final String TAG = "SignInActivityGoogle";
    GoogleSignInClient mGoogleSignInClien;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.server_client_id)) // Ensure this is your Web Client ID
                .build();
//                .requestScopes(new Scope(Scopes.PROFILE)) // Request profile scope
//                .requestScopes(new Scope("https://www.googleapis.com/auth/userinfo.profile"))


        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

//        mGoogleSignInClient = new GoogleSignInClient.Builder(this)
//                .enableAutoManage(this, this)
//                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
//                .build();

        mAuth.signOut();

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        registerViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_login);
        loginBtn = findViewById(R.id.btn_login);
        loginGoogleBtn = findViewById(R.id.btn_signin_google);

        setPasswordToggle();
        createAccountFromLogin();
        forgotPassword();

        loginGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutAndRevokeAccess();  // Ensure the user is signed out and access is revoked
                Intent googleSignIn = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(googleSignIn, RC_SIGN_IN);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();

                if (!email.isEmpty() && !password.isEmpty()) {
                    loginViewModel.login(email, password);
                } else {
                    incompleteFormDialog();

                }

            }
        });

        loginViewModel.userLiveData.observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser firebaseUser) {
                if (firebaseUser != null) {
                    if (firebaseUser.isEmailVerified()) {
                        Log.d(TAG, "signInWithEmail:success");
                        Intent intent2 = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent2);
                        finish();
                    } else {
                        notVerifiedDialog();
                        mAuth.signOut();
                    }
                }
            }
        });

        loginViewModel.errorLiveData.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String error) {
                if (error != null) {
                    Log.w(TAG, "signInWithEmail:failure: " + error);
                    errorMessageToast(error);
                }
            }
        });
    }
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//        // Handle the connection failure here
//        Log.e(TAG, "GoogleSignInClient connection failed: " + connectionResult.getErrorMessage());
//        Toast.makeText(this, "Google Services connection failed", Toast.LENGTH_SHORT).show();
//    }
    private void signOutAndRevokeAccess() {
        mAuth.signOut();

        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            // User is now signed out
            Log.d(TAG, "Google Sign-Out: success");
        });

        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this, task -> {
            // Access is revoked
            Log.d(TAG, "Google Revoke Access: success");
        });
    }

    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInGoogleResult(task);
        }
    }

    private void handleSignInGoogleResult(Task<GoogleSignInAccount> task) {
        try {
            // Get the signed-in account from the Task
            GoogleSignInAccount account = task.getResult(ApiException.class);
            Log.d(TAG, "handleGoogleSignInResult: success");
            Log.d(TAG, "HELLO: " + account.getDisplayName());

            if (account != null) {
                firebaseAuthWithGoogle(account);  // Authenticate with Firebase
            }
        } catch (ApiException e) {
            // Handle the exception
            Log.w(TAG, "Google Sign-In failed: " + e.getStatusCode());
            errorMessageToast("Google Sign-In failed: " + e.getStatusCode());
        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign-in success
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // User is signed in
                            Log.d(TAG, "signInWithCredential:success");
                            String email = acct.getEmail();
                            String name = acct.getDisplayName();

                            registerViewModel.registerGoogleAcc(acct, null, null, null);
                            // Optionally, you can now fetch additional user info
                            // using Google People API or other means
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        // Sign-in failed
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        errorMessageToast("Authentication Failed.");
                    }
                });
    }

    private void createAccountFromLogin () {
        createAccBtn = findViewById(R.id.btnCreateAcc);
        createAccBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent1);
            }
        });
    }

    private void setPasswordToggle () {
        passwordInput = findViewById(R.id.password_login);
        viewPasswordBtn = findViewById(R.id.view_password_btn);
        viewPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility(passwordInput, viewPasswordBtn);
            }
        });
    }

    private void togglePasswordVisibility(EditText editText, ImageView imageView) {
        if (editText.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            imageView.setImageResource(R.drawable.view_password);
        } else {
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            imageView.setImageResource(R.drawable.hide_password);
        }

        editText.setSelection(editText.getText().length());
    }

    private void forgotPassword() {
        forgotPassBtn = findViewById(R.id.btn_forgot_pass);
        forgotPassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPassActivity.class);
                startActivity(intent);
            }
        });
    }

    private void incompleteFormDialog(){
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.incomplete_form_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        dialog.show();

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.custom_corner_rounded);

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9); // 80% of screen width
        params.height = WindowManager.LayoutParams.WRAP_CONTENT; // Adjust height as needed
        dialog.getWindow().setAttributes(params);

        Button btnClose = dialogView.findViewById(R.id.btn_close_incomplete);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void errorMessageToast (String message){
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.error_custom_toast, null);

        ImageView toastIcon = layout.findViewById(R.id.img_error_ic);
        TextView toastText = layout.findViewById(R.id.tv_error_message);

        toastIcon.setImageResource(R.drawable.ic_warning);
        toastText.setText(message);

        Toast toast = new Toast(this);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void notVerifiedDialog(){
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.not_verified_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        dialog.show();

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.custom_corner_rounded);

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(params);

        Button btnClose = dialogView.findViewById(R.id.btn_close_incomplete);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
}