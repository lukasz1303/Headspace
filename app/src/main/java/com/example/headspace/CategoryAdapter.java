package com.example.headspace;

import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Context mContext;
    private List<String> mCategories;

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item,parent,false);
        return new CategoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final CategoryViewHolder holder, final int position) {
        holder.mTextViewCategoryName.setText(mCategories.get(position));
        holder.mRelativeLayoutCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext.getApplicationContext(),CategoryActivity.class);
                i.setAction(Intent.ACTION_SEND);
                i.putExtra("categoryName",mCategories.get(position));
                if (i.resolveActivity(mContext.getPackageManager()) != null) {
                    mContext.startActivity(i);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mCategories!= null){
            return mCategories.size();
        }
        return 0;
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder{
        TextView mTextViewCategoryName;
        RelativeLayout mRelativeLayoutCategory;
        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextViewCategoryName = itemView.findViewById(R.id.tv_category_name);
            mRelativeLayoutCategory = itemView.findViewById(R.id.relative_layout_category);
        }
    }

    CategoryAdapter(Context context, List<String> categories){
        mContext = context;
        mCategories = categories;
    }
}
