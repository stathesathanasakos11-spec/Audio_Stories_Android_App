package com.myprojects.unipiaudiostories.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.myprojects.unipiaudiostories.models.User;
import com.myprojects.unipiaudiostories.R;


public class AuthManager {
    private FirebaseAuth mAuth;

    public interface AuthCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public AuthManager() {
        mAuth = FirebaseAuth.getInstance();
    }



    //σύνδεση χρήστη
    public void signIn(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        // ΕΔΩ Η ΔΙΟΡΘΩΣΗ: Πρέπει ο χρήστης να έχει κάνει verify το email του
                        if (user != null && user.isEmailVerified()) {
                            callback.onSuccess();
                        } else {
                            // Αν δεν είναι verified, τον αποσυνδέουμε αμέσως για να μην κρατάει session
                            signOut();
                            callback.onFailure("Email not verified");
                        }
                    } else {
                        callback.onFailure("");
                    }
                });
    }






    // εγγραφή χρήστη
    public void signUp(final String email, String password, final String username, final AuthCallback callback) {
        // δημιουργία νέου χρήστη με τη method email&password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // αποστολή email επιβεβαίωσης λογαριασμού από την Firebase
                            user.sendEmailVerification()
                                    .addOnCompleteListener(verifyTask -> {
                                        if (verifyTask.isSuccessful()) {
                                            //αποθήκευση χρήστη στη βάση
                                            saveUserToDatabase(username, email, callback);
                                        } else {
                                            //Αποτυχία αποστολής email
                                            callback.onFailure("");
                                        }
                                    });
                        }
                    } else {
                        // Αποτυχία δημιουργίας λογαριασμού (π.χ. το email υπάρχει ήδη)
                        callback.onFailure("");
                    }
                });
    }






    //αποθήκευση χρήστη στη realtime database στον κόμβο User
    private void saveUserToDatabase(String username, String email, AuthCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        // πρέπει να υπάρχει χρήστης για να αποθηκευτεί στον κόμβο User
        if (user == null) return;

        String uid = user.getUid();
        User newUser = new User(username, email);

        FirebaseDatabase.getInstance().getReference("User")
                .child(uid)
                .setValue(newUser)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure("");
                    }
                });
    }






    //αποστολή email επαναφοράς κωδικού από την firebase
    public void resetPassword(String email, AuthCallback callback) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Το email στάλθηκε επιτυχώς
                        callback.onSuccess();
                    } else {
                        callback.onFailure("");
                    }
                });
    }




    public void signOut() {
        mAuth.signOut();
    }







    //διαγραφή προφίλ χρήστη
    public void deleteAccount(AuthCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();

            //διαγραφή από την ΒΔ
            FirebaseDatabase.getInstance().getReference("User").child(uid)
                    .removeValue()
                    .addOnCompleteListener(dbTask -> {
                        if (dbTask.isSuccessful()) {
                            //διαγραφή από το Firebase Authentication
                            user.delete().addOnCompleteListener(authTask -> {
                                if (authTask.isSuccessful()) {
                                    callback.onSuccess();
                                } else {
                                    // για περίπτωση αποτυχίας
                                    callback.onFailure("auth_delete_failed");
                                }
                            });
                        } else {
                            callback.onFailure("database_delete_failed");
                        }
                    });
        }
    }









    //η Firebase δίνει στο sandbox της εφαρμογής στη συσκευή ένα token με αντίστοιχο τρόπο
    // όπως τα shared preferences αποθηκεύουν στη συσκευή τις προτιμήσεις του χρήστη.
    // Εφόσον ο χρήστης δεν έχει κάνει logout αυτό το token παραμένει ενεργό και έτσι δεν χρειάζεται
    // ξανά σύνδεση την επόμενη φορά (session)
    // Διαφορετικά το getCurrentUser() επιστρέφει 'False' και πρέπει να γίνει ξανά login
    public boolean isUserLoggedIn() {
        //αν ο χρήστης έχει ήδη συνδεθεί από την συσκευή του τότε η AuthActivity παραλείπεται
        // ΔΙΟΡΘΩΣΗ: Προσθήκη ελέγχου isEmailVerified για να μην μπαίνει χωρίς verify αν ανοίξει ξανά την εφαρμογή
        return mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified();
    }
}