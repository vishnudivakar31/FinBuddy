package io.wanderingthinkter.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import io.wanderingthinkter.R;
import io.wanderingthinkter.activities.LoginActivity;
import io.wanderingthinkter.models.UserModel;

import static io.wanderingthinkter.util.Constants.KEY_USER_ID;
import static io.wanderingthinkter.util.Constants.USER_COLLECTION;

public class SettingsFragment extends Fragment implements View.OnClickListener {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection(USER_COLLECTION);
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser firebaseUser;
    private TextView userName, changePassword, signOut;
    private ImageView profilePicture;
    private UserModel userModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_settings, container, false);
        userName = view.findViewById(R.id.settings_username);
        changePassword = view.findViewById(R.id.settings_change_password);
        signOut = view.findViewById(R.id.settings_sign_out);
        profilePicture = view.findViewById(R.id.settings_profile_picture);

        changePassword.setOnClickListener(this);
        signOut.setOnClickListener(this);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseUser = auth.getCurrentUser();
        getCurrentUserDetails(firebaseUser.getUid());
    }

    private void getCurrentUserDetails(String uid) {
        collectionReference
                .whereEqualTo(KEY_USER_ID, uid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty()) {
                            for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                userModel = snapshot.toObject(UserModel.class);
                                userName.setText(userModel.getName());
                                Picasso.get().load(userModel.getProfilePic()).into(profilePicture);
                            }
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.settings_change_password:
                requestChangePassword();
                break;
            case R.id.settings_sign_out:
                requestSignOut();
                break;
        }
    }

    private void requestSignOut() {
        auth.signOut();
        startActivity(new Intent(getContext(), LoginActivity.class));
        getActivity().finish();
    }

    private void requestChangePassword() {
        auth.sendPasswordResetEmail(userModel.getEmail())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), R.string.email_sent_txt, Toast.LENGTH_SHORT).show();
                        requestSignOut();
                    }
                });
    }
}
