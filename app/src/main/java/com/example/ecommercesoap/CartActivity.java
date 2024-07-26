package com.example.ecommercesoap;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private ApiService apiService;
    private Button clearCartButton;
    private TextView totalTextView;
    private Button payButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerView);
        clearCartButton = findViewById(R.id.clearCartButton);
        totalTextView = findViewById(R.id.totalTextView);
        payButton = findViewById(R.id.payButton);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2/ecommerce-api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadCartItems();

        clearCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearCart(1); // Pasar el userId (en este caso, 1 como ejemplo)
            }
        });

        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CartActivity.this, PaymentSelectionActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadCartItems() {
        Call<List<CartItem>> call = apiService.getCart(1);
        call.enqueue(new Callback<List<CartItem>>() {
            @Override
            public void onResponse(Call<List<CartItem>> call, Response<List<CartItem>> response) {
                if (response.isSuccessful()) {
                    List<CartItem> cartItems = response.body();
                    if (cartItems != null) {
                        adapter = new CartAdapter(cartItems);
                        recyclerView.setAdapter(adapter);
                        calculateTotal(cartItems);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<CartItem>> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Failed to load cart items: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateTotal(List<CartItem> cartItems) {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        totalTextView.setText("Total: $" + total);
    }

    private void clearCart(int userId) {
        Call<ApiResponse> call = apiService.clearCart(userId);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse != null && apiResponse.getStatus().equals("success")) {
                        Toast.makeText(CartActivity.this, "Cart cleared", Toast.LENGTH_SHORT).show();
                        loadCartItems();
                    } else {
                        Toast.makeText(CartActivity.this, "Failed to clear cart", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CartActivity.this, "Response error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Failed to clear cart: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

        private List<CartItem> cartItems;

        CartAdapter(List<CartItem> cartItems) {
            this.cartItems = cartItems;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_cart, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            CartItem cartItem = cartItems.get(position);
            holder.productNameTextView.setText(cartItem.getName());
            holder.productPriceTextView.setText("$" + cartItem.getPrice());
            holder.productQuantityTextView.setText("Quantity: " + cartItem.getQuantity());
            holder.subtotalTextView.setText("Subtotal: $" + (cartItem.getPrice() * cartItem.getQuantity()));
            Picasso.get().load(cartItem.getImage()).into(holder.productImageView); // Cargar la imagen del producto
        }

        @Override
        public int getItemCount() {
            return cartItems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView productNameTextView;
            TextView productPriceTextView;
            TextView productQuantityTextView;
            TextView subtotalTextView;
            ImageView productImageView; // Vista para la imagen del producto

            ViewHolder(View itemView) {
                super(itemView);
                productNameTextView = itemView.findViewById(R.id.productNameTextView);
                productPriceTextView = itemView.findViewById(R.id.productPriceTextView);
                productQuantityTextView = itemView.findViewById(R.id.productQuantityTextView);
                subtotalTextView = itemView.findViewById(R.id.subtotalTextView);
                productImageView = itemView.findViewById(R.id.productImageView); // Asignar la vista de imagen
            }
        }
    }
}