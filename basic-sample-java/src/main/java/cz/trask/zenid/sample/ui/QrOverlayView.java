package cz.trask.zenid.sample.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class QrOverlayView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public QrOverlayView(Context context) {
        super(context);
    }

    public QrOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float density = getResources().getDisplayMetrics().density;
        float boxSize = Math.min(getWidth(), getHeight()) * 0.65f;
        float left = (getWidth() - boxSize) / 2f;
        float top = (getHeight() - boxSize) / 2f;
        float cornerLen = boxSize * 0.12f;

        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4 * density);
        paint.setPathEffect(new DashPathEffect(new float[]{cornerLen, boxSize - cornerLen}, 0));

        canvas.drawRoundRect(new RectF(left, top, left + boxSize, top + boxSize), 8 * density, 8 * density, paint);
    }
}
