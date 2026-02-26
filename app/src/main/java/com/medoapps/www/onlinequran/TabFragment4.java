package com.medoapps.www.onlinequran;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TabFragment4 extends Fragment {
    private static final String TAG = "EmailPassword";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_fragment_4, container, false);
    }

   /* public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Button SignOut=(Button)getActivity().findViewById(R.id.sign_out_button1);
        Button VerfyMessage=(Button)getActivity().findViewById(R.id.verify_email_button1);

        SignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RegisterActivity.instance5.mAuth.signOut();
                Taps.instance6.finish();
            }
        });
        VerfyMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Disable button
                getView().findViewById(R.id.verify_email_button1).setEnabled(false);

                // Send verification email
                // [START send_email_verification]
                final FirebaseUser user = RegisterActivity.instance5.mAuth.getCurrentUser();
                user.sendEmailVerification()
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                // [START_EXCLUDE]
                                // Re-enable button
                                getView().findViewById(R.id.verify_email_button).setEnabled(true);

                                if (task.isSuccessful()) {
                                    Toast.makeText(getActivity(),
                                            "Verification email sent to " + user.getEmail(),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e(TAG, "sendEmailVerification", task.getException());
                                    Toast.makeText(getActivity(),
                                            "Failed to send verification email.",
                                            Toast.LENGTH_SHORT).show();
                                }
                                // [END_EXCLUDE]
                            }
                        });
                // [END send_email_verification]
            }
        });




    }*/




}