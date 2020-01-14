package com.example.constraintlayouttest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Weather_Item_Adapter extends RecyclerView.Adapter<Weather_Item_Adapter.ViewHolder> {

    private List<String[]> mString;
    public Weather_Item_Adapter(List<String[]> mString) {
        super();
        this.mString=mString;
    }
    static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView textView4;
        TextView textView5;
        TextView textView8;

        public ViewHolder(View view)
        {
            super(view);
             textView4=view.findViewById(R.id.textView4);
             textView5=view.findViewById(R.id.textView5);
             textView8=view.findViewById(R.id.textView8);
        }
    }
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.weather_item,parent,false);
        ViewHolder holder=new ViewHolder(view);
        return holder;
    }
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String[] temp=mString.get(position);
        holder.textView4.setText(temp[0]);
        holder.textView5.setText(temp[1]);
        holder.textView8.setText(temp[2]);
//        holder.textView4.setText("16~24℃");
//        holder.textView5.setText("晴");
//        holder.textView8.setText("周三");
    }
    public int getItemCount() {
        return 3;
    }
}
