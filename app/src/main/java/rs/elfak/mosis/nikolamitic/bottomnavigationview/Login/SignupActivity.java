package rs.elfak.mosis.nikolamitic.bottomnavigationview.Login;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.User;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.MainActivity;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.R;

import static rs.elfak.mosis.nikolamitic.bottomnavigationview.MainActivity.customToast;

public class SignupActivity extends Activity {
    private EditText txtFirstName, txtLastName, txtNickname, txtEmailAddress, txtPassword, txtRepeatPassword;
    private TextView tvDateOfBirth;

    private int year = 1970, month = 0, day = 1;
    static final int DATE_PICKER_ID = 1111;

    private FirebaseAuth mAuth;
    private DatabaseReference users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_signup);

        txtFirstName = findViewById(R.id.sign_up_firstname);
        txtLastName = findViewById(R.id.sign_up_lastname);
        txtNickname = findViewById(R.id.sign_up_nickname);

        tvDateOfBirth = findViewById(R.id.sign_up_date_of_birth);
        Button changeDate = findViewById(R.id.btn_datepicker);

        txtEmailAddress = findViewById(R.id.sign_up_email);
        txtPassword = findViewById(R.id.sign_up_password);
        txtRepeatPassword = findViewById(R.id.sign_up_repeat_password);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        users = database.getReference("users");

        // Button listener to show date picker dialog
        changeDate.setOnClickListener(v -> {
            // On button click show datepicker dialog
            showDialog(DATE_PICKER_ID);
        });
    }

    public void sign_up_button_click(View v) {
        final String firstName = txtFirstName.getText().toString();
        final String lastName = txtLastName.getText().toString();
        final String nickname = txtNickname.getText().toString();
        final String dateOfBirth = tvDateOfBirth.getText().toString();

        final String email = txtEmailAddress.getText().toString();
        final String password = txtPassword.getText().toString();
        final String repeatPassword = txtRepeatPassword.getText().toString();

        if (TextUtils.isEmpty(firstName)) {
            customToast(getApplicationContext(), "Enter your first name!", Toast.LENGTH_SHORT);
            return;
        }

        if (TextUtils.isEmpty(lastName)) {
            customToast(getApplicationContext(), "Enter your last name!", Toast.LENGTH_SHORT);
            return;
        }

        if (TextUtils.isEmpty(nickname)) {
            customToast(getApplicationContext(), "Enter your nickname!", Toast.LENGTH_SHORT);
            return;
        }

        if (dateOfBirth.equals(getResources().getString(R.string.date_of_birth))) {
            customToast(getApplicationContext(), "Enter your date of birth!", Toast.LENGTH_SHORT);
            return;
        }

        if (TextUtils.isEmpty(email)) {
            customToast(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT);
            return;
        }

        if (TextUtils.isEmpty(password)) {
            customToast(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT);
            return;
        }

        if (TextUtils.isEmpty(repeatPassword)) {
            customToast(getApplicationContext(), "Repeat password!", Toast.LENGTH_SHORT);
            return;
        }

        if (!password.equals(repeatPassword)) {
            customToast(getApplicationContext(), "Password and repeat password are not the same!", Toast.LENGTH_SHORT);
            return;
        }

        final ProgressDialog progressDialog = ProgressDialog.show(SignupActivity.this, "Please wait...", "Processing...", true);

        (mAuth.createUserWithEmailAndPassword(email, password)).addOnCompleteListener(task -> {
            progressDialog.dismiss();

            if (task.isSuccessful()) {
                String uid = Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getUser()).getUid();

                addUserInDatabase(uid, firstName, lastName, nickname, dateOfBirth);
                customToast(SignupActivity.this, "Registration successful", Toast.LENGTH_LONG);
                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                intent.putExtra("DISPLAY_NAME", firstName + " " + lastName + "\n" + nickname);
                startActivity(intent);
                finish();
            } else {
                customToast(SignupActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG);
            }
        });
    }

    private void addUserInDatabase(String uid, String first, String last, String nick, String date) {
        User newUser = new User(first, last, nick, date);
        users.child(uid).setValue(newUser);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(first + " " + last + "\n" + nick)
                .build();

        assert user != null;
        user.updateProfile(profileUpdates);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == DATE_PICKER_ID) {// open datepicker dialog.
            // set date picker for current date
            // add pickerListener listner to date picker
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, pickerListener, year, month, day);
            datePickerDialog.getDatePicker().getTouchables().get(0).performClick();
            return datePickerDialog;
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener pickerListener = new DatePickerDialog.OnDateSetListener() {
        // when dialog box is closed, below method will be called.
        @Override
        public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
            year = selectedYear;
            month = selectedMonth;
            day = selectedDay;

            // Show selected date
            tvDateOfBirth.setText(new StringBuilder().append(day).append("-").append(month + 1).append("-").append(year).append(" "));
        }
    };
}
