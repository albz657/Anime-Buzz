package me.jakemoritz.animebuzz.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.IOException;
import java.util.List;

public class CircleBitmapTask extends AsyncTask<List<CircleBitmapHolder> , Void, List<CircleBitmapHolder> > {

    @Override
    protected void onPostExecute(List<CircleBitmapHolder> circleBitmapHolders) {
        App.getInstance().cacheCircleBitmaps(circleBitmapHolders);
    }

    @Override
    protected List<CircleBitmapHolder>  doInBackground(List<CircleBitmapHolder> ... files) {
        int width = App.getInstance().getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
        int height = App.getInstance().getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_height);

        for (CircleBitmapHolder bitmapHolder : files[0]){
            Bitmap circleBitmap = null;
            try {
                circleBitmap = Picasso.with(App.getInstance()).load(bitmapHolder.getFile()).resize(width, height).centerCrop().transform(new CircleTransform()).get();
            } catch (IOException e){
                e.printStackTrace();
            }

            if (circleBitmap != null){
                bitmapHolder.setBitmap(circleBitmap);

            }
        }


        return files[0];
    }

    public class CircleTransform implements Transformation{
        @Override
        public Bitmap transform(Bitmap source) {
            int minSide = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - minSide) / 2;
            int y = (source.getHeight() - minSide) / 2;

            Bitmap tempBitmap = Bitmap.createBitmap(source, x, y, minSide, minSide);
            if (tempBitmap != source){
                source.recycle();
            }

            Bitmap bitmap = Bitmap.createBitmap(minSide, minSide, source.getConfig());

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(tempBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);


            float r = minSide/2f;
            canvas.drawCircle(r, r, r, paint);

            tempBitmap.recycle();
            return bitmap;
        }

        @Override
        public String key() {
            return "circle";
        }
    }
}
