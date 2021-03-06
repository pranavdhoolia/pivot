package com.theenigma.pivot.Forums;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.theenigma.pivot.Forums.ForumAdapters.FirebaseDiscussionRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.theenigma.pivot.Forums.ForumActivities.NewDiscussionActivity;
import com.theenigma.pivot.MainActivity;
import com.theenigma.pivot.ObjectClasses.DiscussionTopic;
import com.theenigma.pivot.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;

/**

 * A simple {@link Fragment} subclass.

 */

public class  ForumFragment extends Fragment implements AdapterView.OnItemClickListener {

   private String stream;
   private String semester;
   private boolean isFirstPageFirstLoad = false;
   public static String i_d;
   public static String Category;
   private  ArrayList<String> subject;
   private ArrayList arr;
   private static String buffer;
   private String email;
   int i = 1;
   public static DocumentReference documentReference;

   private FloatingActionButton addPost;
   private Spinner spinner;
   private ProgressDialog progressBar;

   private FirebaseFirestore firebaseFirestore;
   public static CollectionReference collectionReference;
   public static Query query;
   private FirebaseAuth firebaseAuth;

   private FirebaseDiscussionRecyclerAdapter adapter;

   private View view;

    public ForumFragment() {

    }


    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_forum, container, false);

        MainActivity.isBack = false;

        addPost = view.findViewById(R.id.addPost);


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        final String currentUser = firebaseAuth.getCurrentUser().getUid();
        i_d = null;
        addPost.setVisibility(View.INVISIBLE);
        addPost.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
                Intent postBtnIntent = new Intent(getActivity(), NewDiscussionActivity.class);
                //Category and document id that is passed between activities so that the value can be used globally
                postBtnIntent.putExtra("Category",Category);
                postBtnIntent.putExtra("ID",i_d);
                startActivity(postBtnIntent);
            }
        });



        //gets the stream and semester of the user and passes into findDocumentId
        firebaseFirestore.collection("USER").document(firebaseAuth.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        //progress bar is hidden after Snapshot is set to query
                        subject=new ArrayList<>();
                        firebaseFirestore.collection("USER").document(currentUser).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@androidx.annotation.NonNull Task<DocumentSnapshot> task) {
                                buffer = task.getResult().get("buffer").toString();
                                Log.i("buffer",buffer);
                                //if the user is not teacher or alumni
                                if(buffer.equals("1.0")||buffer.equals("1.1")||buffer.equals("1.2")) {
                                    semester = document.get("semester").toString();
                                    stream = document.get("stream").toString();
                                    Log.i("semester", semester);
                                    Log.i("stream", stream);
                                    findSubjectsOfStudent(semester, stream, view);
                                }
                                //sets the document id that contains the subjects of the particular stream and semester
                                if(buffer.equals("2.0")){
                                    //if not student.
                                    email = document.get("email").toString();
                                    setSubjectArrayFaculty(email);
                                    Log.i("email",email);
                                }
                                else
                                {
                                    subject.add("General");
                                    subject.add("Alumni");
                                    setSubjectArrayOthers();
                                }
                            }
                        });
                }}
            }
        });




        //Array list inflates spinner afterwards
        //first category General is added initially


        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
        if(isFirstPageFirstLoad)
            adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(isFirstPageFirstLoad)
            adapter.stopListening();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    public void findSubjectsOfStudent(String semester, String stream,final View view) {

            //the document id is retreived which contains the subjects of the stream and semester
            Query query = firebaseFirestore.collection("Specific").whereEqualTo("semester", semester).whereEqualTo("stream", stream);



            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {

                             subject  = (ArrayList) document.get("subjects");
                            //set array of subjects that inflates menu later
                            setSubjectArray(view);

                        }
                    }
                }
            });
    }

    public void setSubjectArrayFaculty(String email)
    {
        firebaseFirestore.collection("USER").whereEqualTo("email", email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@androidx.annotation.NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        //overwrites subject array list with this array list
                        subject = (ArrayList) document.get("subjects");
                        Log.i("Subjects of Faculty", String.valueOf(subject));
                    }

                    if(subject.isEmpty())
                    {
                        subject = new ArrayList<>();
                    }


                    subject.add(0,"General");
                    subject.add("Alumni");
                    spinner = view.findViewById(R.id.subjects);

                        //creates new adapter with inflated subject
                        ArrayAdapter<String> a = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, subject);
                        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        spinner.setAdapter(a);


                        //when a spinner item is selected, snapshot is added to its particular category item
                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                                Category = subject.get(position);
                                setAddPost();
                                firebaseFirestore.collection("Specific").document("parent").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@androidx.annotation.NonNull Task<DocumentSnapshot> task) {
                                        i_d = (String) task.getResult().get(Category);
                                        setFirestoreReference(firebaseFirestore,i_d,"q");
                                        //add snapshot to query
                                        addSnapshotToQuery(query);
                                    }
                                });

                            }


                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }

                        });


                }
            }
        });
    }



    public void setSubjectArray(final View view) {

            subject.add(0,"General");

                subject.add("Alumni");
                spinner = view.findViewById(R.id.subjects);

                //creates new adapter with inflated subject
                ArrayAdapter<String> a = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, subject);
                a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                spinner.setAdapter(a);

                //when a spinner item is selected, snapshot is added to its particular category item
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {



                        Category = subject.get(position);
                        setAddPost();
                        firebaseFirestore.collection("Specific").document("parent").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@androidx.annotation.NonNull Task<DocumentSnapshot> task) {
                                i_d = (String) task.getResult().get(Category);
                                setFirestoreReference(firebaseFirestore,i_d,"q");
                                //add snapshot to query
                                addSnapshotToQuery(query);
                            }
                        });

                    }


                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }

                });

    }

    void setSubjectArrayOthers()
    {
        spinner = view.findViewById(R.id.subjects);
        ArrayAdapter<String> a = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, subject);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(a);

        //when a spinner item is selected, snapshot is added to its particular category item
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {


                try{
                Category = subject.get(position);
                setAddPost();
                firebaseFirestore.collection("Specific").document("parent").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@androidx.annotation.NonNull Task<DocumentSnapshot> task) {
                        //id will be null if category is general
                        i_d = (String) task.getResult().get(Category);
                        setFirestoreReference(firebaseFirestore,i_d,"q");
                        //add snapshot to query
                        addSnapshotToQuery(query);
                    }
                });

            }
            catch(Exception e)
            {

            }}


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });
    }

    void addSnapshotToQuery(Query query)
    {

        //Query is created according to the timestamp
        Query query1 = query.orderBy("timestamp",Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<DiscussionTopic> options = new FirestoreRecyclerOptions.Builder<DiscussionTopic>()
                .setQuery(query1,DiscussionTopic.class)
                .build();
        adapter = new FirebaseDiscussionRecyclerAdapter(options);

        Context context = getContext();
        RecyclerView recyclerView = view.findViewById(R.id.discussion_list_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(true);

        adapter.startListening();
        isFirstPageFirstLoad=true;

    }



    public static void setFirestoreReference(FirebaseFirestore firebaseFirestore,String ID,String type)
    {
        if(type.equals("c")) {
            if (Category.equals("General") || Category.equals("Alumni")) {
                Log.i("LOL","SUCC");
                collectionReference = firebaseFirestore.collection("General").document(Category).collection("Posts");
            } else {

                collectionReference = firebaseFirestore.collection("Specific").document(ID).collection("Subjects").document(Category).collection("Posts");


            }
        }
        if(type.equals("q"))
        {
            if (Category.equals("General") || Category.equals("Alumni")) {
                query = firebaseFirestore.collection("General").document(Category).collection("Posts");
            } else {


                    query = firebaseFirestore.collection("Specific").document(ID).collection("Subjects").document(Category).collection("Posts");

            }
        }
    }

    @SuppressLint("RestrictedApi")
    void setAddPost(){

        addPost.setVisibility(View.INVISIBLE);
        Log.i("CAT",Category);
        Log.i("CAT buf",buffer);
        if(Category.equals("Alumni")){
            if(buffer.equals("4.0")||buffer.equals("3.0")) {
                Log.i("CAT", "Invoked Alum");
                addPost.setVisibility(View.VISIBLE);
            }
        }
        else {
            if(!Category.equals("General")){
               if(!buffer.equals("1.0")) {
                   Log.i("CAT", "Invoked Subject");
                   addPost.setVisibility(View.VISIBLE);
               }
        }
        else
            {
                Log.i("CAT","Invoked General");
                addPost.setVisibility(View.VISIBLE);
            }
        }
    }
}