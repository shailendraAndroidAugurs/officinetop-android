package com.officinetop.officine.utils;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import com.google.android.gms.maps.model.Marker;


public abstract class OnInfoWindowElemTouchListener implements View.OnTouchListener {

    private final View view;
    private final Drawable bgDrawableNormal;
    private final Drawable bgDrawablePressed;
    private final Handler handler = new Handler();
    private Marker marker;
    private boolean pressed = false;


    public OnInfoWindowElemTouchListener(View view, Drawable bgDrawableNormal, Drawable bgDrawablePressed){
        this.view = view;
        this.bgDrawableNormal = bgDrawableNormal;
        this.bgDrawablePressed = bgDrawablePressed;
    }

    public void setMarker(Marker marker){
        this.marker = marker;
    }


    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (0<=event.getX() && event.getX()<=view.getWidth() && 0<=event.getY() && event.getY()<=view.getHeight()){
            switch (event.getActionMasked()){
                case MotionEvent.ACTION_DOWN:
                    startPress();
                    break;
                    case MotionEvent.ACTION_UP:
                        handler.postDelayed(confirmClickRunnable,150);
                        break;
                        case MotionEvent.ACTION_CANCEL:
                            endPress();
                            break;
                            default:
                                break;
            }
        }else{
            endPress();
        }
        return false;
    }

    private void startPress(){
        if (!pressed){
            pressed=true;
            handler.removeCallbacks(confirmClickRunnable);
            view.setBackground(bgDrawablePressed);
            if (marker!=null)
                marker.showInfoWindow();
        }
    }

    private boolean endPress(){
        if (pressed){
            pressed=false;
            handler.removeCallbacks(confirmClickRunnable);
            view.setBackground(bgDrawableNormal);
            if (marker!=null)
                marker.showInfoWindow();
            return true;
        }else
            return false;
    }

    private final Runnable confirmClickRunnable = new Runnable() {
        @Override
        public void run() {
            if (endPress()){
                onClickConfirmed(view,marker);
            }
        }
    };

    protected abstract void onClickConfirmed(View view, Marker marker);

}
