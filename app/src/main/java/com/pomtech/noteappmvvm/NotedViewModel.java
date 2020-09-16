package com.pomtech.noteappmvvm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class NotedViewModel extends AndroidViewModel {

    private NoteRepository mRepository;
    private LiveData<List<Note>> mAllNotes;

    public NotedViewModel(@NonNull Application application) {
        super(application);

        mRepository = new NoteRepository(application);
        mAllNotes = mRepository.getAllNotes();


    }

    void insert(Note note){
        mRepository.insert(note);
    }

    void update(Note note){
        mRepository.update(note);
    }

    void delete(Note note){
        mRepository.delete(note);
    }

    void deleteAllNotes(){
        mRepository.deleteAllNotes();
    }

    LiveData<List<Note>> getAllNotes() {
        return mAllNotes;
    }
}
