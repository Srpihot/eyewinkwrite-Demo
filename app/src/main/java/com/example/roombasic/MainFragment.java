package com.example.roombasic;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.icu.util.IslamicCalendar;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {
    private WordViewModel wordViewModel;
    private RecyclerView recyclerView;
    private MyAdapter myAdapter_card,myAdapter_normal;
    private LiveData<List<Word>> filtereWords;
    private static final String VIEW_TYPE_SHP  = "view_type_shp";
    private static final String IS_USING_CARD_VIEW  = "is_using_card_view";
    private List<Word> allWords;
    private boolean undoAction;
    //private DividerItemDecoration dividerItemDecoration;

    public MainFragment() {
        // Required empty public constructor
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.clearData:
                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                builder.setTitle("是否清空所有单词？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        wordViewModel.deleteAllWords();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.create();
                builder.show();
                break;
            case R.id.switchView:
                SharedPreferences shp =requireActivity().getSharedPreferences(VIEW_TYPE_SHP,Context.MODE_PRIVATE);
                boolean isUsingCardView = shp.getBoolean(IS_USING_CARD_VIEW,false);
                SharedPreferences.Editor editor = shp.edit();
                if(isUsingCardView){
                    recyclerView.setAdapter(myAdapter_normal);
                    editor.putBoolean(IS_USING_CARD_VIEW,false);
                }else{
                    recyclerView.setAdapter(myAdapter_card);
                    editor.putBoolean(IS_USING_CARD_VIEW,true);
                }
                editor.apply();
;        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.word_menu,menu);
        SearchView searchView=(SearchView)menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setMaxWidth(1000);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String sph = newText.trim();
                filtereWords.removeObservers(getViewLifecycleOwner());
                filtereWords= wordViewModel.findWord(sph);
                filtereWords.observe(getViewLifecycleOwner(), new Observer<List<Word>>() {
                    @Override
                    public void onChanged(List<Word> words) {
                        int temp=myAdapter_normal.getItemCount();
                        allWords= words;
                        if(temp!=words.size()){
                            myAdapter_normal.submitList(words);
                            myAdapter_card.submitList(words);
                        }
                    }
                });
                return true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        wordViewModel=ViewModelProviders.of(requireActivity()).get(WordViewModel.class);
        recyclerView=requireActivity().findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        myAdapter_normal= new MyAdapter(false,wordViewModel);
        myAdapter_card=new MyAdapter(true,wordViewModel);
        recyclerView.setItemAnimator(new DefaultItemAnimator(){
            @Override
            public void onAnimationFinished(@NonNull RecyclerView.ViewHolder viewHolder) {
                super.onAnimationFinished(viewHolder);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (linearLayoutManager != null){
                    int firstPosition = linearLayoutManager.findFirstVisibleItemPosition();
                    int lastPosition = linearLayoutManager.findLastVisibleItemPosition();
                    for(int i = firstPosition;i<=lastPosition;i++){
                        MyAdapter.MyViewHolder holder=(MyAdapter.MyViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
                        if(holder!=null){
                            holder.textViewId.setText(String.valueOf(i+1));
                        }
                    }
                }
            }
        });
        SharedPreferences shp= requireActivity().getSharedPreferences(VIEW_TYPE_SHP, Context.MODE_PRIVATE);
        boolean isUsingCardView = shp.getBoolean(IS_USING_CARD_VIEW,false);
        //dividerItemDecoration= new DividerItemDecoration(requireActivity(),DividerItemDecoration.VERTICAL);

        if(isUsingCardView){
            recyclerView.setAdapter(myAdapter_card);
        }else{
            recyclerView.setAdapter(myAdapter_normal);
            //recyclerView.addItemDecoration(dividerItemDecoration);
        }

        filtereWords = wordViewModel.getAllWordsLive();
        filtereWords.observe(getViewLifecycleOwner(), new Observer<List<Word>>() {
            @Override
            public void onChanged(List<Word> words) {
                int temp=myAdapter_normal.getItemCount();
                allWords=words;
                if(temp!= words.size()){
                    if(temp < words.size() && !undoAction){
                        recyclerView.smoothScrollBy(0,-200);
                    }
                    undoAction=false;
                    myAdapter_normal.submitList(words);
                    myAdapter_card.submitList(words);
                }
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP|ItemTouchHelper.DOWN, ItemTouchHelper.START|ItemTouchHelper.END) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                Word wordFrom = allWords.get(viewHolder.getAdapterPosition());
                Word wordTo = allWords.get(target.getAdapterPosition());
                int idTemp=wordFrom.getId();
                wordFrom.setId(wordTo.getId());
                wordTo.setId(idTemp);
                wordViewModel.updateWords(wordFrom,wordTo);
                myAdapter_normal.notifyItemMoved(viewHolder.getAdapterPosition(),target.getAdapterPosition());
                myAdapter_card.notifyItemMoved(viewHolder.getAdapterPosition(),target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final  Word wordToDelete = allWords.get(viewHolder.getAdapterPosition());
                wordViewModel.deleteWords(wordToDelete);
                Snackbar.make(requireActivity().findViewById(R.id.wordsFragmentView),"删除一个词汇",Snackbar.LENGTH_SHORT)
                .setAction("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        undoAction=true;
                        wordViewModel.insertWords(wordToDelete);
                    }
                });
            }
            Drawable icon = ContextCompat.getDrawable(requireActivity(),R.drawable.ic_delete_forever_black_24dp);
            Drawable background = new ColorDrawable(Color.LTGRAY);
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive)
            {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                View itemView = viewHolder.itemView;
                int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;

                int iconLeft,iconRight,iconTop,iconBottom;
                int backTop,backBottom,backLeft,backRight;
                backTop = itemView.getTop();
                backBottom = itemView.getBottom();
                iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) /2;
                iconBottom = iconTop + icon.getIntrinsicHeight();
                if (dX > 0) {
                    backLeft = itemView.getLeft();
                    backRight = itemView.getLeft() + (int)dX;
                    background.setBounds(backLeft,backTop,backRight,backBottom);
                    iconLeft = itemView.getLeft() + iconMargin ;
                    iconRight = iconLeft + icon.getIntrinsicWidth();
                    icon.setBounds(iconLeft,iconTop,iconRight,iconBottom);
                } else if (dX < 0){
                    backRight = itemView.getRight();
                    backLeft = itemView.getRight() + (int)dX;
                    background.setBounds(backLeft,backTop,backRight,backBottom);
                    iconRight = itemView.getRight()  - iconMargin;
                    iconLeft = iconRight - icon.getIntrinsicWidth();
                    icon.setBounds(iconLeft,iconTop,iconRight,iconBottom);
                } else {
                    background.setBounds(0,0,0,0);
                    icon.setBounds(0,0,0,0);
                }
                background.draw(c);
                icon.draw(c);
            }
        }).attachToRecyclerView(recyclerView);


        FloatingActionButton floatingActionButton = requireActivity().findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_mainFragment_to_addFragment);
            }
        });


    }
}
