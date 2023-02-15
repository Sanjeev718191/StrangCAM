package com.sk.strangcam.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.sk.strangcam.Models.Message;
import com.sk.strangcam.R;
import com.sk.strangcam.databinding.ItemReceiveBinding;
import com.sk.strangcam.databinding.ItemSendBinding;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter{

    private Context context;
    private ArrayList<Message> messagesList;
    final int ITEM_SENT = 1;
    final int ITEM_RECEIVE = 2;

    public ChatAdapter(){}

    public ChatAdapter(Context context, ArrayList<Message> messagesList) {
        this.context = context;
        this.messagesList = messagesList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == ITEM_SENT){
            View view = LayoutInflater.from(context).inflate(R.layout.item_send, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_receive, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messagesList.get(position);
        if(FirebaseAuth.getInstance().getUid().equals(message.getSenderId())) return ITEM_SENT;
        else return ITEM_RECEIVE;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messagesList.get(position);
        if(holder.getClass() == SenderViewHolder.class){
            SenderViewHolder viewHolder = (SenderViewHolder) holder;
            viewHolder.binding.messageSend.setText(message.getMessage());
        } else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            viewHolder.binding.messageReceive.setText(message.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }


    // =========================================================================================================
    public class SenderViewHolder extends RecyclerView.ViewHolder{
        ItemSendBinding binding;
        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSendBinding.bind(itemView);
        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder{
        ItemReceiveBinding binding;
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemReceiveBinding.bind(itemView);
        }
    }

}
