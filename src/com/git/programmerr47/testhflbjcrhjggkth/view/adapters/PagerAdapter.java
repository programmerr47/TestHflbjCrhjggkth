package com.git.programmerr47.testhflbjcrhjggkth.view.adapters;

import java.util.ArrayList;
import java.util.List;

import com.git.programmerr47.testhflbjcrhjggkth.view.fragments.FragmentWithName;

import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;

public class PagerAdapter extends FragmentPagerAdapter{

	protected int pageCount = 3;
    protected List<FragmentWithName> fragments;
    private int currentPosition;
    private boolean isDraggingPage;

    public PagerAdapter(FragmentManager fm, int pages) {
        super(fm);
        fragments = new ArrayList<FragmentWithName>();
        pageCount = pages;
    }

    protected void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }
    
    @Override
    public Fragment getItem(int position) {
    	return fragments.get(position);
    }
       
     @Override
     public int getCount() {
    	 return pageCount;
     }
     
     @Override
     public CharSequence getPageTitle(int position) {
    	 String title;
    	 if (!isDraggingPage) {
    		 title = (currentPosition == position) ? fragments.get(position).getFragmentName() : "";
    	 } else { 
    		 title = "";
    	 }
    	 SpannableStringBuilder sb = new SpannableStringBuilder(" " + title);
    	 Drawable icon = fragments.get(position).getIcon();
    	 if (icon != null) {
        	 icon.setBounds(0, 0, 45, 45);
        	 ImageSpan span = new ImageSpan(icon, ImageSpan.ALIGN_BOTTOM);
        	 sb.setSpan(span, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    	 }
    	 return sb;
     }
     
     public void setCurrentPage(int position) {
    	 currentPosition = position;
     }
     
     public void setIsDragging(boolean isDragging) {
    	 this.isDraggingPage = isDragging;
     }
     
     public boolean isDragging() {
    	 return isDraggingPage;
     }
}
