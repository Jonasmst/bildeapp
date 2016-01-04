package com.jonasmst.img.bilde;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // TODO: Resolve 'NetworkOnMainThreadException' by fetching mail as an AsyncTask

    Button checkMailButton;
    ImageView mainImage;
    LinearLayout imageRow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Resolve views
        checkMailButton = (Button) findViewById(R.id.Main_checkMailButton);
        mainImage = (ImageView) findViewById(R.id.Main_imageView);
        imageRow = (LinearLayout) findViewById(R.id.Main_scrollview_linearlayout);

        // Setup buttons
        fix_onclicks();
    }

    private void fix_onclicks() {
        checkMailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Checking mail", Toast.LENGTH_SHORT).show();
                AsyncEmailChecker checker = new AsyncEmailChecker(getApplicationContext());
                checker.execute();
            }
        });
    }

    private class AsyncEmailChecker extends AsyncTask<String, Void, ArrayList<EmailEntry>> {
        private String tag = "asyncemailchecker";
        private Context context;

        public AsyncEmailChecker(Context c) {
            this.context = c;
        }

        @Override
        protected ArrayList<EmailEntry> doInBackground(String... params) {

            // Create ReadEmail instance and check mail
            String host = "pop.gmail.com";
            String storeType = "pop3";
            String username = "grosspapi.images@gmail.com";
            String password = "grosspapi";
            ReadEmail emailReader = new ReadEmail(context, host, storeType, username, password);

            // Get email
            ArrayList<EmailEntry> messages = emailReader.checkMail();

            return messages;
        }

        protected void onPostExecute(ArrayList<EmailEntry> messages) {
            //super.onPostExecute(messages);
            Toast.makeText(getApplicationContext(), "Message.length: " + messages.size(), Toast.LENGTH_SHORT).show();

            // Check length of ArrayList
            if (messages.size() > 0) {

                ArrayList<Bitmap> images = new ArrayList<>();

                // Add message content to TextView
                for(EmailEntry email : messages) {
                    if(email.getImageAttachment() == null) {
                        continue;
                    }

                    // HERE: Check if there's an image attachment, and if so, send to ImageView
                    File imageFile = email.getImageAttachment();
                    Bitmap imageBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

                    images.add(imageBitmap);
                }

                // HERE: Add one image as background, the rest to horizontalscrollview
                if(images.size() > 0) {

                    Log.v(tag, "Number of images: " + images.size());

                    // Set first image as background
                    mainImage.setImageBitmap(images.get(0));

                    // Set the rest of the images to horizontalscrollview
                    for(int i = 1; i < images.size(); i++) {
                        // Create new imageview, set the background and add it to horizontal scrollview
                        ImageView tempImageView = new ImageView(getApplicationContext());
                        tempImageView.setImageBitmap(images.get(i));
                        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        tempImageView.setLayoutParams(llParams);

                        imageRow.addView(tempImageView);
                    }
                } else {
                    Log.v(tag, "There are messages, but no image attachments");
                }

            } else {
                // Say "No messages in inbox"
                Log.v(tag, "onPost: No messages in inbox");
            }

            Log.v(tag, "End of onPost");
        }
    }

}

/*
======
 IDEA
======
Main activity: Basically one huge imageview (preferably zoomable) with a horizontal scrollview below, containing
older images (ordered by date received).

Background processes: Basically a Service or similar, checking a predefined e-mail account regularly
(for testing purposes: Once every 5 seconds. More realistically: Every 30 mins or so). If new mail, check if:
    1. The subject contains a certain string (predefined - to filter out spam and other mail).
    2. The email contains an attachment.
    3. The attachment is an image format (regex lookup on extension or something)
If new mail is 'approved', download image attachment and set as background, push previous image to queue (horizontal scrollview)
 */