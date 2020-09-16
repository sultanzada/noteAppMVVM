package com.pomtech.noteappmvvm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends AppCompatActivity {

    public static final int ADD_NOTE_REQUEST = 1;
    public static final int EDIT_NOTE_REQUEST = 2;

    private NotedViewModel mNotedViewModel;
    private RecyclerView recyclerView;
    private CoordinatorLayout coordinatorLayout;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_all_notes:
                mNotedViewModel.deleteAllNotes();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem register = menu.findItem(R.id.action_delete_all_notes);
        mNotedViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(NotedViewModel.class);

        mNotedViewModel.getAllNotes().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(List<Note> notes) {

                if (notes.size() > 1) {
                    register.setVisible(true);
                } else {
                    register.setVisible(false);
                }

            }
        });
        return true;
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
        setContentView(R.layout.activity_main);


        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean firstStart = prefs.getBoolean("firstStart", true);
        if (firstStart) {
            showInformationToast();
        }

        coordinatorLayout = findViewById(R.id.main);
        Toolbar main_toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(main_toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.list_of_notes);

        final LinearLayout no_content_layout = findViewById(R.id.no_content_layout);

        final Typeface iranian_sans = ResourcesCompat.getFont(this, R.font.iranian_sans_bold);


        recyclerView = findViewById(R.id.recycler_view_note_items);
        final FloatingActionButton addNoteFab = findViewById(R.id.button_add_note);

        addNoteFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddEditNoteActivity.class);
                startActivityForResult(intent, ADD_NOTE_REQUEST);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        final NoteAdapter noteAdapter = new NoteAdapter();
        recyclerView.setAdapter(noteAdapter);


        mNotedViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(NotedViewModel.class);

        mNotedViewModel.getAllNotes().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(List<Note> notes) {

                noteAdapter.submitList(notes);
                recyclerView.smoothScrollToPosition(0);

                if (notes.isEmpty()) {
                    no_content_layout.setVisibility(View.VISIBLE);

                } else {
                    no_content_layout.setVisibility(View.GONE);
                }


            }
        });


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {

                final int position = viewHolder.getAdapterPosition();
                final Note myNote = noteAdapter.getNoteAt(position);

                mNotedViewModel.delete(myNote);

                // showing snack bar with Undo option
                Snackbar snackbar = Snackbar
                        .make(recyclerView, R.string.deleted_note, Snackbar.LENGTH_LONG);
                snackbar.setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mNotedViewModel.insert(myNote);

                    }
                });
                snackbar.setActionTextColor(Color.GREEN);
                snackbar.show();

            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {


                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorRed))
                        .addActionIcon(R.drawable.ic_delete)
                        .addSwipeLeftLabel("حذف")
                        .setSwipeLeftLabelColor(Color.WHITE)
                        .setSwipeLeftLabelTextSize(1, 15)
                        .setSwipeLeftLabelTypeface(iranian_sans)
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(recyclerView);


        noteAdapter.setOnITemClickListener(new NoteAdapter.onItemClickListener() {
            @Override
            public void onItemClick(Note note) {
                Intent intent = new Intent(MainActivity.this, AddEditNoteActivity.class);
                intent.putExtra(AddEditNoteActivity.EXTRA_ID, note.getId());
                intent.putExtra(AddEditNoteActivity.EXTRA_TITLE, note.getTitle());
                intent.putExtra(AddEditNoteActivity.EXTRA_DESCRIPTION, note.getDescription());
                intent.putExtra(AddEditNoteActivity.EXTRA_DATE, note.getDateTime());
                startActivityForResult(intent, EDIT_NOTE_REQUEST);
            }
        });

    }

    private void showInformationToast() {
        Toasty.info(this, "برای پاک کردن هر یادداشت, آن را از راست به چپ بکشید", Toast.LENGTH_LONG, true).show();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firstStart", false);
        editor.apply();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String title;
        String description;
        String dateTime;
        int id;
        if (requestCode == ADD_NOTE_REQUEST && resultCode == RESULT_OK) {

            assert data != null;
            title = data.getStringExtra(AddEditNoteActivity.EXTRA_TITLE);
            description = data.getStringExtra(AddEditNoteActivity.EXTRA_DESCRIPTION);
            dateTime = data.getStringExtra(AddEditNoteActivity.EXTRA_DATE);

            Note note = new Note(title, description, dateTime);
            mNotedViewModel.insert(note);
        } else if (requestCode == EDIT_NOTE_REQUEST && resultCode == RESULT_OK) {

            id = Objects.requireNonNull(data).getIntExtra(AddEditNoteActivity.EXTRA_ID, -1);

            if (id == -1) {
                Toast.makeText(this, "Note can not be Updated", Toast.LENGTH_SHORT).show();
                return;
            }

            title = data.getStringExtra(AddEditNoteActivity.EXTRA_TITLE);
            description = data.getStringExtra(AddEditNoteActivity.EXTRA_DESCRIPTION);
            dateTime = data.getStringExtra(AddEditNoteActivity.EXTRA_DATE);

            Note note = new Note(title, description, dateTime);
            note.setId(id);
            mNotedViewModel.update(note);
//            Toast.makeText(this, "Note Updated", Toast.LENGTH_SHORT).show();

        }
    }

}
