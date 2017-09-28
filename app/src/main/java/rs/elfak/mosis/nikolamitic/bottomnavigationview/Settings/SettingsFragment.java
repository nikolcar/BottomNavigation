package rs.elfak.mosis.nikolamitic.bottomnavigationview.Settings;

import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import rs.elfak.mosis.nikolamitic.bottomnavigationview.Login.LoginActivity;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.R;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends Fragment
{
    private FirebaseAuth mAuth;
    private FirebaseUser loggedUser;
    private FirebaseDatabase database;
    private StorageReference storage;

    private Button btnChangePassword, btnChangePhoto, btnSave, changePassword, camera, gallery;
    private CheckBox work_check, players_check, friends_check;
    private EditText newPassword;
    private Spinner gpsSpinner;
    private LinearLayout photoLayout, changePasswordLayout;
    private FloatingActionButton btnLogout;

    private Integer gpsRefresh;
    private Boolean friends_status, players_status, workback_status;
    private Uri savedURI;

    TextView tvName, tvPoints;
    private static ImageView ivAvatar;

    Rect displayRectangle;
    LayoutInflater layoutInflater;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.settings_fragment, container, false);


        tvName = (TextView) v.findViewById(R.id.item_friend_name);
        ivAvatar = (ImageView) v.findViewById(R.id.item_friend_avatar);

        mAuth = FirebaseAuth.getInstance();

        loggedUser = mAuth.getCurrentUser();

        database = FirebaseDatabase.getInstance();

        storage = FirebaseStorage.getInstance().getReference().child("profile_images/" + loggedUser.getUid() + ".jpg");

        if(loggedUser!=null)
        {
            updatePoints();
            tvName.setText(loggedUser.getDisplayName());
            //ivAvatar.setImageBitmap();
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
                int pos = adapter.getPosition(gpsRefresh.toString());
                gpsSpinner.setSelection(pos);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


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
*/
        btnChangePassword.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(getActivity(),R.style.dialog_no_tytle);

                //TODO change password
                View layout = layoutInflater.inflate(R.layout.dialog_change_password, null);
                layout.setMinimumWidth((int)(displayRectangle.width() * 0.8f));

                dialog.setContentView(layout);

                TextView tvTitle = (TextView) dialog.findViewById(R.id.change_password_title);
                tvTitle.setText(R.string.change_password);


                dialog.show();
            }
        });

        btnChangePhoto.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Dialog dialog = new Dialog(getActivity(),R.style.dialog_no_tytle);

                //TODO change image
                View layout = layoutInflater.inflate(R.layout.dialog_change_photo, null);
                layout.setMinimumWidth((int)(displayRectangle.width() * 0.8f));

                dialog.setContentView(layout);

                TextView tvTitle = (TextView) dialog.findViewById(R.id.change_photo_title);
                tvTitle.setText(R.string.change_image);

                dialog.show();
            }
        });
/*
        camera.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent imageIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                File imagesFolder = new File(Environment.getExternalStorageDirectory(), "WorkingWithPhotosApp");
                imagesFolder.mkdirs();

                File image = new File(imagesFolder, "QR_1.png");
                savedURI = Uri.fromFile(image);

                imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, savedURI);

                startActivityForResult(imageIntent, 0);
            }
        });

        gallery.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , 1);
            }
        });
*/
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 || requestCode == 1)
        {
            if (resultCode == RESULT_OK)
            {
                final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "Please wait...", "Processing...",true);

                if (data != null)
                    savedURI = data.getData();
                /*UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setPhotoUri(selectedImage).build();
                user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(SettingsActivity.this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                    }
                });*/

                storage.putFile(savedURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getActivity(), "Profile picture updated!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Failed to upload picture, please try again!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();                    }
                });
            }
            else {
                Toast.makeText(getActivity(), "Action canceled!", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
