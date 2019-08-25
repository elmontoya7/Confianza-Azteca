package pp.facerecognizer;


import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class AuthenticationFragment extends Fragment {

    private Button button;
    private TextView lastAccessText;
    private static String PREFS_NAME = "record";
    private int SELFIE_REQUEST_CODE = 100;
    private ImageView successSelfie, successRecord, recordBtn;
    private boolean selfie = false;
    private boolean voice = false;
    private boolean startRecording = true;
    private MediaRecorder recorder;
    private String fileName;
    private LinearLayout selfieLayout, recordLayout;

    private int validation;
    private String id;

    public enum State {
        SELFIE,
        VOICE,
        FINISH
    }

    private State currentState = State.SELFIE;

    public AuthenticationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        Calendar calendar = Calendar.getInstance();
        editor.putString("lastAccess", calendar.get(Calendar.DATE) + " de " + getMonth(calendar.get(Calendar.MONTH)) + " del " + calendar.get(Calendar.YEAR));

// Apply the edits!
        editor.apply();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_authentication, container, false);

        validation = getArguments().getInt("validation");
        id = getArguments().getString("id");

        selfieLayout = view.findViewById(R.id.selfie_layout);
        recordLayout = view.findViewById(R.id.record_layout);

        if (validation == 2) {
            recordLayout.setVisibility(View.VISIBLE);
        }

        TextView lastAccessText = view.findViewById(R.id.last_access_text);

        fileName = getActivity().getExternalCacheDir().getAbsolutePath();
        fileName += "/confianza_azteca/audio.3gp";

        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        String lastAccess = settings.getString("lastAccess", " día de hoy");

        lastAccessText.setText("Tu último acceso fue el " + lastAccess);

        button = view.findViewById(R.id.main_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performActionButton();
            }
        });

        successRecord = view.findViewById(R.id.success_record);
        successSelfie = view.findViewById(R.id.success_selfie);
        recordBtn = view.findViewById(R.id.record_btn);
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRecord();
            }
        });

        updateButtonState();

        return view;
    }

    private void updateButtonState () {
        switch (currentState) {
            case SELFIE:
                button.setText("Iniciar autenticación");
                break;
            case VOICE:
                button.setText("Iniciar grabación de voz");
                button.setVisibility(View.INVISIBLE);
                break;
            case FINISH:
                button.setText("Terminar");
                break;
        }
    }

    private void performActionButton () {
        Intent intent;

        switch (currentState) {
            case SELFIE:
                intent = new Intent(getActivity(), MainActivity.class);
                intent.putExtra("close", true);
                startActivityForResult(intent, SELFIE_REQUEST_CODE);
                if (validation == 2) {
                    currentState = State.VOICE;
                } else {
                    currentState = State.FINISH;
                }
                break;
            case VOICE:
                break;
            case FINISH:
                ((AuthenticationActivity)getActivity()).onStatusChangeListener(1);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELFIE_REQUEST_CODE) {
            Boolean recognized = data.getBooleanExtra("recognized", false);
            selfie = recognized;
            successSelfie.setVisibility(View.VISIBLE);
        }

        updateButtonState();
    }

    public String getMonth(int month) {
        return new DateFormatSymbols().getMonths()[month];
    }

    private void onRecord() {
        if (startRecording) {
            startRecording();
        } else {
            stopRecording();
        }

        startRecording = !startRecording;
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Toast.makeText(getActivity(), "Ocurrió un error iniciando la grabación.", Toast.LENGTH_SHORT).show();
        }

        recorder.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }
}
