package net.yura.domination.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import java.util.ArrayList;
import java.util.List;

public class GoogleAccount {

    private static final int RC_SIGN_IN = 9000;

    private Activity activity;

    interface SignInListener {
        void onSignInSucceeded();
        void onSignInFailed();
    }

    private List<SignInListener> listeners = new ArrayList();

    public GoogleAccount(Activity activity) {
        this.activity = activity;
    }

    public void addSignInListener(SignInListener listener) {
        listeners.add(listener);
    }

    public boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(activity) != null;
    }

    public void signInSilently() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(activity,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        signInClient.silentSignIn().addOnCompleteListener(activity,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            // The signed in account is stored in the task's result.
                            GoogleSignInAccount signedInAccount = task.getResult();
                            signInSuccessful(signedInAccount);
                        }
                    }
                });
    }

    public void startSignInIntent() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(activity,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        Intent intent = signInClient.getSignInIntent();
        activity.startActivityForResult(intent, RC_SIGN_IN);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // The signed in account is stored in the result.
                GoogleSignInAccount signedInAccount = result.getSignInAccount();
                signInSuccessful(signedInAccount);
            }
            else {
                signInFailed();

                // HACK: for some strange reason, user cancelled actually returns status of ERROR
                if (result.getStatus() != Status.RESULT_CANCELED && result.getStatus().getStatusCode() != CommonStatusCodes.ERROR) {
                    String message = result.getStatus().getStatusMessage();
                    if (message == null || "".equals(message)) {
                        message = "Failed to sign in";
                    }
                    new AlertDialog.Builder(activity).setMessage(message)
                            .setNeutralButton(android.R.string.ok, null).show();
                }
            }
        }
    }

    public void signOut() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(activity,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        signInClient.signOut().addOnCompleteListener(activity,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // at this point, the user is signed out.
                    }
                });
    }

    private void signInSuccessful(GoogleSignInAccount signedInAccount) {
        for (SignInListener listener : listeners) {
            listener.onSignInSucceeded();
        }
    }

    private void signInFailed() {
        for (SignInListener listener : listeners) {
            listener.onSignInFailed();
        }
    }
}
