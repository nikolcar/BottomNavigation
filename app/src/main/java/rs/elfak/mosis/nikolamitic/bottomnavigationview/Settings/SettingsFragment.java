package rs.elfak.mosis.nikolamitic.bottomnavigationview.Settings;

import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.BitmapManipulation;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.User;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Login.LoginActivity;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.R;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends Fragment
{
    public Dialog dialog;

    private Button btnChangePassword, btnChangePhoto, btnSave;
    private CheckBox work_check, players_check, friends_check;
    public EditText newPassword, repetePassword;
    private Spinner gpsSpinner;
    private FloatingActionButton btnLogout;

    private Integer gpsRefresh;
    private Boolean friends_status, players_status, workback_status;
    public Uri savedURI;

    private TextView tvName, tvPoints;
    private static ImageView ivAvatar;

    private Rect displayRectangle;
    private LayoutInflater layoutInflater;

    private FirebaseAuth mAuth;
    private FirebaseUser loggedUser;
    private FirebaseDatabase database;
    private StorageReference storage;

    static File localFileProfileImage = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.settings_fragment, container, false);

        mAuth = FirebaseAuth.getInstance();
        loggedUser = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance().getReference().child("profile_images/" + loggedUser.getUid() + ".jpg");
        database = FirebaseDatabase.getInstance();

        tvName = (TextView) v.findViewById(R.id.item_friend_name);
        ivAvatar = (ImageView) v.findViewById(R.id.item_friend_avatar);

        if(loggedUser!=null)
        {
            updatePoints();
            tvName.setText(loggedUser.getDisplayName());
            //final Uri photoUrl = loggedUser.getPhotoUrl();
            setProfilePhoto();
        }

        btnChangePassword = (Button) v.findViewById(R.id.btn_change_password);
        btnChangePhoto = (Button) v.findViewById(R.id.btn_change_image);

        displayRectangle = new Rect();
        Window window = getActivity().getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        layoutInflater = (LayoutInflater)getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);

        work_check = (CheckBox) v.findViewById(R.id.settings_work_back);
        players_check = (CheckBox) v.findViewById(R.id.settings_show_players);
        friends_check = (CheckBox) v.findViewById(R.id.settings_show_friends);

        gpsSpinner = (Spinner) v.findViewById(R.id.gps_spinner);
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.gps_refresh_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gpsSpinner.setAdapter(adapter);

        //TODO spinner selector
        gpsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                gpsRefresh = Integer.parseInt(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        btnSave = (Button) v.findViewById(R.id.btn_save_settings);

        btnLogout = (FloatingActionButton) v.findViewById(R.id.btn_logout);

        btnLogout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mAuth.signOut();
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
        });

/*
        database.getReference("users").child(loggedUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);
                friends_status = u.showfriends;
                players_status = u.showplayers;
                workback_status = u.workback;
                gpsRefresh = u.gpsrefresh;

                friends_check.setChecked(friends_status);
                players_check.setChecked(players_status);
                work_check.setChecked(workback_status);
                //int pos = adapter.getPosition(gpsRefresh.toString());
                //gpsSpinner.setSelection(pos);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
*/

        btnSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "Please wait...", "Processing...",true);

                database.getReference("users").child(loggedUser.getUid()).child("workback").setValue(work_check.isChecked());
                database.getReference("users").child(loggedUser.getUid()).child("showplayers").setValue(players_check.isChecked());
                database.getReference("users").child(loggedUser.getUid()).child("showfriends").setValue(friends_check.isChecked());
                database.getReference("users").child(loggedUser.getUid()).child("gpsrefresh").setValue(gpsRefresh);

                progressDialog.dismiss();

                Toast.makeText(getActivity(), "Settings saved", Toast.LENGTH_SHORT).show();

                //Snackbar.make(findViewById(android.R.id.content), "Please exit the app in order to apply the settings about showing players or friends", Snackbar.LENGTH_LONG).show();
            }
        });

        btnChangePassword.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                dialog = new Dialog(getActivity(),R.style.dialog_no_tytle);

                //TODO change password
                View layout = layoutInflater.inflate(R.layout.dialog_change_password, null);
                layout.setMinimumWidth((int)(displayRectangle.width() * 0.8f));

                dialog.setContentView(layout);

                TextView tvTitle = (TextView) dialog.findViewById(R.id.change_password_title);
                tvTitle.setText(R.string.change_password);

                newPassword = (EditText) dialog.findViewById(R.id.change_password_new_password);
                repetePassword = (EditText) dialog.findViewById(R.id.change_password_repeat_password);

                dialog.show();
            }
        });

        btnChangePhoto.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog = new Dialog(getActivity(),R.style.dialog_no_tytle);

                //TODO change image
                View layout = layoutInflater.inflate(R.layout.dialog_change_photo, null);
                layout.setMinimumWidth((int)(displayRectangle.width() * 0.8f));

                dialog.setContentView(layout);

                TextView tvTitle = (TextView) dialog.findViewById(R.id.change_photo_title);
                tvTitle.setText(R.string.change_image);

                dialog.show();
            }
        });

        return v;
    }

    public void updatePoints(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("users").child(loggedUser.getUid()).child("points").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int points = dataSnapshot.getValue(Integer.class);
                tvPoints = (TextView) getView().findViewById(R.id.item_friend_points);
                tvPoints.setText(String.valueOf(points));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private Bitmap getBitmapFromURL(Uri uri)
    {
        try
        {
            String str = uri.toString();
            URL src = new URL(str);
            HttpURLConnection connection = (HttpURLConnection) src.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input=connection.getInputStream();
            Bitmap mBitmap = BitmapFactory.decodeStream(input);
            return mBitmap;
        }
        catch (IOException e)
        {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public void setProfilePhoto()
    {
        try
        {
            localFileProfileImage = File.createTempFile("profileImage",".jpg");
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        storage = FirebaseStorage.getInstance().getReference().child("profile_images/" + loggedUser.getUid() + ".jpg");
        storage.getFile(localFileProfileImage).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Bitmap bitmap = BitmapFactory.decodeFile(localFileProfileImage.getAbsolutePath());
                if(bitmap!=null){
                    bitmap = BitmapManipulation.getCroppedBitmap(bitmap);
                    ivAvatar.setImageBitmap(bitmap);
                    bitmap = null;
                }else{
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //Toast.makeText(MainActivity.this, "Error downloading/saving profile image", Toast.LENGTH_SHORT).show();
                //TODO: Can't display this, maybe user doesn't have a profile photo
            }
        });
    }

}
