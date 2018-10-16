package com.example.hp.vguide;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.google.firebase.auth.FirebaseAuth;

import static android.app.Activity.RESULT_OK;


//import static android.support.v4.provider.FontsContractCompat.FontRequestCallback.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Profile.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Profile#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Profile extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ListView mlistview;
    private FirebaseAuth mAuth;
    private ImageButton mProfileImage;
    private EditText mProfileName;
    private Button mProfileStrBtn;
    private DatabaseReference mUsersDatabase;
    private static final int GALLERY_REQUEST = 1;
    private Uri mImageUri = null;
    private StorageReference mStorage;
    private DatabaseReference mDatabase;
    private ProgressDialog mProgress;
    private StorageReference mStorageRef;

    private Toolbar mToolbar;
   //private Firebase mRootRef;
    private TextView mNameView;
    private DatabaseReference mDatabase1;
    private ImageView mImageView;
    private DatabaseReference mDatabase2;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public Profile() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Profile.
     */
    // TODO: Rename and change types and number of parameters
    public static Profile newInstance(String param1, String param2) {
        Profile fragment = new Profile();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);


        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();



        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        mDatabase1 = FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("displayname");
        mDatabase2 = FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("image");


        mImageView = (ImageView) v.findViewById(R.id.imageView2);
        mDatabase2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String image1 = dataSnapshot.getValue().toString();
                Context c = getActivity().getApplicationContext();
                Picasso.with(c).load(image1).into(mImageView);






            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mNameView = (TextView) v.findViewById(R.id.profile_name);
        mDatabase1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name1 = dataSnapshot.getValue().toString();
                mNameView.setText(name1);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });





        mProfileImage = (ImageButton) v.findViewById(R.id.imageButton);
        mProfileName =(EditText) v.findViewById(R.id.editText);
        mProfileStrBtn=(Button) v.findViewById(R.id.button2);


        //mProgress = new ProgressDialog(this);

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent gallery =new Intent(Intent.ACTION_GET_CONTENT);
                gallery.setType("image/*");
                startActivityForResult(gallery, GALLERY_REQUEST);


            }
        });

        mProfileStrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startPosting();

            }
        });







    return v;
    }





    private void startPosting() {

        //mProgress.setMessage("uploaded");
        //mProgress.show();

        final String title_val = mProfileName.getText().toString().trim();
        mDatabase.child("displayname").setValue(title_val);



        StorageReference filepath = mStorage.child("VGuide").child(mImageUri.getLastPathSegment());

        filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    mAuth = FirebaseAuth.getInstance();
                    String userId = mAuth.getCurrentUser().getUid();

                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
                    DatabaseReference mData = mDatabase;
                    mDatabase.child("image").setValue(downloadUrl.toString());
                    mDatabase.child("displayname").setValue(title_val);
                    // Map<String, String> userData = new HashMap<String, String>();


                    // String value = mProfileName.getText().toString();
                    //Firebase childref = mRootRef.child("Users").child(userId).child("username");
                    //childref.setValue(value);




                    // DatabaseReference newPost = mDatabase.push();
                    // newPost.child("username").setValue(title_val);
                    // newPost.child("image").setValue(downloadUrl.toString());
                    //mProgress.dismiss();


                }
            });


    }






























    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        getActivity();
       // for (Fragment fragment : getChildFragmentManager().getFragments()) {
            // fragment.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            mImageUri = data.getData();
            mProfileImage.setImageURI(mImageUri);
            Uri uri =data.getData();
            StorageReference filepath =mStorage.child("Photos").child(uri.getLastPathSegment());
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {



                }
            });

        }
    }
}


/*public class Profile extends AppCompatActivity {

    private ListView mlistview;
    private FirebaseAuth mAuth;
    private ImageButton mProfileImage;
    private EditText mProfileName;
    private Button mProfileStrBtn;
    private DatabaseReference mUsersDatabase;
    private static final int GALLERY_REQUEST = 1;
    private Uri mImageUri = null;
    private StorageReference mStorage;
    private DatabaseReference mDatabase;
    private ProgressDialog mProgress;
    private Toolbar mToolbar;
    //private Firebase mRootRef;
    private TextView mNameView;
    private DatabaseReference mDatabase1;
    private ImageView mImageView;
    private DatabaseReference mDatabase2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);
        //toolbar
       // mToolbar = (Toolbar) findViewById(R.id.main_app_bar);
       // setSupportActionBar(mToolbar);
        //getSupportActionBar().setTitle("User Profile");




        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        mDatabase1 = FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("username");
        mDatabase2 = FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("image");


        mImageView = (ImageView) findViewById(R.id.imageView2);
        mDatabase2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String image1 = dataSnapshot.getValue().toString();
               // Picasso.with(Profiles.this).load(image1).into(mImageView);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mNameView = (TextView) findViewById(R.id.textView2);
        mDatabase1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name1 = dataSnapshot.getValue().toString();
                mNameView.setText(name1);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });





        mProfileImage = (ImageButton) findViewById(R.id.imageButton);
        mProfileName =(EditText) findViewById(R.id.editText);
        mProfileStrBtn=(Button) findViewById(R.id.button2);
        mProgress = new ProgressDialog(this);

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent gallery =new Intent(Intent.ACTION_GET_CONTENT);
                gallery.setType("image/*");
                startActivityForResult(gallery, GALLERY_REQUEST);


            }
        });

        mProfileStrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startPosting();

            }
        });


        // DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("contacts");
        // mlistview = (ListView)findViewById(R.id.grouplist2);


        // FirebaseListAdapter<String> firebaseListAdapter = new FirebaseListAdapter<String>(
        // this,
        // String.class,
        // android.R.layout.simple_list_item_1,
        // mDatabase
        /// ) {
        // @Override
        //protected void populateView(View view, String model, int i) {

        // TextView textview = (TextView)view.findViewById(android.R.id.text1);
        // textview.setText(model);

    }
    // };
    // mlistview.setAdapter(firebaseListAdapter);


    // }

    private void startPosting() {

        mProgress.setMessage("uploaded");
        mProgress.show();

        final String title_val = mProfileName.getText().toString().trim();

        if(!TextUtils.isEmpty(title_val) && mImageUri != null){

            StorageReference filepath = mStorage.child("Hudl_Profiles").child(mImageUri.getLastPathSegment());

            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    mAuth = FirebaseAuth.getInstance();
                    String userId = mAuth.getCurrentUser().getUid();

                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
                    DatabaseReference mData = mDatabase;
                    mDatabase.child("image").setValue(downloadUrl.toString());
                    mDatabase.child("username").setValue(title_val);
                    // Map<String, String> userData = new HashMap<String, String>();


                    // String value = mProfileName.getText().toString();
                    //Firebase childref = mRootRef.child("Users").child(userId).child("username");
                    //childref.setValue(value);




                    // DatabaseReference newPost = mDatabase.push();
                    // newPost.child("username").setValue(title_val);
                    // newPost.child("image").setValue(downloadUrl.toString());
                    mProgress.dismiss();

                }
            });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            mImageUri = data.getData();
            mProfileImage.setImageURI(mImageUri);
        }
    }














}*/

