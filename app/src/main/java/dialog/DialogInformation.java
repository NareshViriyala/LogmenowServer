package dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Spanned;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.home.logmenowserver.R;

import database.DBHelper;
import shared.CommonClasses;
import shared.GlobalClass;

/**
 * Created by Home on 8/10/2016.
 */
public class DialogInformation extends Dialog {
    private Button btn_ok;
    private ImageView img_entitylogo;
    private GlobalClass gc;
    private Context context;
    private DBHelper mydb;
    private CommonClasses cc;

    private TextView tv_line;
    private String ImageName;
    private Spanned textDecoration;

    public DialogInformation(Context context, Spanned textDecoration, String ImageName) {
        super(context);
        this.context = context;
        mydb = new DBHelper(context);
        cc = new CommonClasses(context);
        this.textDecoration = textDecoration;
        this.ImageName = ImageName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_information);
        gc = (GlobalClass) context.getApplicationContext();

        img_entitylogo = (ImageView) findViewById(R.id.img_entitylogo);
        img_entitylogo.setImageResource(R.drawable.ic_logo);

        /*byte[] bytes = mydb.getEntityImage(ImageName);
        Bitmap bitmap = null;

        if(bytes != null) {
            bitmap = cc.getBitmap(bytes);
            img_entitylogo.setImageBitmap(bitmap);
        }
        else
            img_entitylogo.setImageResource(R.drawable.ic_noimage);*/

        tv_line = (TextView) findViewById(R.id.tv_line);
        tv_line.setText(textDecoration);

        btn_ok = (Button) findViewById(R.id.btn_ok);
        //this.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        this.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}