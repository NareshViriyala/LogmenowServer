package activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.home.logmenowserver.R;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import adapter.AdapterEntityType;
import database.DBHelper;
import dialog.DialogInformation;
import shared.CommonClasses;

/**
 * Created by Home on 8/23/2016.
 */
public class ActivityEntity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener{

    private DBHelper mydb;
    private String PageName = "ActivityEntity";
    private TextView tv_title;
    private ImageView img_back;
    private ImageView img_sync;
    private ImageView img_more;
    //private ImageView img_loading;
    private GridView gv_usage;
    private CommonClasses cc;
    List<String> etypes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_entity);
            mydb = new DBHelper(this);
            cc = new CommonClasses(this);
            tv_title = (TextView) findViewById(R.id.tv_title);
            tv_title.setOnClickListener(this);
            img_back = (ImageView) findViewById(R.id.img_back);
            img_back.setOnClickListener(this);
            img_sync = (ImageView) findViewById(R.id.img_sync);
            img_sync.setOnClickListener(this);
            img_more = (ImageView) findViewById(R.id.img_more);
            img_more.setOnClickListener(this);

            gv_usage = (GridView) findViewById(R.id.gv_usage);
            gv_usage.setOnItemClickListener(this);

            JSONArray jsonArray = new JSONArray(getIntent().getStringExtra("Types"));
            for (int i=0; i<jsonArray.length();i++) {
                //etypes.add(jsonArray.getJSONObject(i).getString("EntityType").replace("Mall","Parking"));
                etypes.add(jsonArray.getJSONObject(i).getString("EntityType"));
            }
            if(etypes.size() > 0){
                AdapterEntityType adapter = new AdapterEntityType(this, etypes);
                gv_usage.setAdapter(adapter);
            }else{
                String appname = getResources().getString(R.string.app_name);
                String clientappname = getResources().getString(R.string.client_app_name);
                String text = "This device is not registered with <u><b>"+appname+"</b></u>";
                text = text + "<br/>Please contact us for assistance.";
                text = text + "<br/><br/>Looking for <u><b>"+clientappname+"</b></u> instead?";
                text = text + "<br/>Please download it from AppStore.";
                new DialogInformation(this, Html.fromHtml(text), null).show();
            }

        }
        catch (Exception e){mydb.logAppError(PageName, "onCreate", "Exception", e.getMessage());}
    }

    @Override
    public void onClick(View v) {
        try{
            switch (v.getId()){
                case R.id.img_back:
                    onBackPressed();
                    break;
                case R.id.tv_title:
                    onBackPressed();
                    break;
                case R.id.img_call:
                    break;
                case R.id.img_sync:
                    break;
                case R.id.img_more:
                    Toast.makeText(ActivityEntity.this, mydb.getDeviceID(), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
        catch (Exception e){mydb.logAppError(PageName, "onClick", "Exception", e.getMessage());}
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            Class<?> c = Class.forName("activity.Activity"+etypes.get(position));
            Intent intent = new Intent(this, c);
            startActivity(intent);
            this.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
        }
        catch (Exception e){mydb.logAppError(PageName, "onItemClick", "Exception", e.getMessage());}
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    @Override
    public void onResume() {
        super.onResume();
        tv_title.setText("Usage Options");
        img_back.setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    @Override
    public void onDestroy(){super.onDestroy();}
}
