package com.example.vehilealertapp;


import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import static android.view.View.GONE;


/**
 * A simple {@link Fragment} subclass.
 */
public class send_fragment extends DialogFragment implements View.OnClickListener {


    public send_fragment() {
        // Required empty public constructor
    }
    Victim vic;
    location lc;
    void setVic(Victim vic,Uri imageuri)
    {
        this.imageuri=imageuri;
        this.vic=vic;
    }
    void setLocation(location lc)
    {
        this.lc=lc;
    }
    TextView name;
    TextView cntno;
    TextView regno;
    TextView details;
    ImageView imageView;
    Uri imageuri;
    Button send;
    ProgressBar pb;
    DatabaseReference dr;
    StorageReference sr;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_send_fragment, container, false);
        name=(TextView) view.findViewById(R.id.name);
        cntno=(TextView) view.findViewById(R.id.cnt);
        regno=(TextView)view.findViewById(R.id.reg_no);
        details=(TextView)view.findViewById(R.id.details);
        imageView=(ImageView)view.findViewById(R.id.img);
        name.setText(vic.name);
        cntno.setText(vic.contact_no);
        regno.setText(vic.registration_number);
        details.setText(vic.details);
        if(imageuri!=null)
            imageView.setImageURI(imageuri);
        send=(Button)view.findViewById(R.id.snd);
        send.setOnClickListener(this);
        dr=FirebaseDatabase.getInstance().getReference("/Users/Victims");
        sr= FirebaseStorage.getInstance().getReference();
        pb=(ProgressBar)view.findViewById(R.id.progress);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        this.dismiss();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.snd:sendData();
                            break;
        }
    }

    private void sendData() {
        pb.setVisibility(View.VISIBLE);
        if(imageuri!=null) {
            sr.child(vic.registration_number).putFile(imageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        sr.child(vic.registration_number).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                            vic.imageurl=uri.toString();
                            sendOnlyData();
                            }
                        });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity().getApplicationContext(), "error occurred please try again", Toast.LENGTH_LONG).show();
                    pb.setVisibility(GONE);
                }
            });
        }
        else
            sendOnlyData();

    }
    void sendOnlyData() {
        final String key = dr.push().getKey();
        HashMap<String,Object> map=new HashMap<String,Object>();
        map.put("registration_number",vic.registration_number);
        map.put("contact_no",vic.contact_no);
        map.put("name",vic.name);
        map.put("details",vic.details);
        map.put("imageuri",vic.imageurl);
        map.put("location",lc);
        dr.child(key).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity().getApplicationContext(), "alert sent successfully", Toast.LENGTH_LONG).show();
                        pb.setVisibility(GONE);
                        removeTop();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity().getApplicationContext(), "error occurred please try again", Toast.LENGTH_LONG).show();
                pb.setVisibility(GONE);
            }
        });;

    }

    private void removeTop() {
        Intent intnt=new Intent(getActivity(),MainActivity.class);
        intnt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intnt);
    }
}
