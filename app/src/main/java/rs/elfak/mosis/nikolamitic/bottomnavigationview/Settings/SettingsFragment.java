package rs.elfak.mosis.nikolamitic.bottomnavigationview.Settings;

import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
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
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Friends.FriendsFragment;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Login.LoginActivity;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.MainActivity;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.MyLocationService;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.R;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends Fragment
{
    public Dialog dialog;

    private CheckBox work_check, players_check, friends_check;
    public Boolean friends_status, players_status, workback_status;

    private Spinner gpsSpinner;

    public Integer gpsRefresh;
    public Uri savedURI;

    private TextView tvName, tvPoints;
    private static ImageView ivAvatar;

    private FirebaseAuth mAuth;
    private FirebaseUser loggedUser;
    private FirebaseDatabase database;

    private ArrayAdapter<CharSequence> adapter;

    static File localFileProfileImage = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.settings_fragment, container, false);

        mAuth = FirebaseAuth.getInstance();
        loggedUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();

        tvName = (TextView) v.findViewById(R.id.item_friend_name);
        ivAvatar = (ImageView) v.findViewById(R.id.item_friend_avatar);

        if(loggedUser!=null)
        {
            tvName.setText(getArguments().getString("display_name"));
            updateInfo();
        }

        Button btnChangePassword = (Button) v.findViewById(R.id.btn_change_password);
        Button btnChangePhoto = (Button) v.findViewById(R.id.btn_change_image);
        Button btnSave = (Button) v.findViewById(R.id.btn_save_settings);
        FloatingActionButton btnLogout = (FloatingActionButton) v.findViewById(R.id.btn_logout);

        final Rect displayRectangle = new Rect();
        Window window = getActivity().getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        final LayoutInflater layoutInflater = (LayoutInflater)getActivity().getSystemService(MainActivity.LAYOUT_INFLATER_SERVICE);

        work_check = (CheckBox) v.findViewById(R.id.settings_work_back);
        players_check = (CheckBox) v.findViewById(R.id.settings_show_players);
        friends_check = (CheckBox) v.findViewById(R.id.settings_show_friends);

        gpsSpinner = (Spinner) v.findViewById(R.id.gps_spinner);
        adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.gps_refresh_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gpsSpinner.setAdapter(adapter);
        gpsSpinner.getBackground().setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.SRC_ATOP);

        gpsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                gpsRefresh = Integer.parseInt(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                signOut();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "Please wait...", "Processing...",true);

                boolean friends = friends_check.isChecked();
                boolean players = players_check.isChecked();

                database.getReference("users").child(loggedUser.getUid()).child("workback").setValue(work_check.isChecked());
                database.getReference("users").child(loggedUser.getUid()).child("showplayers").setValue(players);
                database.getReference("users").child(loggedUser.getUid()).child("showfriends").setValue(friends);
                database.getReference("users").child(loggedUser.getUid()).child("gpsrefresh").setValue(gpsRefresh);

                progressDialog.dismiss();

                Toast.makeText(getActivity(), "Settings saved", Toast.LENGTH_SHORT).show();

                MainActivity activity = (MainActivity)getActivity();

                activity.changeVisibility(players_status != players, friends_status != friends);

                players_status = players;
                friends_status = friends;

                activity.backgroundService.putExtra("settingsGpsRefreshTime", gpsRefresh);
                activity.backgroundService.putExtra("loggedUserUid", loggedUser.getUid());

                if(work_check.isChecked() & !activity.isMyServiceRunning(MyLocationService.class))
                {
                    activity.startService(activity.backgroundService);
                    Toast.makeText(getActivity(),"Starting background service",Toast.LENGTH_SHORT).show();
                }

                if (!work_check.isChecked() & activity.isMyServiceRunning(MyLocationService.class))
                {
                    activity.stopService(activity.backgroundService);
                    Toast.makeText(getActivity(),"Stopping background service",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnChangePassword.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog = new Dialog(getActivity(),R.style.dialog_no_tytle);

                View layout = layoutInflater.inflate(R.layout.dialog_change_password, null);
                layout.setMinimumWidth((int)(displayRectangle.width() * 0.8f));

                dialog.setContentView(layout);

                TextView tvTitle = (TextView) dialog.findViewById(R.id.change_password_title);
                tvTitle.setText(R.string.change_password);

                final EditText etNewPassword = (EditText) dialog.findViewById(R.id.change_password_new_password);
                final EditText etRepeatPassword = (EditText) dialog.findViewById(R.id.change_password_repeat_password);
                Button passwordChanged = (Button) dialog.findViewById(R.id.change_password);
                Button cancelChange = (Button) dialog.findViewById(R.id.cancel_password);

                passwordChanged.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        final String newPassword = etNewPassword.getText().toString();
                        final String repeatPassword = etRepeatPassword.getText().toString();

                        if (TextUtils.isEmpty(newPassword))
                        {
                            Toast.makeText(getActivity(), "Enter new password!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (newPassword.trim().length() < 6)
                        {
                            Toast.makeText(getActivity(), "Password too short, enter minimum 6 characters", Toast.LENGTH_SHORT).show();
                        }

                        if (TextUtils.isEmpty(repeatPassword))
                        {
                            Toast.makeText(getActivity(), "Repeat new password!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (!newPassword.equals(repeatPassword))
                        {
                            Toast.makeText(getActivity(), "Password and repeat password are not the same!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "Please wait...", "Processing...",true);

                        try
                        {
                            loggedUser.updatePassword(newPassword.trim()).addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        Toast.makeText(getActivity(), "Password is updated, sign in with new password!", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        Toast.makeText(getActivity(), "In order to change password you need to sing out and then sign in again :(", Toast.LENGTH_SHORT).show();
                                    }
                                    progressDialog.dismiss();
                                    dialog.dismiss();
                                    signOut();
                                }
                            });
                        }
                        catch (Throwable t)
                        {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Error, please try again.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                cancelChange.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        btnChangePhoto.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog = new Dialog(getActivity(),R.style.dialog_no_tytle);

                View layout = layoutInflater.inflate(R.layout.dialog_change_photo, null);
                layout.setMinimumWidth((int)(displayRectangle.width() * 0.8f));

                dialog.setContentView(layout);

                TextView tvTitle = (TextView) dialog.findViewById(R.id.change_photo_title);
                tvTitle.setText(R.string.change_image);

                Button btnCamera = (Button) dialog.findViewById(R.id.camera);
                Button btnGallery = (Button) dialog.findViewById(R.id.gallery);

                btnCamera.setOnClickListener(new View.OnClickListener()
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

                btnGallery.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(pickPhoto , 1);
                    }
                });

                dialog.show();
            }
        });

        return v;
    }

    public void getSettingsFromServer()
    {
        database.getReference("users").child(loggedUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                User u = dataSnapshot.getValue(User.class);

                friends_status = u.getShowfriends();
                players_status = u.getShowplayers();
                workback_status = u.getWorkback();
                gpsRefresh = u.getGpsrefresh();

                MainActivity activity = (MainActivity)getActivity();
                activity.loadPlayersFromServer(players_status, friends_status);

                activity.backgroundService.putExtra("settingsGpsRefreshTime", gpsRefresh);
                activity.backgroundService.putExtra("loggedUserUid", loggedUser.getUid());

                if(workback_status & !activity.isMyServiceRunning(MyLocationService.class))
                {
                    activity.startService(activity.backgroundService);
                    Toast.makeText(getActivity(),"Starting background service",Toast.LENGTH_SHORT).show();
                }

                if (!workback_status & activity.isMyServiceRunning(MyLocationService.class))
                {
                    activity.stopService(activity.backgroundService);
                    Toast.makeText(getActivity(),"Stopping background service",Toast.LENGTH_SHORT).show();
                }

                friends_check.setChecked(friends_status);
                players_check.setChecked(players_status);
                work_check.setChecked(workback_status);
                int pos = adapter.getPosition(gpsRefresh.toString());
                gpsSpinner.setSelection(pos);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void updateInfo()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("users").child(loggedUser.getUid()).child("points").addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                int points = dataSnapshot.getValue(Integer.class);
                tvPoints = (TextView) getView().findViewById(R.id.item_friend_points);
                tvPoints.setText(String.valueOf(points));
                tvName.setText(loggedUser.getDisplayName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
            }
        });

        setProfilePhoto();
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
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        StorageReference storage = FirebaseStorage.getInstance().getReference().child("profile_images/" + loggedUser.getUid() + ".jpg");
        storage.getFile(localFileProfileImage).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>()
        {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot)
            {
                Bitmap bitmap = BitmapFactory.decodeFile(localFileProfileImage.getAbsolutePath());
                if(bitmap!=null)
                {
                    bitmap = BitmapManipulation.getCroppedBitmap(bitmap);
                    ivAvatar.setImageBitmap(bitmap);
                    bitmap = null;
                }
                else
                {
                }

            }
        })
        .addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception exception)
            {
                //Toast.makeText(MainActivity.this, "Error downloading/saving profile image", Toast.LENGTH_SHORT).show();
                //TODO: Can't display this, maybe user doesn't have a profile photo
            }
        });
    }

    public void signOut()
    {
        mAuth.signOut();
        startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 || requestCode == 1)
        {
            if (resultCode == RESULT_OK)
            {
                final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "Please wait...", "Processing...",true);

                if (data != null)
                    savedURI = data.getData();

                //                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setPhotoUri(settingsFragment.savedURI).build();
//                progressDialog.dismiss();
//                loggedUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        Toast.makeText(MainActivity.this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
//                    }
//                });

                StorageReference storage = FirebaseStorage.getInstance().getReference().child("profile_images/" + loggedUser.getUid() + ".jpg");
                storage.putFile(savedURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        setProfilePhoto();
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Profile picture updated!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(getActivity(), "Failed to upload picture, please try again!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
                dialog.dismiss();
            }
            else
            {
                Toast.makeText(getActivity(), "Action canceled!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
