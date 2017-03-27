package com.jkm.rgbcontroller;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class AnalogueView extends View {
    private static final int MAX_VALUE = 180;

    private OnMoveListener mMoveListener;

    private Paint black = new Paint();
    private Paint grey = new Paint();
    private Paint white = new Paint();

    private float x, y;
    private double r, t;
    private int cx, cy, w;
    private int smallCircleRadius = 0;

    private int toDo;

    public AnalogueView(Context context, AttributeSet attrs) {
        super(context, attrs);
        black.setColor(Color.BLACK);
        grey.setColor(Color.GRAY);
        white.setColor(Color.WHITE);
        black.setFlags(Paint.ANTI_ALIAS_FLAG);
        white.setFlags(Paint.ANTI_ALIAS_FLAG);
        grey.setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int dw, int dh) {
        this.w = w;
        cx = w / 2;
        cy = h / 2;
        x = cx;
        y = cy;
        smallCircleRadius = w / 6;
        super.onSizeChanged(w, h, dw, dh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawMyStuff(canvas);
        switch (toDo) {
            case 1:
                //center();
                break;
            default:
                break;
        }
    }

    private void drawMyStuff(final Canvas canvas) {
        canvas.drawCircle(cx, cy, w / 2, black);
        canvas.drawCircle(cx, cy, w / 2 - 5, grey);
        canvas.drawCircle(cx, cy, w / 2 - 10, black);
        canvas.drawCircle(x, y, smallCircleRadius + 2, white);
        canvas.drawCircle(x, y, smallCircleRadius, grey);
    }

    // n2p  : normal to polar coordinates conversion
    // p2n  : polar to normal coordinates conversion
    // R    : distance to polar center
    // T    : polar angle
    double n2pR(double x, double y) {
        return distance(x, y, cx, cy);
    }

    double n2pT(double x, double y) {
        return Math.atan2((y - cy), (x - cx));
    }

    double p2nX(double r, double t) {
        return r * Math.cos(t) + cx;
    }

    double p2nY(double r, double t) {
        return r * Math.sin(t) + cy;
    }

    double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                updatePosition(event);
                break;
            case MotionEvent.ACTION_MOVE:
                updatePosition(event);
                break;
            case MotionEvent.ACTION_UP:
                toDo = 1;
                invalidate();
                break;
            default:
                break;
        }
        return true;
    }

    private void center() {
        if (r > 15) {
            r -= 15;
        } else {
            toDo = 0;
            r = 0;
        }
        x = (float) p2nX(r, t);
        y = (float) p2nY(r, t);

        if (mMoveListener != null) {
            mMoveListener.onCenter();
        }
        invalidate();
    }

    void updatePosition(MotionEvent e) {
        r = Math.min(w / 2 - smallCircleRadius, n2pR(e.getX(), e.getY()));
        t = n2pT(e.getX(), e.getY());
        x = (float) p2nX(r, t);
        y = (float) p2nY(r, t);

        int range = w - 2 * smallCircleRadius;
        int X = (int) ((MAX_VALUE * (x - smallCircleRadius)) / range);
        int Y = (int) ((MAX_VALUE * (smallCircleRadius - y + range)) / range);

        if (mMoveListener != null) {
            mMoveListener.onAnalogueMove(X, Y);
        }
        invalidate();
    }

    public void setOnMoveListener(OnMoveListener listener) {
        mMoveListener = listener;
    }

    interface OnMoveListener {
        void onCenter();

        void onAnalogueMove(int X, int Y);
    }
}