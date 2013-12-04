package com.git.programmerr47.testhflbjcrhjggkth.view.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import com.git.programmerr47.testhflbjcrhjggkth.R;
import com.git.programmerr47.testhflbjcrhjggkth.model.MicroScrobblerModel;
import com.git.programmerr47.testhflbjcrhjggkth.model.RecognizeServiceConnection;
import com.git.programmerr47.testhflbjcrhjggkth.model.database.DatabaseSongData;
import com.git.programmerr47.testhflbjcrhjggkth.model.pleer.api.Api;
import com.git.programmerr47.testhflbjcrhjggkth.model.pleer.api.Audio;
import com.git.programmerr47.testhflbjcrhjggkth.model.pleer.api.KException;
import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class PleerListAdapter extends BaseAdapter{

    private MicroScrobblerModel model;
    private DatabaseSongData currentSongData;
    private List<Audio> urls;
    private LayoutInflater inflater;
    private Activity activity;
    private int resLayout;

    public PleerListAdapter(final Activity activity, int resLayout) {
        this.activity = activity;
        this.resLayout = resLayout;
        model = RecognizeServiceConnection.getModel();
        currentSongData = model.getCurrentOpenSong();
        urls = new ArrayList<Audio>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    urls = Api.searchAudio(currentSongData.getArtist() + " " + currentSongData.getTitle(), 0, 0);
                    Log.v("PleerListAdapter", "ANSWER FROM INTERNET");
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            PleerListAdapter.this.notifyDataSetChanged();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (JSONException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (KException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }).start();
        inflater = (LayoutInflater) this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return urls.size();
    }

    @Override
    public Object getItem(int position) {
        return urls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(resLayout, parent, false);
        }

        TextView textView;

        textView = (TextView) view.findViewById(R.id.ppUrlListItemArtist);
        textView.setText(urls.get(position).artist);

        textView = (TextView) view.findViewById(R.id.ppUrlListItemTitle);
        textView.setText(urls.get(position).title);

        textView = (TextView) view.findViewById(R.id.ppUrlListItemDuration);
        textView.setText(urls.get(position).duration + "");

        textView = (TextView) view.findViewById(R.id.ppUrlListItemBitRate);
        textView.setText(urls.get(position).bitrate);

        LinearLayout info = (LinearLayout) view.findViewById(R.id.ppUrlListItemInfo);
        LinearLayout numbers = (LinearLayout) view.findViewById(R.id.ppUrlListItemNumbers);
        RadioButton radioButton = (RadioButton) view.findViewById(R.id.ppUrlListItemCheckbutton);

        if ((currentSongData.getPleercomUrl() != null) && (currentSongData.getPleercomUrl().equals(urls.get(position).url))) {
            Log.v("PleerListAdapter", "selected");
            //info.setBackgroundResource(R.drawable.list_item_bg_pressed);
            //numbers.setBackgroundResource(R.drawable.list_item_bg_pressed);
            radioButton.setChecked(true);
        } else {
            Log.v("PleerListAdapter", "don't selected");
            //info.setBackgroundResource(R.drawable.list_item_bg_default);
            //numbers.setBackgroundResource(R.drawable.list_item_bg_default);
            radioButton.setChecked(false);
        }

        return view;
    }
}
