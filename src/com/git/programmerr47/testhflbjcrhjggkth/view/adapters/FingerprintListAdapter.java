package com.git.programmerr47.testhflbjcrhjggkth.view.adapters;

import android.graphics.Color;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.git.programmerr47.testhflbjcrhjggkth.R;
import com.git.programmerr47.testhflbjcrhjggkth.model.FingerprintData;
import com.git.programmerr47.testhflbjcrhjggkth.model.MicroScrobblerModel;
import com.git.programmerr47.testhflbjcrhjggkth.model.RecognizeServiceConnection;
import com.git.programmerr47.testhflbjcrhjggkth.model.database.DatabaseFingerprintData;
import com.git.programmerr47.testhflbjcrhjggkth.model.database.FingerprintsDeque;
import com.git.programmerr47.testhflbjcrhjggkth.utils.ImageUtils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.nineoldandroids.view.ViewHelper;

public class FingerprintListAdapter extends BaseAdapter {
	private static final String TAG = "FingerprintListAdapter";
	private int idItem;
	private Activity activity;
	private LayoutInflater inflater;
	private MicroScrobblerModel model;
    private boolean isScrolling = false;
    private int lastPosition;
	
	public FingerprintListAdapter(Activity activity, int idItem) {
		this.activity = activity;
		this.idItem = idItem;
		model = RecognizeServiceConnection.getModel();
		inflater = (LayoutInflater) this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		Log.v(TAG, "List count = " + model.getFingerprintList().size());
		return model.getFingerprintList().size();
	}

	@Override
	public Object getItem(int position) {
		return model.getFingerprintList().get(position);
	}
	
	private DatabaseFingerprintData getFingerprintData(int position) {
		return (DatabaseFingerprintData) getItem(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

    @Override
    public void notifyDataSetChanged() {
        isScrolling = false;
        super.notifyDataSetChanged();
    }

    public void scrolling() {
        isScrolling = true;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = inflater.inflate(idItem, parent, false);
		}
		DatabaseFingerprintData data = getFingerprintData(position);
		String fingerXML = data.getFingerprint();
		String finger = fingerXML.substring(fingerXML.indexOf("<FP_BLOCK"), fingerXML.indexOf("</FP_BLOCK>") + 11);
		
		TextView fingerprintText = (TextView) view.findViewById(R.id.fingerprintText);
		if (data.getRecognizeStatus() != null) {
			fingerprintText.setText(data.getRecognizeStatus());
		} else {
			fingerprintText.setText(finger.hashCode() + "");
		}
		TextView fingerprintDate = (TextView) view.findViewById(R.id.fingerprintDate);
		fingerprintDate.setText(data.getDate().toString());
		
		Bitmap coverArtBMP = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888);
		int[] colors = ImageUtils.getPixel8bitColors(finger);
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				coverArtBMP.setPixel(i, j, Color.rgb(colors[i * 8 + j], colors[i * 8 + j], colors[i * 8 + j]));
			}
		}

		ImageView fingerCoverArt = (ImageView) view.findViewById(R.id.fingerprintImage);
        fingerCoverArt.setImageBitmap(coverArtBMP);

        if (data.isInQueueForRecognizing()) {
            ViewHelper.setAlpha(view, 0.5f);
            ViewHelper.setScaleX(view, 0.85f);
            ViewHelper.setScaleY(view, 0.85f);
            ViewHelper.setPivotX(view, view.getWidth() * 0.5f);
            ViewHelper.setPivotY(view, view.getHeight() * 0.5f);

        } else {
            ViewHelper.setAlpha(view, 1.0f);
            ViewHelper.setScaleX(view, 1.0f);
            ViewHelper.setScaleY(view, 1.0f);
            ViewHelper.setPivotX(view, 0);
            ViewHelper.setPivotY(view, 0);

        }

        if (isScrolling) {
            if (position > lastPosition) {
                view.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.list_view_up_down));
            } else {
                view.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.list_view_down_up));
            }
        }
        
        /*if (data.isDeleting()) {
        	deletionFromList(data, view);
        }*/
        
        lastPosition = position;
		
		return view;
	}

    public void recognizeFingerprint(final View view, final int position) {
        final FingerprintData data = (FingerprintData) model.getFingerprintList().get(position);
        if (!data.isInQueueForRecognizing()) {
            final Animation addToDequeue = AnimationUtils.loadAnimation(activity, R.anim.add_to_recognize_queue);
            
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
    			
    			@Override
    			public void run() {
                    //try to recognize
                    ViewHelper.setAlpha(view, 0.5f);
                    ViewHelper.setScaleX(view, 0.85f);
                    ViewHelper.setScaleY(view, 0.85f);
                    ViewHelper.setPivotX(view, view.getWidth() * 0.5f);
                    ViewHelper.setPivotY(view, view.getHeight() * 0.5f);
                    
    	            model.getRecognizeListManager().addFingerprintToQueue(data);
                    data.setInQueueForRecognizing(true);
    			}
    		}, addToDequeue.getDuration() + addToDequeue.getStartOffset());

            view.startAnimation(addToDequeue);
        } else {
            final Animation removeFromDequeue = AnimationUtils.loadAnimation(activity, R.anim.remove_from_recognize_queue);
            view.startAnimation(removeFromDequeue);
            ViewHelper.setAlpha(view, 1.0f);
            ViewHelper.setScaleX(view, 1.0f);
            ViewHelper.setScaleY(view, 1.0f);
            ViewHelper.setPivotX(view, 0);
            ViewHelper.setPivotY(view, 0);
            model.getRecognizeListManager().removeFingerprintFromQueue(data);
            data.setRecognizeStatus(null);
            data.setInQueueForRecognizing(false);
            notifyDataSetChanged();
        }
    }
    
    public void deletionFromList(final FingerprintData data, final View view) {
    	Log.v("Fingers", "Deletion from fingerList " + data.getFingerprint().substring(data.getFingerprint().indexOf("<FP_BLOCK"), data.getFingerprint().indexOf("</FP_BLOCK>") + 11).hashCode());
        final Animation deletionAnimation = AnimationUtils.loadAnimation(activity, R.anim.complete_recognize);
        Log.v("Animation", "Deletion animation duration is " + deletionAnimation.getDuration());
        Log.v("Animation", "Deletion animation offset is " + deletionAnimation.getStartOffset());

        view.startAnimation(deletionAnimation);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
                int beforeSize = model.getFingerprintList().size();
				model.getFingerprintList().remove(data);
                Log.v(TAG, "Listsize(adapter) after deletion is " + model.getFingerprintList().size());
                int afterSize = model.getFingerprintList().size();
                if (beforeSize > afterSize) {
                    /*deque.pollFirst();
                    Log.v("Fingers", "(adapter) after deletion deque.size() = " + deque.size());*/
                }
                //notifyDataSetChanged();
			}
		}, deletionAnimation.getDuration() + deletionAnimation.getStartOffset());
    }
}
