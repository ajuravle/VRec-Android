package com.example.diana.vrec;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import static android.R.layout.simple_list_item_1;


/**
 * Created by Diana on 28.12.2016.
 */

public class ListFragment extends Fragment {
    private static ListView lista;
    private static List<String> playList = null;
    private static ArrayAdapter<String> adaptor;
    private SearchView search;
    private static View v;
    private LayoutInflater infla;
    private ViewGroup conta;
    private Dialog dialog;
    private static ImageButton play = null;
    private static ImageButton close = null;
    private static SeekBar mSeekBar2 =null;
    private MediaPlayer mp;
    private static boolean mStartPlay = true;
    private Handler myHandler = new Handler();
    private static TextView begin;


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.list_fragment, container, false);
        this.infla = inflater;
        this.conta = container;

        search = (SearchView) v.findViewById(R.id.searchView);
        int id = search.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView textView = (TextView) search.findViewById(id);
        textView.setHint(getString(R.string.hint));
        textView.setHintTextColor(Color.rgb(113,158,130));
        textView.setTextColor(Color.rgb(60,202,88));

        int id2 = search.getContext().getResources().getIdentifier("android:id/search_button", null, null);
        ImageView icon1 =(ImageView) search.findViewById(id2);
        icon1.setImageResource(R.drawable.ll);
        icon1.getLayoutParams().height = 70;
        icon1.getLayoutParams().width = 60;


        int id3 = search.getContext().getResources().getIdentifier("android:id/search_close_btn", null, null);
        ImageView icon2 =(ImageView) search.findViewById(id3);
        icon2.setImageResource(R.drawable.x16);

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                List<String> searchplayList = new ArrayList<String>();

                for (String name: playList){
                    if (name.toLowerCase().contains(newText.toLowerCase())) {
                        searchplayList.add(name);
                    }
                }

                adaptor = new ArrayAdapter<String>(getContext(), simple_list_item_1, searchplayList){
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view =super.getView(position, convertView, parent);

                        TextView textView=(TextView) view.findViewById(android.R.id.text1);
                       // textView.setBackground(Drawable.createFromPath("@drawable/button_recording"));
                        textView.setTextColor(Color.rgb(60,202,88));

                        return view;
                    }
                };
                lista.setAdapter(adaptor);

                return false;
            }
        });

        lista = (ListView) v.findViewById(R.id.listView);
        lista.setSelector( R.drawable.control_list);
        setRecList();

        adaptor = new ArrayAdapter<String>(getContext(), simple_list_item_1, playList){
        @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view =super.getView(position, convertView, parent);

                TextView textView=(TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.rgb(60,202,88));

                return view;
            }
        };
        lista.setAdapter(adaptor); //APELEAZA CAND SE ADAUGA SAU STERGE O INREGISTRARE, UPDATE-UL

        //creez un dialog pentru MediaPlayer

        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.play_layout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mp.stop();
                myHandler.removeCallbacksAndMessages(null);
            }
        });

        play = (ImageButton) dialog.findViewById(R.id.play);
        play.setImageResource(android.R.drawable.ic_media_pause);
        close = (ImageButton) dialog.findViewById(R.id.close);
        mSeekBar2 = (SeekBar) dialog.findViewById(R.id.seekBar2);
        final TextView titlu = (TextView) dialog.findViewById(R.id.titlu);
        begin = (TextView) dialog.findViewById(R.id.begin);
        final TextView end = (TextView) dialog.findViewById(R.id.end);


        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

                final String item  = (String) parent.getItemAtPosition(position);
                File file = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.getExternalStorageDirectory().getAbsolutePath()), "MyRecordings/"+item);

                play.setImageResource(android.R.drawable.ic_media_pause);
                mp = new MediaPlayer();

                try{
                    mp.setDataSource(file.getAbsolutePath());
                    mp.prepare();
                    mp.start();

                }catch(Exception e){e.printStackTrace();}
                seekBar2Progress();

                titlu.setText(item);
                titlu.setSelected(true);
                begin.setText(String.format("%02d",mp.getDuration()/60000)+":"+String.format("%02d",(mp.getDuration()/1000)%60));
                end.setText(String.format("%02d",mp.getDuration()/60000)+":"+String.format("%02d",(mp.getDuration()/1000)%60));

                play.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mStartPlay) {
                            play.setImageResource(android.R.drawable.ic_media_play);
                            myHandler.removeCallbacksAndMessages(null);
                            mp.pause();
                        }else{
                            play.setImageResource(android.R.drawable.ic_media_pause);
                            mp.start();
                            seekBar2Progress();
                        }
                        mStartPlay = !mStartPlay;
                    }
                });

                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mp.stop();
                        myHandler.removeCallbacksAndMessages(null);
                        dialog.dismiss();

                    }
                });

                mSeekBar2.setMax(mp.getDuration());
                mSeekBar2.setProgress(1);

                mSeekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    int myStep = 1;
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if(mp != null && fromUser){
                            mp.seekTo(progress);
                        }
                    }
                    public void onStartTrackingTouch(SeekBar seekBar) {}
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });

                dialog.show();

            }
        });

        lista.setOnCreateContextMenuListener(new AdapterView.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                menu.setHeaderTitle("Options");
                getActivity().getMenuInflater().inflate(R.menu.contex_menu, menu);
            }
        });

        return v;
    }

    // seekbar2 run
    public void seekBar2Progress() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mp != null){
                    int mCurrentPosition = mp.getCurrentPosition();
                    Log.e("gskjdfsdf", String.valueOf(mCurrentPosition)+"   "+String.valueOf(mp.getDuration()));
                    mSeekBar2.setProgress(mCurrentPosition);

                    if (mCurrentPosition + 60 >= mp.getDuration()){
                        dialog.dismiss();
                        mp.stop();
                        myHandler.removeCallbacksAndMessages(null);
                    }
                }
                myHandler.postDelayed(this, 100);

                begin.setText(String.format("%02d",mp.getCurrentPosition()/60000)+":"+String.format("%02d",(mp.getCurrentPosition()/1000)%60));
            }
        });
    }


    public static void setRecList() {
        String name;
        playList = new ArrayList<>();
        File dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.getExternalStorageDirectory().getAbsolutePath()), "MyRecordings");
        for (File f : dir.listFiles()) {
            if (f.isFile()) {
                name = f.getName();
                playList.add(name);
            }
        }

    }

    public static void notifyAd(){

        adaptor = new ArrayAdapter<String>(v.getContext(), simple_list_item_1, playList){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view =super.getView(position, convertView, parent);

                TextView textView=(TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.rgb(60,202,88));

                return view;
            }
        };
        lista.setAdapter(adaptor);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        final String ite  =  playList.get(info.position);
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.getExternalStorageDirectory().getAbsolutePath()), "MyRecordings/"+ite);


        switch (item.getItemId()) {
            case R.id.i1:
                if (file.exists()){
                    ShowMessageBox2(file);
                    setRecList();
                    notifyAd();
                }
                return true;
            case R.id.i2:
                if (file.exists()){
                    file.delete();
                    setRecList();
                    notifyAd();
                    Toast toast = Toast.makeText(getContext()," Your record was deleted!", Toast.LENGTH_SHORT );
                    toast.show();
                }

                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


    public void ShowMessageBox2(final File fi) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(false);
        builder.setMessage("Give a new name");

        View v = infla.inflate(R.layout.alert_layout, conta, false);
        final EditText text = (EditText) v.findViewById(R.id.et);

        builder.setView(v);

        builder.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String name = text.getText().toString();
                String ext = null;
                if(fi.getAbsolutePath().split("\\.").length >0) {
                    ext = fi.getAbsolutePath().split("\\.")[fi.getAbsolutePath().split("\\.").length - 1];
                }
                File file = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.getExternalStorageDirectory().getAbsolutePath()), "MyRecordings");
                File to = new File(file, name+"."+ext);
                fi.renameTo(to);

                setRecList();
                notifyAd();

                Toast toast = Toast.makeText(getContext()," Your record was renamed!", Toast.LENGTH_SHORT );
                toast.show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alertDialog = builder.create();

        alertDialog.show();

    }


    @Override
    public void onResume() {
        super.onResume();
        ListFragment.setRecList();
        ListFragment.notifyAd();
    }

    @Override
    public void onPause(){
        super.onPause();
        dialog.dismiss();
        if (mp != null){
            mp.stop();
        }
        myHandler.removeCallbacksAndMessages(null);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myHandler.removeCallbacksAndMessages(null);
    }

    public static ListFragment newInstance(String text) {

        ListFragment f = new ListFragment();
        return f;
    }

}
