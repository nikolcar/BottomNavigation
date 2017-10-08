package rs.elfak.mosis.nikolamitic.bottomnavigationview.Login;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.User;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.MainActivity;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.R;

import static rs.elfak.mosis.nikolamitic.bottomnavigationview.MyLocationService.latitude;
import static rs.elfak.mosis.nikolamitic.bottomnavigationview.MyLocationService.longitude;

public class SignupActivity extends Activity
{
    private EditText txtFirstName, txtLastName, txtNickname, txtEmailAddress, txtPassword,  txtRepeatPassword;
    private TextView tvDateOfBirth;

    private int year = 1970, month = 00, day = 01;
    static final int DATE_PICKER_ID = 1111;

    private FirebaseAuth mAuth;
    private DatabaseReference users;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_signup);

        txtFirstName = (EditText) findViewById(R.id.sign_up_firstname);
        txtLastName = (EditText) findViewById(R.id.sign_up_lastname);
        txtNickname = (EditText) findViewById(R.id.sign_up_nickname);

        tvDateOfBirth = (TextView) findViewById(R.id.sign_up_date_of_birth);
        Button changeDate = (Button) findViewById(R.id.btn_datepicker);

        txtEmailAddress = (EditText) findViewById(R.id.sign_up_email);
        txtPassword = (EditText) findViewById(R.id.sign_up_password);
        txtRepeatPassword = (EditText) findViewById(R.id.sign_up_repeat_password);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        users = database.getReference("users");

        // Button listener to show date picker dialog
        changeDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // On button click show datepicker dialog
                showDialog(DATE_PICKER_ID);
            }
        });
    }

    public void sign_up_button_click(View v)
    {
        final String firstName = txtFirstName.getText().toString();
        final String lastName = txtLastName.getText().toString();
        final String nickname = txtNickname.getText().toString();
        final String dateOfBirth = tvDateOfBirth.getText().toString();

        final String email = txtEmailAddress.getText().toString();
        final String password = txtPassword.getText().toString();
        final String repeatPassword = txtRepeatPassword.getText().toString();

        if (TextUtils.isEmpty(firstName))
        {
            Toast.makeText(getApplicationContext(), "Enter your first name!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(lastName))
        {
            Toast.makeText(getApplicationContext(), "Enter your last name!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(nickname))
        {
            Toast.makeText(getApplicationContext(), "Enter your nickname!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dateOfBirth.equals(getResources().getString(R.string.date_of_birth)))
        {
            Toast.makeText(getApplicationContext(), "Enter your date of birth!", Toast.LENGTH_SHORT).show();
            return;
        }

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

        if (TextUtils.isEmpty(repeatPassword))
        {
            Toast.makeText(getApplicationContext(), "Repeat password!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(repeatPassword))
        {
            Toast.makeText(getApplicationContext(), "Password and repeat password are not the same!", Toast.LENGTH_SHORT).show();
            return;
        }

        final ProgressDialog progressDialog = ProgressDialog.show(SignupActivity.this, "Please wait...", "Processing...",true);

        (mAuth.createUserWithEmailAndPassword(email,password)).addOnCompleteListener(new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                progressDialog.dismiss();

                if(task.isSuccessful())
                {
                    String uid = task.getResult().getUser().getUid();

                    addUserInDatabase(uid, firstName, lastName, nickname, dateOfBirth);
                    Toast.makeText(SignupActivity.this, "Registration successful", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                    intent.putExtra("DISPLAY_NAME", firstName + " " + lastName + "\n" + nickname);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    Toast.makeText(SignupActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void addUserInDatabase(String uid, String first, String last, String nick, String date)
    {
        User newUser = new User(first, last, nick, date, longitude, latitude);
        users.child(uid).setValue(newUser);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(first + " " + last + "\n" + nick)
                .build();

        user.updateProfile(profileUpdates);
    }

    @Override
    protected Dialog onCreateDialog(int id)
    {
        switch (id)
        {
            case DATE_PICKER_ID:

                // open datepicker dialog.
                // set date picker for current date
                // add pickerListener listner to date picker
                DatePickerDialog datePickerDialog = new DatePickerDialog(this,pickerListener, year, month, day);
                datePickerDialog.getDatePicker().getTouchables().get(0).performClick();
                return datePickerDialog;
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener pickerListener = new DatePickerDialog.OnDateSetListener()
    {
        // when dialog box is closed, below method will be called.
        @Override
        public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay)
        {
            year  = selectedYear;
            month = selectedMonth;
            day   = selectedDay;

            // Show selected date
            tvDateOfBirth.setText(new StringBuilder().append(day).append("-").append(month+1).append("-").append(year).append(" "));
        }
    };
}

