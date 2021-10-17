package uz.soft.blogapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import uz.soft.blogapp.R;
import uz.soft.blogapp.models.Post;

public class RowAdapter extends RecyclerView.Adapter<RowAdapter.MyVH> {

    private Context myContext;
    private List<Post> myData;

    public RowAdapter(Context myContext, List<Post> myData) {
        this.myContext = myContext;
        this.myData = myData;
    }

    @NonNull
    @Override
    public MyVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View row = LayoutInflater.from(myContext).inflate(R.layout.row_post_item, parent, false);
        return new MyVH(row);
    }

    @Override
    public void onBindViewHolder(@NonNull MyVH holder, int position) {
        holder.onBind(myData.get(position), myContext);
    }

    @Override
    public int getItemCount() {
        return myData.size();
    }

    class MyVH extends RecyclerView.ViewHolder {

        private TextView tvTitle;
        private ImageView bgImg, userImg;

        public MyVH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.row_title_txt);
            bgImg = itemView.findViewById(R.id.row_bg_img);
            userImg = itemView.findViewById(R.id.row_user_img);
        }

        public void onBind(Post post, Context myContext) {
            tvTitle.setText(post.getTitle());
            Glide.with(myContext).load(post.getPicture()).into(bgImg);
            Glide.with(myContext).load(post.getUserPhoto()).into(userImg);
        }
    }
}
