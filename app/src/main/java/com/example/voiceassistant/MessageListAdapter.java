package com.example.voiceassistant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter {
    public List<Message> messageList = new ArrayList<>();
    private static final int ASSISTANT_TYPE = 0;
    private static final int USER_TYPE = 1;


    /**
     * Вовзращает сообщение пользователя или ассистента.     *
     * @param parent   внутри этого элемента будет отображаться RecyclerView.
     * @param viewType тип представления(элементы списка).
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == USER_TYPE) { // user
            view = LayoutInflater
                    .from(parent.getContext())
                    .inflate(
                            R.layout.user_message,
                            parent,
                            false);
        } else { // assistant
            view = LayoutInflater
                    .from(parent.getContext())
                    .inflate(
                            R.layout.assistant_message,
                            parent,
                            false);
        }

        return new MessageViewHolder(view);
    }

    /**
     * Вызывается, когда появляется новое сообщение.     *
     * @param holder   номер сообщения.
     * @param position номер сообщения.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((MessageViewHolder) holder).bind(messageList.get(position));
    }

    /**
     * Размер сообщений.
     */
    @Override
    public int getItemCount() {
        return messageList.size();
    }

    /**
     * Тип сообщения в зависимости от номера индекса.
     */
    public int getItemViewType(int index) {
        Message message = messageList.get(index);
        if (message.isSend) {
            return USER_TYPE;
        }
        else {
            return ASSISTANT_TYPE;
        }
    }
}
