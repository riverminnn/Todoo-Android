// app/src/main/java/com/example/todooapp/utils/UserManager.java
package com.example.todooapp.utils.shared;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;

public class UserManager {
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";

    private SharedPreferences prefs;
    private FirebaseAuth firebaseAuth;

    public UserManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public boolean isLoggedIn() {
        return prefs.contains(KEY_USER_ID) || firebaseAuth.getCurrentUser() != null;
    }

    public void saveUserSession(String userId, String email) {
        prefs.edit()
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USER_EMAIL, email)
                .apply();
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    public void clearUserSession() {
        // Sign out from Firebase
        firebaseAuth.signOut();

        // Clear shared preferences
        prefs.edit()
                .remove(KEY_USER_ID)
                .remove(KEY_USER_EMAIL)
                .apply();
    }
}