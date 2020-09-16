package com.pomtech.noteappmvvm;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputLayout;
import com.r0adkll.slidr.Slidr;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class AddEditNoteActivity extends AppCompatActivity {
    public static final String EXTRA_TITLE =
            "com.pomtech.noteappmvvm.EXTRA_TITLE";
    public static final String EXTRA_DESCRIPTION =
            "com.pomtech.noteappmvvm.EXTRA_DESCRIPTION";
    public static final String EXTRA_DATE =
            "com.pomtech.noteappmvvm.EXTRA_DATE";
    public static final String EXTRA_ID =
            "com.pomtech.noteappmvvm.EXTRA_ID";

    private TextInputLayout editTextTitle, editTextDescription;
    private MenuItem saveNoteMenuItem;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_note_menu, menu);
        saveNoteMenuItem = menu.findItem(R.id.action_save_note);
        saveNoteMenuItem.setVisible(false);
        Objects.requireNonNull(editTextTitle.getEditText()).addTextChangedListener(textWatcher);
        Objects.requireNonNull(editTextDescription.getEditText()).addTextChangedListener(textWatcher);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_save_note) {
            saveNote();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String languageToLoad = "fa"; // your language
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_add_note);


        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean firstStart = prefs.getBoolean("firstStart", true);
        if (firstStart) {
            showInformationToast();
        }

        Slidr.attach(this);

        Toolbar addEdit_note_toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(addEdit_note_toolbar);

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDescription = findViewById(R.id.edit_text_description);

        editTextDescription.setHintEnabled(false);


        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ID)) {
            getSupportActionBar().setTitle(R.string.edit_note);
            Objects.requireNonNull(editTextTitle.getEditText()).setText(intent.getStringExtra(EXTRA_TITLE));
            Objects.requireNonNull(editTextDescription.getEditText()).setText(intent.getStringExtra(EXTRA_DESCRIPTION));
        } else {
            getSupportActionBar().setTitle(R.string.new_note);
        }
    }

    private void showInformationToast() {
        Toasty.info(this, "برای رفتن به صفحه اصلی, صفحه را از سمت چپ به سمت راست بکشید", Toast.LENGTH_LONG, true).show();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firstStart", false);
        editor.apply();
    }

    private void saveNote() {
        String title = Objects.requireNonNull(editTextTitle.getEditText()).getText().toString();
        String description = Objects.requireNonNull(editTextDescription.getEditText()).getText().toString();
//        int priority = numberPickerPriority.getValue();


        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MM dd, yyyy");
        String saveCurrentDate = currentDate.format(calendar.getTime());

        Log.i("date", "saveNote: " + saveCurrentDate);
        char[] month = new char[2];
        saveCurrentDate.getChars(0, 2, month, 0);
        int miladiMonth = Integer.parseInt(String.valueOf(month));
        Log.i("month", "saveNote: " + miladiMonth);

        char[] day = new char[2];
        saveCurrentDate.getChars(3, 5, day, 0);
        int miladiDay = Integer.parseInt(String.valueOf(day));
        Log.i("day", "saveNote: " + miladiDay);

        char[] year = new char[4];
        saveCurrentDate.getChars(7, 11, year, 0);
        int miladiYear = Integer.parseInt(String.valueOf(year));
        Log.i("year", "saveNote: " + miladiYear);


        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        String saveCurrentTime = currentTime.format(calendar.getTime());


        DateConverter converter = new DateConverter();
        converter.gregorianToPersian(miladiYear, miladiMonth, miladiDay);

        String shamsiYear = String.valueOf(converter.getYear());
        String shamsimonth = String.valueOf(converter.getMonth());
        String shamsiDay = String.valueOf(converter.getDay());


        String shamsiCurrentDateTime = saveCurrentTime + "  -  " + shamsiDay + " ‌‌‌‌٬ " + shamsimonth + " ‌‌‌‌٬ " + shamsiYear;
        String finalShamsiCurrentDateTime = FormatHelper.toPersianNumber(shamsiCurrentDateTime);

        Log.i("dateTime", "saveNote: " + shamsiCurrentDateTime);

        Intent data = new Intent();
        data.putExtra(EXTRA_TITLE, title);
        data.putExtra(EXTRA_DESCRIPTION, description);
        data.putExtra(EXTRA_DATE, finalShamsiCurrentDateTime);

        int id = getIntent().getIntExtra(EXTRA_ID, -1);
        if (id != -1) {
            data.putExtra(EXTRA_ID, id);
        }

        setResult(RESULT_OK, data);
        finish();
    }


    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            saveNoteMenuItem.setVisible(false);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (Objects.requireNonNull(editTextDescription.getEditText()).getText().toString().trim().length() == 0 &&
                    Objects.requireNonNull(editTextTitle.getEditText()).getText().toString().trim().length() == 0) {
                saveNoteMenuItem.setVisible(false);
            } else if (0 == editTextDescription.getEditText().getText().toString().trim().length()) {
                saveNoteMenuItem.setVisible(false);
            } else if (Objects.requireNonNull(editTextTitle.getEditText()).getText().toString().trim().length() == 0) {
                saveNoteMenuItem.setVisible(false);
            } else {
                saveNoteMenuItem.setVisible(true);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
}
