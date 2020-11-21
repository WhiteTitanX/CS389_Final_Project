package com.cs389f20.diamonds;

import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Iterator;

public class DrawButtons {

    public static void drawButtons(Iterator<?> it, RelativeLayout layout) {
        draw(it, layout, null);
    }

    public static void drawButtons(Iterator<?> it, LinearLayout layout) {
        draw(it, null, layout);
    }

    private static void draw(Iterator<?> it, RelativeLayout relativeLayout, LinearLayout linearLayout) //List<Property> or List<Builing>
    {
        ImageButton btn;
        MainActivity ma = MainActivity.getInstance();
        BuildingSelectActivity bsa = BuildingSelectActivity.getInstance();
        TextView tv;
        int relativeLastID = -1;
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
                if (relativeLastID == -1)
                    relativeLastID = R.id.propertySelectHeader;
            } else if (obj instanceof Building) {
                btn = new ImageButton(bsa);
                tv = new TextView(bsa);
                b = (Building) obj;
                tv.setText(b.name);
                tv.setTextSize(24f);
                if (relativeLastID == -1 && relativeLayout != null)
                    relativeLastID = relativeLayout.getId();
            } else
                continue;

            final String name = ((p != null) ? p.name : b.name);


            //Button Image & Clickable NOTE: Currently db doesn't support images, so it is hard-coded
            if (b != null) {
                if (b.name.equalsIgnoreCase("Miller"))
                    btn.setImageResource(R.drawable.pace_miller);
                else if (b.name.equalsIgnoreCase("Willcox"))
                    btn.setImageResource(R.drawable.willcox_hall);
                else
                    btn.setImageResource(R.drawable.default_building);
            } else {
                if (p.name.equalsIgnoreCase("Pace"))
                    btn.setImageResource(R.drawable.pace);
                else
                    btn.setImageResource(R.drawable.default_property);
            }
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
            if (linearLayout != null) {
                LinearLayout.LayoutParams[] params = getParams();
                linearLayout.addView(btn, params[0]);
                linearLayout.addView(tv, params[1]);
            } else if (relativeLayout != null) {
                RelativeLayout.LayoutParams[] params = getParams(relativeLastID);
                relativeLayout.addView(btn, params[0]);
                btn.setId(View.generateViewId());
                params[1].addRule(RelativeLayout.BELOW, btn.getId());
                relativeLayout.addView(tv, params[1]);
                tv.setId(View.generateViewId());
                relativeLastID = tv.getId();
            }
        }
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

    private static RelativeLayout.LayoutParams[] getParams(int relativeLastID) {
        RelativeLayout.LayoutParams paramsButton, paramsText;
        paramsButton = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsButton.addRule(RelativeLayout.BELOW, relativeLastID);
        paramsButton.setMargins(0, IMAGE_MARGIN_TOP, 0, 0);
        paramsButton.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);

        paramsText = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsText.setMargins(0, TEXT_MARGIN_TOP, 0, 0);
        paramsText.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        paramsText.addRule(RelativeLayout.TEXT_ALIGNMENT_CENTER, RelativeLayout.TRUE);
        return new RelativeLayout.LayoutParams[]{paramsButton, paramsText};
    }
}
