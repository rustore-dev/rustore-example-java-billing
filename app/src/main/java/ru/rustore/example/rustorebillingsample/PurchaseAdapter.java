package ru.rustore.example.rustorebillingsample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.rustore.sdk.billingclient.model.purchase.Purchase;

public class PurchaseAdapter extends RecyclerView.Adapter<PurchaseAdapter.ViewHolder> {

    private final List<Purchase> mPurchases;

    public PurchaseAdapter(List<Purchase> purchases) {
        mPurchases = purchases;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView purchaseId;
        public TextView purchaseState;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            purchaseId = (TextView) itemView.findViewById(R.id.purchaseId);
            purchaseState = (TextView) itemView.findViewById(R.id.purchaseState);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View purchase = inflater.inflate(R.layout.item_purchase, parent, false);

        return new ViewHolder(purchase);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Purchase purchase = mPurchases.get(position);

        TextView purchaseId = holder.purchaseId;
        TextView purchaseState = holder.purchaseState;

        purchaseId.setText(purchase.getPurchaseId());
        purchaseState.setText(String.valueOf(purchase.getPurchaseState()));
    }


    @Override
    public int getItemCount() {
        return mPurchases.size();
    }


}
