package com.officinetop.utils;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomLayout extends RelativeLayout {


    private GoogleMap map;
    private Marker marker;
    private int offPXL;
    private View infoWindow;


    public CustomLayout(Context context) {
        super(context);
    }

    public CustomLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /*
    must be called before we route touch events
     */
    public void init(GoogleMap map, int bottomOffsetPixels) {
        this.map = map;
        this.offPXL = bottomOffsetPixels;
    }

    public void setMarkerWithInfoWindow(Marker marker, View infoWindow) {
        this.marker = marker;
        this.infoWindow = infoWindow;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean ret = false;
        if (marker != null && marker.isInfoWindowShown() && map != null && infoWindow != null) {
            Point point = map.getProjection().toScreenLocation(marker.getPosition());
            MotionEvent copyEV = MotionEvent.obtain(ev);
            copyEV.offsetLocation(-point.x + (infoWindow.getWidth() / 2), -point.y + (infoWindow.getHeight() + offPXL));
            ret = infoWindow.dispatchTouchEvent(copyEV);
        }
        return ret || super.dispatchTouchEvent(ev);

    }
}
