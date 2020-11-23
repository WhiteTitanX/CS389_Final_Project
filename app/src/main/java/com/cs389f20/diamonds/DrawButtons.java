package com.cs389f20.diamonds;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

public class DrawButtons {

    public static void drawButtons(Iterator<?> it, LinearLayout layout) {
        draw(it, layout);
    }

    private static void draw(Iterator<?> it, LinearLayout layout) //List<Property> or List<Builing>
    {
        ImageButton btn;
        MainActivity ma = MainActivity.getInstance();
        BuildingSelectActivity bsa = BuildingSelectActivity.getInstance();
        TextView tv;
        if (!it.hasNext()) {
            Log.w(DrawButtons.class.getSimpleName(), "Error: can't draw buttons because there aren't any building or properties.");
            Toast.makeText(ma.getApplicationContext(), "There aren't any connected properties/buildings.", Toast.LENGTH_LONG).show();
            return;
        }

        //Clear all current buttons
        layout.removeAllViews();

        while (it.hasNext()) {
            final Object obj = it.next();
            Property p = null;
            Building b = null;

            if (obj instanceof Property) {
                p = (Property) obj;
                btn = new ImageButton(ma);
                tv = new TextView(ma);
                tv.setText(p.name);
            } else if (obj instanceof Building) {
                btn = new ImageButton(bsa);
                tv = new TextView(bsa);
                b = (Building) obj;
                tv.setText(b.name);
                tv.setTextSize(24f);
            } else
                continue;

            final String name = ((p != null) ? p.name : b.name);

            //Button Image
            if (b != null) {
                btn.setImageResource(R.drawable.default_building);
                LoadImageFromWebOperations(btn, b.image_url);

            } else {
                if (p.name.equalsIgnoreCase("Pace University"))
                    btn.setImageResource(R.drawable.pace);
                else
                    btn.setImageResource(R.drawable.default_property);
            }

            //Button Clickable
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (obj instanceof Property)
                        MainActivity.getInstance().launchBuildingSelectActivity(name);
                    else
                        BuildingSelectActivity.getInstance().launchBuildingActivity(v, name);
                }
            });

            //Layout Params & Add Button and Text to Layout
            LinearLayout.LayoutParams[] params = getParams();
            layout.addView(btn, params[0]);
            layout.addView(tv, params[1]);
        }
        if (layout.getVisibility() == View.INVISIBLE)
            layout.setVisibility(View.VISIBLE);
    }

    final static int IMAGE_MARGIN_TOP = 75, TEXT_MARGIN_TOP = 5;

    private static LinearLayout.LayoutParams[] getParams() {
        LinearLayout.LayoutParams paramsButton, paramsText;
        paramsButton = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsButton.setMargins(0, IMAGE_MARGIN_TOP, 0, 0);

        paramsText = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsText.setMargins(0, TEXT_MARGIN_TOP, 0, 0);
        return new LinearLayout.LayoutParams[]{paramsButton, paramsText};
    }

    private static void LoadImageFromWebOperations(final ImageButton btn, final String url) {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    InputStream is = (InputStream) new URL(url).getContent();
                    final Drawable d = Drawable.createFromStream(is, null);
                    Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
                    final Drawable d2 = new BitmapDrawable(BuildingSelectActivity.getInstance().getResources(),
                            Bitmap.createScaledBitmap(bitmap, 512, 512, true));
                    Handler handler = new android.os.Handler(Looper.getMainLooper());
                    Runnable img = new Runnable() {
                        @Override
                        public void run() {
                            btn.setImageDrawable(d2);
                        }
                    };
                    handler.post(img);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }
}
