package com.example.wia2007mad.AllModules;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.wia2007mad.R;
import com.example.wia2007mad.databinding.DialogUpdateProfileBinding;
import com.example.wia2007mad.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;


public class ProfileFragment extends Fragment {

   // FirebaseDatabase firebaseDatabase;
   // DatabaseReference databaseReference;
   // StorageReference storageReference;
    String storagepath = "Users_Profile_Cover_image/";
    ImageView set;
    ProgressDialog pd;
    private static final int CAMERA_REQUEST = 100;
    private static final int STORAGE_REQUEST = 200;
    private static final int IMAGEPICK_GALLERY_REQUEST = 300;
    private static final int IMAGE_PICKCAMERA_REQUEST = 400;
    String cameraPermission[];
    String storagePermission[];
    Uri imageuri;
    String profileOrCoverPhoto;
    private static AlertDialog dialog = null;

    //important attribute
    FirebaseFirestore firebaseFirestore;
    DocumentReference userreference;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    String newusernameinput,newphonenumberinput;
    FragmentProfileBinding binding;
    Context fragmentContext;
    Dialog dialogupdateprofiledetails;
    Dialog dialogconfirmpassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding= FragmentProfileBinding.inflate(inflater,container,false);
        fragmentContext=getContext();
        dialogconfirmpassword=new Dialog(fragmentContext);
        dialogupdateprofiledetails=new Dialog(fragmentContext);

        pd = new ProgressDialog(getContext());
        pd.setCanceledOnTouchOutside(false);

        //firebaseDatabase=FirebaseDatabase.getInstance();
      //  storageReference= FirebaseStorage.getInstance().getReference();
       // databaseReference=firebaseDatabase.getReference("Users");
        cameraPermission=new String[]{
                Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        storagePermission=new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };


        //1st section
        //get database documents
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();
        assert firebaseUser != null;
        String uid=firebaseUser.getUid();
        firebaseFirestore=FirebaseFirestore.getInstance();
        userreference=firebaseFirestore.collection("users").document(uid);
        userreference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    NormalUser user=documentSnapshot.toObject(NormalUser.class);
                    if(user!=null){
                        String profileusername=binding.profileusername.getText().toString()+" "+user.getUsername();
                        binding.profileusername.setText(profileusername);
                        String profileemail=binding.profileemail.getText().toString()+" "+user.getEmail();
                        binding.profileemail.setText(profileemail);
                        String profilephonenumber=binding.profilephonenumber.getText().toString()+" "+user.getPhone_number();
                        binding.profilephonenumber.setText(profilephonenumber);
                    }
                }
            }
        });

        binding.ChangeProfileDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupToChangeProfileDetails();
            }
        });
        // 2nd section retrieve
        //3rd section , using the dialog to do changes if password is betul ^_^


        return binding.getRoot();
    }

    private void showPopupToChangeProfileDetails() {
        // Create the dialog
        dialogupdateprofiledetails.setContentView(R.layout.dialog_update_profile);
        dialogupdateprofiledetails.setCancelable(false);
        // Initialize the views
        EditText newusername=dialogupdateprofiledetails.findViewById(R.id.newusername),newphonenumber=dialogupdateprofiledetails.findViewById(R.id.newphonenumber);
        TextView cancelupdate=dialogupdateprofiledetails.findViewById(R.id.cancelupdate),confirmupdate=dialogupdateprofiledetails.findViewById(R.id.updateprofiledetailsconfirmbutton);
        // Set text or other properties if needed

        // Set the close button action
        cancelupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogupdateprofiledetails.dismiss();
            }
        });
        confirmupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newusernameinput=newusername.getText().toString();
                newphonenumberinput=newphonenumber.getText().toString();
                if(newusernameinput.isEmpty()||newphonenumberinput.isEmpty()){
                    Toast.makeText(getContext(),"Please update your profile details...",Toast.LENGTH_SHORT).show();
                }else{
                    showPopupToConfirmPasswordToApproveChanges();
                }
            }
        });
        // Set the dialog background to transparent
        if (dialogupdateprofiledetails.getWindow() != null) {
            dialogupdateprofiledetails.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        // Show the popup dialog
        dialogupdateprofiledetails.show();
    }

    private void showPopupToConfirmPasswordToApproveChanges() {
        // Create the dialog
        dialogconfirmpassword.setContentView(R.layout.dialog_confirm_password);
        dialogconfirmpassword.setCancelable(false);

        // Initialize the views
        EditText confirmpassword=dialogconfirmpassword.findViewById(R.id.confirmpassword);
        TextView backconfirmpassword=dialogconfirmpassword.findViewById(R.id.backconfirmpassword),proceedconfirmpassword=dialogconfirmpassword.findViewById(R.id.proceedconfirmpassword);
        backconfirmpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogconfirmpassword.dismiss();
            }
        });
        proceedconfirmpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String confirmpasswordinput=confirmpassword.getText().toString();
                if(firebaseUser!=null){
                    AuthCredential credential= EmailAuthProvider.getCredential(firebaseUser.getEmail(),confirmpasswordinput);
                    firebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            Map<String,Object> updateddata=new HashMap<>();
                            updateddata.put("username",newusernameinput);
                            updateddata.put("phone_number",newphonenumberinput);

                            //update data
                            userreference.update(updateddata).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getContext(), "Personal Details Updated Successfully !", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), "Error updating user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else{
                            Toast.makeText(getContext(), "Wrong Credentials..." , Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                dialogconfirmpassword.dismiss();
                dialogupdateprofiledetails.dismiss();
            }
        });
        // Set the dialog background to transparent
        if (dialogconfirmpassword.getWindow() != null) {
            dialogconfirmpassword.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        // Show the popup dialog
        dialogconfirmpassword.show();
    }
}
