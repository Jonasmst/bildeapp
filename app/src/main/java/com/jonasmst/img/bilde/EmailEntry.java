package com.jonasmst.img.bilde;

import java.io.File;

/**
 * Created by jonas on 25.12.15.
 */
public class EmailEntry {

    private String subject;
    private String from;
    private String text;
    private File image_attachment;

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSubject() {
        return subject;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getFrom() {
        return from;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setImageAttachment(File image) {
        this.image_attachment = image;
    }

    public File getImageAttachment() {
        return image_attachment;
    }
}
