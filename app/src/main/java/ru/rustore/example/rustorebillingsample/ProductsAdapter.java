package ru.rustore.example.rustorebillingsample;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.List;

import ru.rustore.sdk.billingclient.model.product.Product;
import ru.rustore.sdk.billingclient.model.purchase.Purchase;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ViewHolder> {
    private final List<Product> mProducts;

    public ProductsAdapter(List<Product> products) {
        mProducts = products;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView productIdTextView;
        public TextView productNameTextView;
        public TextView productPriceTextView;

        public Button buyProductButton;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.productNameTextView = itemView.findViewById(R.id.productName);
            productIdTextView = itemView.findViewById(R.id.productId);
            productPriceTextView = itemView.findViewById(R.id.productPrice);
            buyProductButton = itemView.findViewById(R.id.buyProduct);
        }
    }

    @NonNull
    @Override
    public ProductsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View productView = inflater.inflate(R.layout.item_product, parent, false);

        return new ViewHolder(productView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductsAdapter.ViewHolder holder, int position) {
        Product product = mProducts.get(position);

        TextView productName = holder.productNameTextView;
        TextView productId = holder.productIdTextView;
        TextView priceText = holder.productPriceTextView;

        productName.setText(product.getTitle());
        productId.setText(product.getProductId());
        priceText.setText(String.valueOf(product.getPrice()));


        Button button = holder.buyProductButton;
        button.setText("Купить");
        button.setEnabled(true);
    }

    @Override
    public int getItemCount() {
        return mProducts.size();
    }
}
