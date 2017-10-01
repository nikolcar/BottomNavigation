package rs.elfak.mosis.nikolamitic.bottomnavigationview.Login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import rs.elfak.mosis.nikolamitic.bottomnavigationview.R;

public class ResetPasswordActivity extends Activity
{
    FirebaseAuth mAuth;
    EditText etEmail;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_reset_password);

        mAuth = FirebaseAuth.getInstance();

        Intent i = getIntent();
        email = i.getStringExtra("Email");

        etEmail = (EditText) findViewById(R.id.reset_password_email);
        etEmail.setText(email);
        etEmail.setSelection(email.length());
    }

    public void reset_password_button_click(View v)
    {
        email = etEmail.getText().toString();

        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

        final ProgressDialog progressDialog = ProgressDialog.show(ResetPasswordActivity.this, "Please wait...", "Processing...",true);

        (mAuth.sendPasswordResetEmail(email)).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                progressDialog.dismiss();

                if(task.isSuccessful())
                {
                    Toast.makeText(ResetPasswordActivity.this, "Email is sent", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                    i.putExtra("Reset_Email",email);
                    startActivity(i);
                    finish();
                }
                else
                {
                    Toast.makeText(ResetPasswordActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
