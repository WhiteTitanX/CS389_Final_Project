package com.cs389f20.diamonds;

import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Iterator;

public class DrawButtons {


    public static void drawButtons(Iterator<?> it, RelativeLayout layout) //List<Property> or List<Builing>
    {
        ImageButton btn;
        MainActivity ma = MainActivity.getInstance();
        BuildingSelectActivity bsa = BuildingSelectActivity.getInstance();
        TextView tv;
        int lastID = -1;
        final int IMAGE_MARGIN_TOP = 60, TEXT_MARGIN_TOP = 25;
        RelativeLayout.LayoutParams paramsButton, paramsText;
        if (!it.hasNext()) {
            Log.w(DrawButtons.class.getSimpleName(), "Error: can't draw buttons because there aren't any building or properties.");
            Toast.makeText(ma.getApplicationContext(), "There aren't any connected properties/buildings.", Toast.LENGTH_LONG).show();
            return;
        }
        while (it.hasNext()) {
            final Object obj = it.next();
            Property p = null;
            Building b = null;


            if (obj instanceof Property) {
                btn = new ImageButton(ma);
                tv = new TextView(ma);
                p = (Property) obj;
                tv.setText(p.name);
                if (lastID == -1)
                    lastID = R.id.propertySelectHeader;
            } else if (obj instanceof Building) {
                btn = new ImageButton(bsa);
                tv = new TextView(bsa);
                b = (Building) obj;
                tv.setText(b.name);
                tv.setTextSize(24f);
                if (lastID == -1)
                    lastID = layout.getId();
            } else
                continue;

            final String name = ((p != null) ? p.name : b.name);

            //Layout parameters
            paramsButton = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            paramsButton.addRule(RelativeLayout.BELOW, lastID);
            paramsButton.setMargins(0, IMAGE_MARGIN_TOP, 0, 0);
            paramsButton.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);

            paramsText = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            paramsText.setMargins(0, TEXT_MARGIN_TOP, 0, 0);
            paramsText.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            paramsText.addRule(RelativeLayout.TEXT_ALIGNMENT_CENTER, RelativeLayout.TRUE);


            //Button Image & Clickable
            btn.setImageResource(R.drawable.ic_launcher_background); //three or one house
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   if(obj instanceof Property)
                       MainActivity.getInstance().launchBuildingSelectActivity(name);
                   else
                       BuildingSelectActivity.getInstance().launchBuildingActivity(v, name);
                }
            });

            //Add Button and Text to Layout
            layout.addView(btn, paramsButton);
            btn.setId(View.generateViewId());
            paramsText.addRule(RelativeLayout.BELOW, btn.getId());
            layout.addView(tv, paramsText);
            tv.setId(View.generateViewId());
            lastID = tv.getId();
        }
    }

}
