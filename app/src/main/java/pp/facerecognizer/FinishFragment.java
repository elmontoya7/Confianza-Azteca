package pp.facerecognizer;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class FinishFragment extends Fragment {


    public FinishFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        setTimer();
        return inflater.inflate(R.layout.fragment_finish, container, false);
    }

    private void setTimer () {
        new CountDownTimer(3000, 1000) {

            public void onTick(long millisUntilFinished) {
                Log.d("TIMER", "onTick: " + millisUntilFinished);
            }

            public void onFinish() {
                getActivity().finish();
            }

        }.start();
    }

}
