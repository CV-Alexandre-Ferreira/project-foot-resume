package com.example.projectfoot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.example.projectfoot.config.FirebaseConfig;
import com.example.projectfoot.helper.Base64Custom;
import com.example.projectfoot.helper.UserFirebase;
import com.example.projectfoot.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class SignUpActivity extends AppCompatActivity {

    private EditText nameField, emailField, passwordField;
    private FirebaseAuth authentication;

    public static final int NORMAL_USER = 0;
    public static final int COURT_USER = 1;

    private Switch switchTypeOfUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        nameField = findViewById(R.id.editNome);
        emailField = findViewById(R.id.editEmail);
        passwordField = findViewById(R.id.editSenha);
        switchTypeOfUser = findViewById(R.id.switchTypeOfUser);
    }

    public void registerUser(User user){

        authentication = FirebaseConfig.getFirebaseAuth();
        authentication.createUserWithEmailAndPassword(
                user.getEmail(), user.getPassword()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    Toast.makeText(SignUpActivity.this, R.string.registration_successful, Toast.LENGTH_SHORT).show();
                    UserFirebase.updateUserName(user.getName());
                    finish();

                    try{

                        String userEmail = Base64Custom.encodeBase64(user.getEmail());

                        if(switchTypeOfUser.isChecked()) user.setUserType(COURT_USER);
                        else user.setUserType(NORMAL_USER);

                        user.setId(userEmail);
                        user.save();

                    }catch (Exception e){e.printStackTrace();}
                }else{
                    String ex = "";
                    try{
                        throw task.getException();
                    }catch (FirebaseAuthWeakPasswordException e){
                        ex = "Digite senha mais forte";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        ex = "Digite um email valido";
                    }catch (FirebaseAuthUserCollisionException e){
                        ex = "Já existe um cadastro nessa conta";
                    }catch (Exception e){
                        ex = "Erro ao cadastrar user: " + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(SignUpActivity.this, ex, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    public void validateUserRegister(View view){

        String name = nameField.getText().toString();
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        if(!name.isEmpty() && !email.isEmpty() && !password.isEmpty()){
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(password);

            registerUser(user);
        }
        else{
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
        }

    }
}