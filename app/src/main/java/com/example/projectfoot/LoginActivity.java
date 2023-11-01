package com.example.projectfoot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projectfoot.config.FirebaseConfig;
import com.example.projectfoot.helper.Base64Custom;
import com.example.projectfoot.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private EditText emailField, passwordField;
    private TextView registerButton;
    private FirebaseAuth authentication;
    private Button loginButton;
    private SignInButton googleSignInButton;
    private static final int RC_SIGN_IN = 100;
    private GoogleSignInClient googleSignInClient;
    private static final String TAG = "GOOGLE_SIGN_IN_TAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        registerButton = findViewById(R.id.buttonParaCadastro);
        googleSignInButton = findViewById(R.id.googleSignInButton);

        authentication = FirebaseConfig.getFirebaseAuth();

        emailField = findViewById(R.id.editEmailLogin);
        passwordField = findViewById(R.id.editSenhaLogin);
        loginButton = findViewById(R.id.buttonLogar);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);

            }
        });

        //configure the google Sign in
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //begin google sign in
                Log.d(TAG,"onClick: begin Google SignIn");
                Intent intent = googleSignInClient.getSignInIntent();
                startActivityForResult(intent, RC_SIGN_IN); //now we need to handle result of intent
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validarAutenticacaoUsuario();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Result returned from launching the Intent from GoogleSignInApi.getSignInIntent
        if(requestCode == RC_SIGN_IN){

            Log.d(TAG, "onActivityResult: Google SignIn intent Result");
            Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(data);

            try{
                //google sign in success, now auth with firebase
                GoogleSignInAccount account = accountTask.getResult(ApiException.class);
                firebaseAuthWithGoogleAccount(account);

            }
            catch (Exception e){

                //failed google sign in
                Log.d(TAG, "onActivityResult ERROR: "+e.getMessage());
            }
        }
    }

    private void firebaseAuthWithGoogleAccount(GoogleSignInAccount account) {

        Log.d(TAG, "firebaseAuthWithGoogleAccount: begin firebase auth with google account");
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        authentication.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "onSuccess: Logged In");

                        //get logged in user
                        FirebaseUser firebaseUser = authentication.getCurrentUser();
                        //get user info
                        String uid = firebaseUser.getUid();
                        String email = firebaseUser.getEmail();

                        //check if user is new or existing
                        Log.d(TAG, "onSuccess: Email"+email);
                        Log.d(TAG, "onSuccess: UID"+uid);

                        if(authResult.getAdditionalUserInfo().isNewUser()){
                            //user is new - Account Created
                            Toast.makeText(LoginActivity.this, R.string.account_created+" "+email, Toast.LENGTH_SHORT).show();


                            User user = new User();
                            String idUser = Base64Custom.encodeBase64(email);
                            user.setId(idUser);
                            user.setName("");
                            user.setEmail(email);
                            user.setUserType(0);

                            user.save();
                            //recuperarUsuarioDatabase();
                            openMainScreen();

                        }
                        else{
                            //existing user - Logged in
                            Log.d(TAG, "onSuccess: Existing user\n"+email);
                            Toast.makeText(LoginActivity.this, R.string.existing_user+" "+email, Toast.LENGTH_SHORT).show();
                            //recuperarUsuarioDatabase();
                        }

                        //start profile activity
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.d(TAG, "onFailure: Logging failed "+e.getMessage());

                    }
                });

    }

    public void openMainScreen(){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public void userLogin(User user){

        authentication.signInWithEmailAndPassword(
                user.getEmail(), user.getPassword()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    openMainScreen();

                }else{
                    //Add expection treatment
                    Toast.makeText(LoginActivity.this, R.string.authentication_error, Toast.LENGTH_SHORT).show();

                }

            }
        });

    }
    public void validarAutenticacaoUsuario(){

        String emailText = emailField.getText().toString();
        String passwordText = passwordField.getText().toString();

        if( !emailText.isEmpty() && !passwordText.isEmpty()){

            User user = new User();
            user.setEmail(emailText);
            user.setPassword(passwordText);

            userLogin(user);

        }
        else{
            Toast.makeText(LoginActivity.this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
        }

    }
}