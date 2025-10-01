package baileydavis.cse162_24f.lab7;

import java.util.ArrayList;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.CalendarView;
import android.widget.Toast;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    CalendarView calendarView;
    private static final int REQUEST_PHONE_CALL = 1;
    protected static final int RESULT_SPEECH = 1;

    private ImageButton btnSpeak;
    private TextView txtText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtText = findViewById(R.id.txtText);
        btnSpeak = findViewById(R.id.btnSpeak);
        calendarView = findViewById(R.id.calendarView);

        btnSpeak.setOnClickListener(v -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-us");
            try {
                startActivityForResult(intent, RESULT_SPEECH);
                txtText.setText("");
            }
            catch (ActivityNotFoundException e) {
                Toast.makeText(getApplicationContext(), "Oops! Your device doesn't support Speech to Text", Toast.LENGTH_SHORT).show();
            }
        });

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
        }

        if (calendarView != null) {
            calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                String msg = "Selected date is " + dayOfMonth + "/" + (month + 1) + "/" + year;
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_SPEECH && resultCode == RESULT_OK && data != null) {
            ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (text != null && !text.isEmpty()) {
                String spokenText = text.get(0).toLowerCase();
                txtText.setText(spokenText);

                long unixTime = System.currentTimeMillis();
                switch (spokenText) {
                    case "today":
                        calendarView.setDate(unixTime);
                        break;
                    case "tomorrow":
                        calendarView.setDate(unixTime + 86400000);
                        break;
                    case "day after tomorrow":
                        calendarView.setDate(unixTime + 172800000);
                        break;
                    case "call emergency":
                        StringBuilder sb = new StringBuilder();
                        for (int i = 1; i < text.size(); i++) {
                            sb.append(text.get(i));
                        }

                        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + R.string.emergency_number));
                        startActivity(callIntent);

                        Toast.makeText(MainActivity.this, sb.toString(), Toast.LENGTH_SHORT).show();
                        break;
                    case "take note":
                        if (text.size() > 1) {
                            StringBuilder sbNote = new StringBuilder();
                            for (int i = 1; i < text.size(); i++) {
                                sbNote.append(text.get(i)).append(" ");
                            }
                            Toast.makeText(MainActivity.this, sbNote.toString(), Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PHONE_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                Toast.makeText(this, "Call Phone permission is required to make emergency calls", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
