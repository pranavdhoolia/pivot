package com.theenigma.pivot.Auth.StudentAuth;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;

import com.theenigma.pivot.Auth.FormHelper;
import com.theenigma.pivot.Auth.RegisterProgressActivity;
import com.theenigma.pivot.R;
import com.theenigma.pivot.Student;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import static com.theenigma.pivot.Student.student;

/**
 * A simple {@link Fragment} subclass.
 */
public class StudentRegisterCredentialFragment extends Fragment {

    AppCompatEditText inputRegNo;
    AppCompatButton nextButton;
    String name, email, regNo, fetchedRegNo;
    String[] emailList;
    FormHelper formHelper;
    FirebaseFirestore db;
    ProgressDialog progressDialog;
    CollectionReference query;
    private View studentRegView;


    public StudentRegisterCredentialFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        studentRegView = inflater.inflate(R.layout.fragment_student_register_credential, container, false);

        nextButton = studentRegView.findViewById(R.id.nextButton);
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setProgress(0);


        inputRegNo = studentRegView.findViewById(R.id.inputRegNo);
        regNo = "";
        inputRegNo.setText("");

        db = FirebaseFirestore.getInstance();
        query = db.collection("Authentication");

        formHelper = new FormHelper();
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                regNo = "";
                regNo = inputRegNo.getText().toString();
                if (!formHelper.validateRegNo(regNo)) {
                    Toast.makeText(view.getContext(), "INVALID REGISTRATION NUMBER", Toast.LENGTH_LONG).show();

                } else {
                    authenticateRegNo();

                }
            }
        });


        return studentRegView;
    }

    private void authenticateRegNo() {
        Log.i("msg", "USER INPUT:" + regNo);

        progressDialog.show();


        readData(new FireStoreCallback() {
            @Override
            public void onCallback(Student user) {
                progressDialog.hide(); //HIDE PROGRESS BAR

                name = user.getName();
                email = user.getEmail();


                if (email.contains(";")) {
                    emailList = email.split(";");
                    Log.i("msg", emailList[0] + "#" + emailList[1]);

                    new AlertDialog.Builder(getContext())
                            .setCancelable(false)
                            .setTitle("We have more than one mail of yours associated with ICAS:")
                            .setSingleChoiceItems(emailList, -1, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    email = emailList[i];
                                }
                            })
                            .setPositiveButton("SELECT", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Toast.makeText(getContext(), email, Toast.LENGTH_SHORT).show();
                                    store();
                                    updateUI();

                                }
                            })
                            .create()
                            .show();
                } else {
                    store();
                    updateUI();
                }


            }
        });
    }

    public void store() {
        student.setEmail(email);
        student.setName(name);
        student.setUserType("STUDENT");
        student.setRegNo(regNo);
    }

    public void updateUI() {
        Log.i("msg", "UPDATE UI REG NO:" + student.getRegNo());
        UserRegCredentialFragment.inputFullName.setText(name);
        UserRegCredentialFragment.inputEmail.setText(email);
        RegisterProgressActivity.pager.setCurrentItem(1);
        inputRegNo.setText("");
        regNo = "";
        RegisterProgressActivity.stepView.go(1, true);
        RegisterProgressActivity.i++;
    }

    private void readData(final FireStoreCallback fireStoreCallback) {
        student = null;
        query
                .whereEqualTo("regNo", regNo)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                student = documentSnapshot.toObject(Student.class);
                            }
                            //CALLBACK
                            fireStoreCallback.onCallback(student);
                        } else {
                            progressDialog.hide();
                            Toast.makeText(getContext(), "REGISTRATION NO NOT FOUND.", Toast.LENGTH_LONG).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("msg", "REGISTRATION NUMBER NOT FOUND");
            }
        });

    }

    private interface FireStoreCallback {
        void onCallback(Student user);
    }

}
