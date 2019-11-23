package com.example.roombasic;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Word {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "english_word")
    private String word;

    @ColumnInfo(name = "chinese_word")
    private String chineseMeaning;

    @ColumnInfo(name = "chinese_invisible")
    private boolean chineseInvisible;

    boolean isChineseInvisible() {
        return chineseInvisible;
    }

    void setChineseInvisible(boolean chineseInvisible) {
        this.chineseInvisible = chineseInvisible;
    }

    Word(String word, String chineseMeaning) {
        this.word = word;
        this.chineseMeaning = chineseMeaning;
    }

    public int getId() {
        return id;
    }

    String getChineseMeaning() {
        return chineseMeaning;
    }

    String getWord() {
        return word;
    }

    public void setChineseMeaning(String chineseMeaning) {
        this.chineseMeaning = chineseMeaning;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public void setId(int id) {
        this.id = id;
    }
}
