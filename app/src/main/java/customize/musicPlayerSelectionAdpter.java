package customize;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.classmatelin.R;

import java.util.List;

public class musicPlayerSelectionAdpter extends RecyclerView.Adapter<musicPlayerSelectionAdpter.ViewHolder> {

    private List<musicPlayerInfos> musicPlayerInfosList;
    static class ViewHolder extends RecyclerView.ViewHolder{

        View itemView;
        ImageView mIcon;
        TextView mName;
        ImageView mSelection;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView=itemView;
            mIcon=(ImageView) itemView.findViewById(R.id.music_player_icon);
            mName=(TextView) itemView.findViewById(R.id.music_player_name);
            mSelection=(ImageView) itemView.findViewById(R.id.music_player_select);
        }
    }

    public musicPlayerSelectionAdpter(List<musicPlayerInfos> list){
        musicPlayerInfosList=list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.music_palyer_item,parent,false);
        ViewHolder holder=new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        musicPlayerInfos playerInfos=musicPlayerInfosList.get(position);

        holder.mIcon.setImageDrawable(playerInfos.getMusicPlayerImage());
        holder.mSelection.setImageResource(playerInfos.getSelection());
        holder.mName.setText(playerInfos.getMusicPlayerName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences=view.getContext().getSharedPreferences("data",MODE_PRIVATE);
                int currentPos=sharedPreferences.getInt(view.getContext().getString(R.string.MPS),0);

                if(currentPos==position) return;
                //设置记录为新播放器
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putString(view.getContext().getString(R.string.MP), (String) holder.mName.getText());
                editor.putInt(view.getContext().getString(R.string.MPS),position);
                editor.apply();
                //设置图标
                musicPlayerInfos playerInfos=musicPlayerInfosList.get(position);
                playerInfos.setSelection(true);

                //以前选中的播放器
                playerInfos=musicPlayerInfosList.get(currentPos);
                playerInfos.setSelection(false);
                notifyDataSetChanged();
                //Toast.makeText(view.getContext(),String.valueOf(position),Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return musicPlayerInfosList.size();
    }
}

