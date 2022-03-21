package customize;

import android.graphics.drawable.Drawable;

import com.classmatelin.R;

public class musicPlayerInfos {
    private String musicPlayerName;
    private Drawable musicPlayerImage;
    private int selection;

    public musicPlayerInfos(Drawable icon,String name,int selection){
        musicPlayerImage=icon;
        musicPlayerName=name;
        this.selection=selection;
    }

    public String getMusicPlayerName() {
        return musicPlayerName;
    }

    public Drawable getMusicPlayerImage() {
        return musicPlayerImage;
    }

    public int getSelection() {
        return selection;
    }

    public void setSelection(boolean b){
        if(b){
            selection= R.drawable.selected;
        }else{
            selection=R.drawable.unselected;
        }
    }

}
