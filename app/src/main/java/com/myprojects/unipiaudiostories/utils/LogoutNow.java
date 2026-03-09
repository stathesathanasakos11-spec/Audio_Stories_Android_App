package com.myprojects.unipiaudiostories.utils;

import android.content.Context;
import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;
import com.myprojects.unipiaudiostories.activities.AuthActivity;

public class LogoutNow {
    public static void logout(Context context) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(context, AuthActivity.class);

        // το σημαντικό είναι πλέον ο χρήστης να μην μπορεί να επιστρέψει στα άλλα Activities
        //και να πρέπει να κάνει υποχρεωτικά login ξανά
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }
}
