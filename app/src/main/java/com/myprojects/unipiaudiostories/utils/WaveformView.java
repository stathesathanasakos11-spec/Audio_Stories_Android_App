package com.myprojects.unipiaudiostories.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.Random;

public class WaveformView extends View {
    private Paint paint = new Paint();
    // ένας πίνακας με 44 ψευδο-τυχαίες τιμές για τα ύψη της μπάρας-κύματος που θα φτιάξω
    private float[] heights = new float[44];
    // το ποσοστό ολοκλήρωσης της εκφώνησης της εργασίας
    private float progress = 0;
    private Random random = new Random();


    public WaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // σχεδιάζω με μωβ χρώμα για να μοιάζει στο dark theme της εφαρμογής
        paint.setColor(Color.parseColor("#6200EE"));
        paint.setStrokeWidth(8f); // πλάτος κάθε μπάρας-στήλης
        paint.setStrokeCap(Paint.Cap.ROUND);
        // τα ύψη στον πίνακα θα είναι τυχαία
        for (int i = 0; i < heights.length; i++) heights[i] = random.nextFloat();
    }



    public void setProgress(float progress) {
        this.progress = progress;
        invalidate(); // σχεδίαση ξανά με βάση την τιμή του progress
        // πρακτικά κάθε φορά και για κάθε χαρακτήρα καλείται ξανά και ξανά η setProgress()
        // ώστε να γίνεται όσο το δυνατόν καλύτερη ενημέρωση της μπάρας μετά την εκφώνηση κάθε χαρακτήρα
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        // μία λογική απόσταση μεταξύ των στοιχείων της μπάρας
        // διαιρώ πλάτος οθόνης με το πλήθος των γραμμών της μπάρας για flexibility σε όλα τα μεγέθη οθόνης
        float spacing = width / heights.length;

        for (int i = 0; i < heights.length; i++) {
            float x = i * spacing + spacing / 2;
            float currentBarHeight = heights[i] * height * 0.8f;

            // υπολογίζω δυναμικά την σχέση προόδου και θέσης στη μπάρα
            float barProgressPosition = ((float) i / heights.length) * 100;

            // άλλο χρώμα για το ολοκληρωμένο μέρος της μπάρας
            // άλλο χρώμα για το μελλοντικό τμήμα της μπάρας
            if (barProgressPosition <= progress) {
                paint.setColor(Color.parseColor("#BB86FC"));
            } else {
                paint.setColor(Color.parseColor("#6200EE"));
            }

            // σχεδίαση της γραμμής
            canvas.drawLine(x, height/2 - currentBarHeight/2, x, height/2 + currentBarHeight/2, paint);
        }

        // στο τρέχον σημείο-θέση της μπάρας ένας λευκός cursor
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(4f);
        float cursorX = (progress / 100) * width;
        canvas.drawLine(cursorX, 0, cursorX, height, paint);
    }
}