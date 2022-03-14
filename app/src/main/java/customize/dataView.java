package customize;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.classmatelin.R;

public class dataView extends FrameLayout {

    private Context mContext;
    private View contentView;
    private View topView;
    private View downView;
    //private View leftView;
    //private View rightView;
    private TextView dataName;
    private TextView dataData;

    public static final int NO_LINE=0;
    public static final int DIVIDE_LINE=1;

    public dataView(Context context){
        this(context,null);
    }
    public dataView(Context context,AttributeSet attributeSet){
        this(context,attributeSet,0);
    }
    public dataView(Context context, AttributeSet attributeSet,int defStyleAttr) {
        super(context,attributeSet,defStyleAttr);
        init(context,attributeSet);
    }

    private  void init(Context context,AttributeSet attributeSet){
        mContext=context;
        LayoutInflater inflater=(LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentView=inflater.inflate(R.layout.data,this,true);
        topView=findViewById(R.id.top_divide_line);
        downView=findViewById(R.id.down_divide_line);
        //leftView=findViewById(R.id.left_divide_line);
        //rightView=findViewById(R.id.right_divide_line);
        dataName=(TextView) findViewById(R.id.data_name);
        dataData=(TextView)findViewById(R.id.data_data);

        TypedArray typedArray=mContext.obtainStyledAttributes(attributeSet,R.styleable.dataLayout);

        topView.setVisibility(typedArray.getInt(R.styleable.dataLayout_top_divide_line,DIVIDE_LINE)==1?VISIBLE:GONE);
        downView.setVisibility(typedArray.getInt(R.styleable.dataLayout_down_divide_line,DIVIDE_LINE)==1?VISIBLE:GONE);
        //leftView.setVisibility(typedArray.getInt(R.styleable.dataLayout_left_divide_line,DIVIDE_LINE)==1?VISIBLE:GONE);
        //rightView.setVisibility(typedArray.getInt(R.styleable.dataLayout_right_divide_line,DIVIDE_LINE)==1?VISIBLE:GONE);
        dataName.setText(typedArray.getString(R.styleable.dataLayout_data_name));
        dataData.setText(typedArray.getString(R.styleable.dataLayout_data_data));

    }
}
