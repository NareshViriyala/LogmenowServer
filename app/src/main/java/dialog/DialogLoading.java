package dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Spanned;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.home.logmenowserver.R;

import database.DBHelper;

/**
 * Created by Home on 8/16/2016.
 */
public class DialogLoading extends Dialog {
    private Context context;
    public DialogLoading(Context context) {
        super(context);
        this.context = context;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_processing);
        ImageView img_processing = (ImageView) findViewById(R.id.img_processing);
        Glide.with(context).load(R.drawable.loading).into(img_processing);
        this.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
    }
}