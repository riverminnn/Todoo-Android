// filepath: d:\University\Year4\HK2\AndroidNC\Project\Todoo\app\src\main\java\com\example\todooapp\data\model\Todo.java
package com.example.todooapp.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "todos")
public class Todo {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String title;
    private String content;
    private String category;
    private long timestamp;

    private long creationDate;

    // For media attachments
    private String imageUri;        // Local path to stored image
    private String audioRecordingUri;  // Local path to audio file
    private double locationLat;     // Latitude coordinate
    private double locationLng;     // Longitude coordinate
    private String locationAddress; // Optional human-readable address

    private String firebaseKey;

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }

    // Add to Todo.java class
    private long reminderTime = 0;
    private boolean hasReminder = false;

    public long getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(long reminderTime) {
        this.reminderTime = reminderTime;
    }

    public boolean hasReminder() {
        return hasReminder;
    }

    public void setHasReminder(boolean hasReminder) {
        this.hasReminder = hasReminder;
    }

    // Add to Todo.java class
    private boolean isPinned = false;

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    // Add to Todo.java class
    private boolean hidden = false;

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    // Add to Todo.java class
    private boolean inTrash = false;
    private long trashDate = 0;

    public boolean isInTrash() {
        return inTrash;
    }

    public void setInTrash(boolean inTrash) {
        this.inTrash = inTrash;
    }

    public long getTrashDate() {
        return trashDate;
    }

    public void setTrashDate(long trashDate) {
        this.trashDate = trashDate;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    private boolean isCompleted;

    // Constructors, getters, and setters


    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getAudioRecordingUri() {
        return audioRecordingUri;
    }

    public void setAudioRecordingUri(String audioRecordingUri) {
        this.audioRecordingUri = audioRecordingUri;
    }

    public double getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(double locationLat) {
        this.locationLat = locationLat;
    }

    public double getLocationLng() {
        return locationLng;
    }

    public void setLocationLng(double locationLng) {
        this.locationLng = locationLng;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }
}