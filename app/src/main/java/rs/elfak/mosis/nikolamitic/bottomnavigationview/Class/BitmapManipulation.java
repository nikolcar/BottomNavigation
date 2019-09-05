package rs.elfak.mosis.nikolamitic.bottomnavigationview.Class;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;

import rs.elfak.mosis.nikolamitic.bottomnavigationview.R;

public class BitmapManipulation {
    public static Bitmap getCroppedBitmap(Bitmap bitmap) {
        //scale bitmap so it takes less memory space
        double ratio = (double) bitmap.getWidth() / (double) bitmap.getHeight();
        int newWidth, newHeight;
        if (bitmap.getHeight() > bitmap.getWidth()) {
            newWidth = 256;
            newHeight = (int) (newWidth / ratio);
        } else {
            newHeight = 256;
            newWidth = (int) (newHeight * ratio);
        }

        bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);

        //creating new square bitmap
        int smallerSide = newWidth;
        if (newHeight < newWidth) {
            smallerSide = newHeight;
        }

        Bitmap output = Bitmap.createBitmap(smallerSide, smallerSide, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, smallerSide, smallerSide);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        canvas.drawCircle(smallerSide / 2, smallerSide / 2, smallerSide / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }

    public static Bitmap getMarkerBitmapFromView(@DrawableRes int resId, Context mContext) {
        View customMarkerView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.item_avatar_image, null);
        ImageView markerImageView = customMarkerView.findViewById(R.id.item_avatar);
        markerImageView.setImageResource(resId);
        markerImageView.setBackgroundColor(Color.TRANSPARENT);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }
}