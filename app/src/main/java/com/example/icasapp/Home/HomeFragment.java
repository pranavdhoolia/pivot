package com.example.icasapp.Home;

import android.content.Intent;
import android.os.Bundle;

import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.icasapp.Firebase.FirebaseHelper;
import com.example.icasapp.Profile.ProfileAdapter;
import com.example.icasapp.Profile.ProfileDisplayActivity;
import com.example.icasapp.R;
import com.example.icasapp.User.TestUser;
import com.example.icasapp.User.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */

public class HomeFragment extends Fragment {
    private String TAG = "mfc";
    private View homeView;

    // listview and adapter
    private ArrayList<TestUser> items;
    private ProfileAdapter itemsAdapter;
    private ListView listView;

    //Search
    private EditText query;

    //toggle
    private Group group;
    LinearLayout homeV;
    private boolean visible = true;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        homeView = inflater.inflate(R.layout.fragment_home, container, false);

        new Thread(new Runnable() {
            public void run() {
                setSearchToggle();
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                setListView();
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                setProfileSearch();
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                populateView(FirebaseHelper.getUser().getUid());
            }
        }).start();

        return homeView;
    }

    public View getHomeView() {
        return homeView;
    }

    //toggle functionality
    private void toggle() {
        homeV = homeView.findViewById(R.id.homeV);
        group.setVisibility(visibilityOf(visible));
        homeV.setVisibility(visibilityOf(!visible));
        visible = !visible;
    }
    private int visibilityOf(boolean visible) {
        return visible ? View.VISIBLE : View.GONE;
    }

    //set all functionality
    private void setSearchToggle() {
        group = (Group) homeView.findViewById(R.id.group);
        ImageButton searchInitButton = homeView.findViewById(R.id.initSearch);
        searchInitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
            }
        });
    }

    private void populateTest(User user) {
        // List<List<String>>
        List<List<String>> list = user.fetchList();

        // Parent layout
        LinearLayout parentLayout = (LinearLayout) homeView.findViewById(R.id.homeV);

        // Layout inflater

        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View view;

        // Profile Photo
        ImageView profilePhoto = new ImageView(getContext());
        float dpCalculation = getResources().getDisplayMetrics().density;

        // Customize image params
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER_HORIZONTAL;
        profilePhoto.setLayoutParams(params);

        profilePhoto.setScaleType(ImageView.ScaleType.CENTER);
        try {
            profilePhoto.getLayoutParams().height = (int) (150 * dpCalculation);
            profilePhoto.getLayoutParams().width = (int) (150 * dpCalculation);
        } catch(Exception e) {
            Log.e("mfc", e+"");
        }

        Glide.with(this).load(user.getProfilePhoto()).into(profilePhoto);
        parentLayout.addView(profilePhoto);

        // LinearLayout
        for (List<String> element : list){
//        for (int i=0; i<5; i++){
            // Add the text layout to the parent layout
            view = layoutInflater.inflate(R.layout.profilefieldelement, null);

            // In order to get the view we have to use the new view with text_layout in it
            TextView t1 = (TextView) view.findViewById(R.id.t1);
            t1.setText(element.get(0));
//            t1.setText("profilefieldelement");

            TextView t2 = (TextView) view.findViewById(R.id.t2);
            t2.setText(element.get(1));
//            t1.setText("ok");

            // Add the text view to the parent layout
            parentLayout.addView(view);
        }
    }

    private void populateView(String uid) {
        Log.e("populate", "Working 0");
        FirebaseHelper.getUserDetails(uid, new FirebaseHelper.CallbackObject<Map<String, Object>>() {
            @Override
            public void callbackCall(Map<String, Object> object) {
                populateTest(new User(object));

            }
        });
    }

    private void setListView() {
        // intitialize the array and listview adapter
        items = new ArrayList<>();
        listView = (ListView) homeView.findViewById(R.id.profile_menu);
        listView.setEmptyView(homeView.findViewById(R.id.emptyElement));
        itemsAdapter = new ProfileAdapter(getContext(), items);
        listView.setAdapter(itemsAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ProfileDisplayActivity.class);
                intent.putExtra("user", (TestUser) listView.getItemAtPosition(position));
                startActivity(intent);
            }
        });
    }

    private void setProfileSearch() {
        query = (EditText) homeView.findViewById(R.id.query);
        Button profileSearch = (Button) homeView.findViewById(R.id.profileSearch);
        profileSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (query.length() == 0) return;
                String queryValue =
                        query.getText().toString().trim();
                FirebaseHelper.getDocumentFromCollectionWhere(
                        "USER",
                        queryValue,
                        new FirebaseHelper.CallbackObject<List<Map<String, Object>>>() {
                            @Override
                            public void callbackCall(List<Map<String, Object>> object) {
                                items.clear();
                                Log.d("Callback", "" + object);
                                for (Map<String, Object> obj : object) {
                                    items.add(new TestUser(obj));
                                    Log.i("Downloaded List Item", obj.toString());
                                }
                                itemsAdapter.notifyDataSetChanged();
                            }
                        });
            }
        });
    }


}