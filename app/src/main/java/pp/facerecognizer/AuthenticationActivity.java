package pp.facerecognizer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class AuthenticationActivity extends AppCompatActivity {

    private int validation;
    private String id;
    private MediaRecorder recorder;
    private String fileName;
    private boolean record = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/audio.3gp";

        Log.d("file_name", fileName);

        Uri data = this.getIntent().getData();
        if (data != null && data.isHierarchical()) {
            String uri = this.getIntent().getDataString();
            Uri url = Uri.parse(uri);
            id = url.getQueryParameter("id");
            validation = Integer.valueOf(url.getQueryParameter("validation"));
        }

        loadFragment(new AuthenticationFragment());
    }

    private void loadFragment(Fragment fragment) {
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        bundle.putInt("validation", validation);

        Log.d("before_send_args", id + " " + validation);

        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragment.setArguments(bundle);
        transaction.replace(R.id.container, fragment, "frago");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onStatusChangeListener (int status) {
        switch (status) {
            case 1:
                loadFragment(new FinishFragment());
                break;
        }
    }

    public void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    public void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AuthenticationActivity.this);
        builder.setTitle("Permisos necesarios");
        builder.setMessage("Algunos permisos son necesarios para el correcto funcionamiento. Para arreglar esto ve a la configuración del teléfono.");
        builder.setPositiveButton("Ir a configuración", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    public void onRecord() {
        if (record) {
            checkRequiredPermissions();
        } else {
            stopRecording();
        }

        record = !record;
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;

        Toast.makeText(this, "Grabación terminada", Toast.LENGTH_SHORT).show();

        FragmentManager fm = getSupportFragmentManager();
        AuthenticationFragment fragment = (AuthenticationFragment) fm.findFragmentByTag("frago");

        fragment.currentState = AuthenticationFragment.State.FINISH;
        fragment.updateButtonState();
    }

    private void startRecording () {
        recorder = new MediaRecorder();
        recorder.reset();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        Toast.makeText(this, "Grabando...", Toast.LENGTH_SHORT).show();

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e("RecordError", e.getMessage());
            Toast.makeText(this, "Ocurrió un error iniciando la grabación.", Toast.LENGTH_SHORT).show();
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

    public void checkRequiredPermissions () {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            // do you work now
                            Toast.makeText(getApplicationContext(), "Permisos aceptados", Toast.LENGTH_SHORT).show();
                            startRecording();
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // permission is denied permenantly, navigate user to app settings
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                        Toast.makeText(getApplicationContext(), "Debes aceptar los permisos para un correcto funcionamiento", Toast.LENGTH_SHORT).show();
                    }
                })
                .withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Ocurrió un error. Intenta de nuevo.", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }
}
