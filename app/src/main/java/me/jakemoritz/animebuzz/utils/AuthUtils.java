package me.jakemoritz.animebuzz.utils;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a utility class to manage the authentication of users' MyAnimeList credentials
 */
public class AuthUtils {

    private static final String TAG = AuthUtils.class.getName();

    // Firebase Authentication constants
    private static final String DUMMY_EMAIL_DOMAIN = "@animebuzz.com";

    // Firebase Firestore constants
    private static final String COLLECTION_USERS = "users";
    private static final String FIELD_BASIC_AUTH_SEQ = "basic_auth_sequence";

    private static AuthUtils instance;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    public static synchronized AuthUtils getInstance(){
        if (instance == null){
            instance = new AuthUtils();
            instance.firebaseAuth = FirebaseAuth.getInstance();
            instance.firebaseFirestore = FirebaseFirestore.getInstance();
        }

        return instance;
    }


    public void createNewUser(String username, String password){
        String dummyEmail = username + DUMMY_EMAIL_DOMAIN;
        
        firebaseAuth.createUserWithEmailAndPassword(dummyEmail, password)
                .addOnSuccessListener(authResult -> {
                    // Successfully added user to Firebase Authentication
                    Task<GetTokenResult> getTokenResultTask = authResult.getUser().getIdToken(false);
                    getTokenResultTask.addOnCompleteListener(task -> {
                        GetTokenResult getTokenResult = task.getResult();
                        String authToken = getTokenResult.getToken();

                        FirebaseUser firebaseUser = authResult.getUser();
                        Map<String, String> userData = new HashMap<>();
                        userData.put("userId", firebaseUser.getUid());
                        userData.put(FIELD_BASIC_AUTH_SEQ, "testseq");
                        firebaseFirestore.collection(COLLECTION_USERS)
                                .add(userData)
                                .addOnCompleteListener(task1 -> {
                                    Log.d(TAG, "x");
                                })
                                .addOnFailureListener(e -> {
                                   Log.d(TAG, "s");
                                });
                    }).addOnFailureListener(e -> {
                        // Failed adding user to Firebase Authentication
                        // TODO: Log error to Crashlytics
                        Log.d(TAG, "failed auth");
                    });
                })
                .addOnFailureListener(e -> {
                    // Failed adding user to Firebase Authentication
                    // TODO: Log error to Crashlytics
                    Log.d(TAG, "failed auth");
                });
    }

    // TODO: Delete a user in Firebase Authentication if they sign out of MAL

}
