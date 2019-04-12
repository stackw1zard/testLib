package com.stackwizards.mcq_wizard.dialog;

import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.stackwizards.mcq_wizard.R;

public class ResultDialog {

    //  CHANGE TO INTERFACE TO SUPPORT MORE THAN THIS
    public static void showCustomDialog(DialogResultInterface dialogContext) {
//        Activity activity, int points,int msgIndex, View view;
        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
        String[] results = {"Dude, you'll have to try harder", "Almost There, keep at it.", "A Real Wizard get more that 100%", "You are my Master ;)"};
        ViewGroup viewGroup = dialogContext.getView().findViewById(android.R.id.content);


        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(dialogContext.getmActivity().getApplicationContext()).inflate(R.layout.my_dialog, viewGroup, false);


        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(dialogContext.getmActivity());

        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView);


        //finally creating the alert dialog and displaying it
        AlertDialog alertDialog = builder.create();

        dialogView.findViewById(R.id.btnGameOver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
//                startActivity(new Intent(NoConnectionActivity.this, StatusActivity.class));
//                ((AppCompatActivity)NoConnectionActivity.this).finish();

            }
        });

        TextView gameOverTextMessage = ((TextView) dialogView.findViewById(R.id.gameOverTextMessage));
        int msgIndex = (dialogContext.getPointScore()) / dialogContext.getQuestion_array().size();

        if (msgIndex < 0) {
            msgIndex = 0;
        } else if (msgIndex > 3) {
            msgIndex = 3;
        }

        gameOverTextMessage.setText(results[msgIndex] + "\n    Score: " + dialogContext.getPointScore());
        switch (msgIndex) {
            case 0:
                ((ImageView) dialogView.findViewById(R.id.gameResultIcon)).setImageResource(R.drawable.skull_dude_icon);
                break;
            case 1:
                ((ImageView) dialogView.findViewById(R.id.gameResultIcon)).setImageResource(R.drawable.stack_app_logout_icon);
                break;
            case 2:
                ((ImageView) dialogView.findViewById(R.id.gameResultIcon)).setImageResource(R.drawable.skull_pirate_icon);
                break;
            case 3:
                ((ImageView) dialogView.findViewById(R.id.gameResultIcon)).setImageResource(R.drawable.skull_wizard_icon);
                break;
        }

        alertDialog.show();
//
    }


}
