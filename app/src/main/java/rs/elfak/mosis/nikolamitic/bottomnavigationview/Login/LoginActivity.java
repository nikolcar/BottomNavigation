package rs.elfak.mosis.nikolamitic.bottomnavigationview.Login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import rs.elfak.mosis.nikolamitic.bottomnavigationview.MainActivity;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.R;

public class LoginActivity extends Activity
{
    private EditText inputEmail, inputPassword;
    String email;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        Intent i = getIntent();
        email = i.getStringExtra("Reset_Email");

        inputEmail = (EditText) findViewById(R.id.login_email);

        inputEmail.setText(email);

        inputPassword = (EditText) findViewById(R.id.login_password);

        if(email!=null)
        {
            inputPassword.requestFocus();
        }

        //TODO remove default values
        inputEmail.setText("mitic.nikolca94@gmail.com");
        inputPassword.setText("nikolcar");

        mAuth = FirebaseAuth.getInstance();
    }

    public void sign_up_click(View view)
    {
        startActivity(new Intent(LoginActivity.this, SignupActivity.class));
    }

    public void reset_password_click(View view)
    {
        Intent i = new Intent(LoginActivity.this, ResetPasswordActivity.class);
        email = inputEmail.getText().toString();
        i.putExtra("Email",email);
        startActivity(i);
    }

    public void login_button_click(View view)
    {
        email = inputEmail.getText().toString();
        final String password = inputPassword.getText().toString();

        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password))
        {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

        final ProgressDialog progressDialog = ProgressDialog.show(LoginActivity.this, "Please wait...", "Processing...",true);

        (mAuth.signInWithEmailAndPassword(email,password))
                .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        progressDialog.dismiss();

                        if(task.isSuccessful())
                        {
                            Toast.makeText(LoginActivity.this, "Log in successful", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }
                        else
                        {
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
