package com.theenigma.pivot.DeveloperOptions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.theenigma.pivot.MainActivity;
import com.theenigma.pivot.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class setUserPriveledge extends AppCompatActivity {
    AutoCompleteTextView setText;
    FirebaseFirestore firebaseFirestore;
    ArrayList names;
    RadioGroup radioGroup;
    Button setPriviledge;
    RadioButton radioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_user_priveledge);

        setText = findViewById(R.id.searchText);
        names = new ArrayList();
        radioGroup = findViewById(R.id.radioGroup);
        setPriviledge = findViewById(R.id.setPriveledge);

        final HashMap map = new HashMap();
        map.put("Student","1.0");
        map.put("CR","1.1");
        map.put("Council","1.2");
        map.put("Faculty","2.0");
        map.put("ALUMNI","3.0");
        map.put("Admin","4.0");


        try {

            final ArrayAdapter<String> autoComplete = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

            firebaseFirestore = FirebaseFirestore.getInstance();

            firebaseFirestore.collection("USER").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    for (DocumentSnapshot documentSnapshot : task.getResult()) {

                        autoComplete.add(documentSnapshot.get("name").toString());
                    }
                }
            });

            setText.setAdapter(autoComplete);


            // Uncheck or reset the radio buttons initially
            radioGroup.clearCheck();

            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    radioButton = group.findViewById(checkedId);
                }
            });

            setPriviledge.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String name = setText.getText().toString();
                    int selectedId = radioGroup.getCheckedRadioButtonId();

                    if (selectedId == -1 || name.equals("")) {
                        Toast.makeText(getApplicationContext(),
                                "Nothing has been selected",
                                Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        firebaseFirestore.collection("USER").whereEqualTo("name", name).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                for (DocumentSnapshot documentSnapshot : task.getResult()) {

                                        documentSnapshot.getReference().update("buffer",map.get(radioButton.getText())).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                            }
                                        });
                                }
                            }
                        });
                    }

                }
            });
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(),"Something went wrong",Toast.LENGTH_LONG);
            Log.e("ERROR",e.getMessage());
        }
    }
}
