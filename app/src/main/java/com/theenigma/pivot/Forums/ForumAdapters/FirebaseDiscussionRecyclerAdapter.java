package com.theenigma.pivot.Forums.ForumAdapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.theenigma.pivot.Forums.ForumActivities.QuestionsActivity;
import com.theenigma.pivot.ObjectClasses.DiscussionTopic;
import com.theenigma.pivot.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Calendar;
import java.util.Date;

import static com.theenigma.pivot.Forums.ForumFragment.Category;
import static com.theenigma.pivot.Forums.ForumFragment.i_d;

public class FirebaseDiscussionRecyclerAdapter extends FirestoreRecyclerAdapter<DiscussionTopic, FirebaseDiscussionRecyclerAdapter.DiscussionHolder> {

    private Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private ListenerRegistration listenerRegistration;
    private String buffer;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public FirebaseDiscussionRecyclerAdapter(@NonNull FirestoreRecyclerOptions<DiscussionTopic> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final DiscussionHolder discussionHolder, final int i, @NonNull final DiscussionTopic discussionTopic) {

        final String blogPostId = getSnapshots().getSnapshot(i).getId();

        //calling method from Discussion Topic class
        final String content=discussionTopic.getContent();
        discussionHolder.setContentText(content);

        final String currentUser= firebaseAuth.getInstance().getUid();

        int n = discussionTopic.getQuestion();
        discussionHolder.setCommentCount(Integer.toString(n));

        discussionHolder.delete.setVisibility(View.GONE);

        final int question=discussionTopic.getQuestion();
        discussionHolder.setCommentCount(Integer.toString(question));

        String url=discussionTopic.getImage_url();
        discussionHolder.setUrl(url);

        long currentTime = Calendar.getInstance().getTime().getTime();
        long uploadtime = discussionTopic.getTimestamp().getTime();
        long diff=currentTime-uploadtime;
        int value= (int) (diff/(3.6*(Math.pow(10,6))));

        if(value>24)
        {
            String dateString = DateFormat.format("MM/dd/yyyy", new Date(uploadtime)).toString();
            discussionHolder.setTime(dateString);
        }
        else
        {
            String dateString = DateFormat.format("hh:mm", new Date(uploadtime)).toString();
            discussionHolder.setTime(dateString);
        }



        discussionHolder.blogPost.setOnClickListener(new View.OnClickListener() {
                                                         @Override
                                                         public void onClick(View v) {
                                                             Intent commentIntent=new Intent(context, QuestionsActivity.class);
                                                             commentIntent.putExtra("post_id", blogPostId);
                                                             //Category is passed between activities so that the value can be used
                                                             commentIntent.putExtra("Category",Category);
                                                             commentIntent.putExtra("ID",i_d);
                                                             commentIntent.putExtra("Topic",content);
                                                             context.startActivity(commentIntent);
                                                         }
                                                     });

                firebaseFirestore.collection("USER").document(currentUser).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@androidx.annotation.NonNull Task<DocumentSnapshot> task) {
                        buffer = (String) task.getResult().get("buffer");
                        Log.i("BUFFER", buffer);
                        //if current user is logged
                        if (discussionTopic.getUser_id().equals(currentUser) || buffer.equals("4.0")) {
                            discussionHolder.delete.setVisibility(View.VISIBLE);
                        } else
                            discussionHolder.delete.setVisibility(View.GONE);
                    }
                });


        //deletion
        discussionHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete(i);
                notifyDataSetChanged();
            }
        });

    }

    @NonNull
    @Override
    public DiscussionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        firebaseFirestore = firebaseFirestore.getInstance();
        View v =  LayoutInflater.from(parent.getContext()).inflate(R.layout.discussion_list_item,parent,false);
        return new DiscussionHolder(v);
    }

    class DiscussionHolder extends RecyclerView.ViewHolder{
        //used to put values in card view elements
        private View mView;
        private CardView blogPost;
        private TextView contentView;
        private ImageView imageView;
        private ImageView delete;
        private TextView commentCount;
        private TextView Time;

        public DiscussionHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;
            delete=mView.findViewById(R.id.delete);
            blogPost=mView.findViewById(R.id.main_blog_post);
        }

        public void setContentText(String text){
            contentView=mView.findViewById(R.id.forum_topic);
            contentView.setText(text);
        }
        public void setUrl(String url){
            imageView=mView.findViewById(R.id.forum_image);
            Glide.with(context).load(url).into(imageView);

        }
        public void setCommentCount(String count)
        {
            commentCount=mView.findViewById(R.id.blog_comment_count);
            String text=count+" Questions ";
            commentCount.setText(text);
        }
        public void setTime(String time)
        {
            Time=mView.findViewById(R.id.time);
            Time.setText(time);
        }
    }

    public void delete(final int position)
    {
        new AlertDialog.Builder(context)
                .setTitle("Deleting the topic")
                .setMessage("Are you sure you want to delete this entry?")

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                        getSnapshots().getSnapshot(position).getReference().delete();
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    }
