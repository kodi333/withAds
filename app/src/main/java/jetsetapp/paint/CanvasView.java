package jetsetapp.paint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;


public class CanvasView extends View {

    private static final float TOLERANCE = 5;
    protected Paint paint = new Paint();
    Context context;
    FileOutputStream fos = null;
    View undoButton;
    private List<Path> paths = new ArrayList<Path>();
    private List<Integer> colors = new ArrayList<Integer>();
    private List<Float> strokes = new ArrayList<Float>();
    private ArrayList<Path> undonePaths = new ArrayList<Path>();
    private ArrayList<Integer> undoneColors = new ArrayList<Integer>();
    private ArrayList<Float> undoneStrokes = new ArrayList<Float>();
    private int currentColor = Color.BLACK;
    private float currentStroke = 4F;
    private Bitmap mBitmap;
    private Canvas canvas;
    private Path path = new Path();
    private float mX;
    private float mY;
    private ViewStub view;


    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        setFocusable(true);
        setFocusableInTouchMode(true);

        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.BLACK); // BLACK
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(currentStroke);

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        canvas = new Canvas(mBitmap);

    }


    public void saveFile() {

        mBitmap = Bitmap.createBitmap(this.getWidth(),this.getHeight(),Bitmap.Config.ARGB_8888);

        Save savefile = new Save();
        savefile.SaveImage(context,mBitmap);
    }

    public void changeColor(int color) {
        currentColor = color;
        path = new Path();
    }


    public int getColor() {
        int color = currentColor;
       return color;
    }

//    public int getPathsSize(){
//        int size = paths.size();
//        return size;
//    }

    public void changeStroke(float size) {
        currentStroke = size;
        path = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawARGB(255,255,255,255);
        for (int x = 0; x < paths.size(); x++) {
            paint.setColor(colors.get(x));
            paint.setStrokeWidth(strokes.get(x));
            canvas.drawPath(paths.get(x), paint);
        }
        paint.setColor(currentColor);
        paint.setStrokeWidth(currentStroke);
        canvas.drawPath(path, paint);
    }

    private void startTouch(float x, float y) {
        path.moveTo(x, y);
        mX = x;
        mY = y;

    }

    private void moveTouch(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOLERANCE || dy >= TOLERANCE) {
            path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void upTouch() {
        path.lineTo(mX, mY);
        canvas = new Canvas();
        path = new Path();

//         Show undo redo buttons
        if(paths.size() > 0){
            MainActivity.undoButton.setVisibility(View.VISIBLE);
        }
    }

    public void undoLastDraw(){
        if(paths.size() > 0) {
            undonePaths.add(paths.remove(paths.size() - 1));
            undoneColors.add(colors.remove(colors.size() - 1));
            undoneStrokes.add(strokes.remove(strokes.size() - 1));

            if(paths.size() <= 0){
                MainActivity.undoButton.setVisibility(View.INVISIBLE);
            }

            if(undonePaths.size() > 0){
                MainActivity.redoButton.setVisibility(View.VISIBLE);
            }

//              invalidate();
        }
    }

    public void redoLastDraw(){
        if(undonePaths.size()>0) {
            paths.add(undonePaths.remove(undonePaths.size()-1));
            colors.add(undoneColors.remove(undoneColors.size()-1));
            strokes.add(undoneStrokes.remove(undoneStrokes.size()-1));

            if(undonePaths.size() <= 0){
                MainActivity.redoButton.setVisibility(View.INVISIBLE);
            }

            if(undonePaths.size() > 0){
                MainActivity.redoButton.setVisibility(View.VISIBLE);
            }

            if(paths.size() > 0){
            MainActivity.undoButton.setVisibility(View.VISIBLE);
            }
        }
    }

    public void clearCanvas() {

        path.reset();
        undonePaths.clear();
        paths.clear();
        colors.clear();
        strokes.clear();
        path = new Path();
        if(paths.size() <= 0){
            MainActivity.undoButton.setVisibility(View.INVISIBLE);
        }
//
        if(undonePaths.size() <= 0){
            MainActivity.redoButton.setVisibility(View.INVISIBLE);
        }
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                moveTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                paths.add(path);
                colors.add(currentColor);
                strokes.add(currentStroke);
                upTouch();
                invalidate();
                break;
        }

        return true;
    }

}
