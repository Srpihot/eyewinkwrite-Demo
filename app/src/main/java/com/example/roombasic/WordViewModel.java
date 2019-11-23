package com.example.roombasic;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class WordViewModel extends AndroidViewModel {
    private WordRepository wordRepository;
    public WordViewModel(@NonNull Application application) {
        super(application);
        wordRepository = new WordRepository(application);
    }

    LiveData<List<Word>> getAllWordsLive() { return wordRepository.getAllWordsLive(); }
    LiveData<List<Word>> findWord(String sph){
        return wordRepository.findWord(sph);
    }

    void insertWords(Word... words){wordRepository.insertWords(words);}
    void updateWords(Word... words) {
        wordRepository.updateWords(words);
    }
     void deleteWords(Word... words) {
        wordRepository.deleteWords(words);
    }
    void deleteAllWords() {
        wordRepository.deleteAllWords();
    }
}
