package com.jonasmst.img.bilde;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.io.IOException;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;

/**
 * Created by jonas on 25.12.15.
 * This class is dedicated to reading email from a predefined account.
 */
public class ReadEmail {

    // TODO: Create constructor to house email attributes, such as hostname and user info.
    // TODO: Implement receive_mail as a get-function to be called from an Activity

    private String host;
    private String mailStoreType;
    private String username;
    private String password;
    private String tag = "reademail";
    private Context context;

    public ReadEmail(Context c, String hostname, String storeType, String user, String pwd) {
        this.host = hostname;
        this.mailStoreType = storeType;
        this.username = user;
        this.password = pwd;
        this.context = c;
    }

    public ArrayList<EmailEntry> checkMail() {

        // Container for storing email entries
        ArrayList<EmailEntry> emails = new ArrayList<EmailEntry>();

        try {
            Properties properties = new Properties();
            properties.setProperty("mail.store.protocol", "imaps");

            Session session = Session.getInstance(properties, null);
            Store store = session.getStore();
            store.connect("imap.gmail.com", "grosspapi.images@gmail.com", "grosspapi");

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.getMessages();

            // Loop messages
            for(int i = 0; i < messages.length; i++) {
                // Surely, this can be done as an enhanced for-loop
                Message message = messages[i];

                // Create email entry for this message
                EmailEntry entry = new EmailEntry();
                entry.setSubject(message.getSubject());
                entry.setFrom(message.getFrom().toString());
                entry.setText("This is text");

                // The ugly bit about checking MIME-types
                //check if the content is plain text
                if (message.isMimeType("text/plain")) {
                    entry.setText(message.getContentType().toString());
                }

                //check if the content has attachment
                else if (message.isMimeType("multipart/*")) {
                    //Multipart multipart = (Multipart) message.getContent();
                    Multipart multipart = null;

                    Object content = message.getContent();
                    if(content instanceof Multipart) {
                        multipart = (Multipart) message.getContent();
                    } else {
                        Log.v(tag, "NOT MULTIPART: " + message.getContentType());
                    }

                    for (int x = 0; x < multipart.getCount(); x++) {
                        BodyPart bodyPart = multipart.getBodyPart(x);

                        String disposition = bodyPart.getDisposition();

                        if(disposition != null && (disposition.equalsIgnoreCase("ATTACHMENT"))) {
                            DataHandler datahandler = bodyPart.getDataHandler();
                            InputStream is = datahandler.getInputStream();
                            String filePath = context.getFilesDir().getPath().toString() + bodyPart.getFileName();

                            File file = new File(filePath);
                            FileOutputStream fos = new FileOutputStream(file);
                            byte[] buf = new byte[4096];
                            int bytesRead;
                            while((bytesRead = is.read(buf)) != -1) {
                                fos.write(buf, 0, bytesRead);
                            }
                            fos.close();
                            entry.setImageAttachment(file);
                        }
                    }
                } else {
                    Log.v(tag, "MimeType is not plain or multipart");
                }

                // Add hashmap to global container
                emails.add(entry);
            }

            // Close the store and folder objects
            inbox.close(false);
            store.close();

        } catch(NoSuchProviderException e) {
            Log.v(tag, "NoSuchProviderException: " + e.getMessage());
            e.printStackTrace();
        } catch(MessagingException e) {
            Log.v(tag, "MessagingException: " + e.getMessage());
            e.printStackTrace();
        } catch(IOException e) {
            Log.v(tag, "IOException: " + e.getMessage());
            e.printStackTrace();
        }

        // Finally, return messages
        return emails;

    }

}
