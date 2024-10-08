package com.example.ecommercesoap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
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

public class ProductsActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://www.vendemoscomputadoras.com/ecommerce-api/";
    private static final String TAG = "ProductsActivity";
    private ApiService apiService;
    private RecyclerView recyclerView;
    private ProductsAdapter adapter;
    private SearchView searchView;
    private int categoryId;
    private TextView cartItemCount;
    private ImageView cartButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_products);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        cartItemCount = findViewById(R.id.cartItemCount);
        cartButton = findViewById(R.id.cartButton);

        categoryId = getIntent().getIntExtra("category_id", 0);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchView = findViewById(R.id.searchView);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        getProducts(categoryId);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchProducts(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    getProducts(categoryId);
                }
                return false;
            }
        });

        apiService = retrofit.create(ApiService.class);

        cartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProductsActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });

        updateCartItemCount();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartItemCount();
    }

    private void getProducts(int categoryId) {
        Call<List<Product>> call = apiService.getProducts(categoryId);
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful()) {
                    List<Product> products = response.body();
                    if (products != null) {
                        adapter = new ProductsAdapter(products);
                        recyclerView.setAdapter(adapter);
                    } else {
                        Toast.makeText(ProductsActivity.this, "Failed to load products: empty response", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProductsActivity.this, "Response error: " + response.message(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Response error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e(TAG, "Failure: " + t.getMessage());
                Toast.makeText(ProductsActivity.this, "Failed to load products: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchProducts(String query) {
        Call<List<Product>> call = apiService.searchProducts(query);
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful()) {
                    List<Product> products = response.body();
                    if (products != null) {
                        adapter = new ProductsAdapter(products);
                        recyclerView.setAdapter(adapter);
                    } else {
                        Toast.makeText(ProductsActivity.this, "No products found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProductsActivity.this, "Response error: " + response.message(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Response error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e(TAG, "Failure: " + t.getMessage());
                Toast.makeText(ProductsActivity.this, "Failed to search products: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ViewHolder> {

        private List<Product> products;

        ProductsAdapter(List<Product> products) {
            this.products = products;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_product, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Product product = products.get(position);
            holder.nameTextView.setText(product.getName());
            holder.priceTextView.setText("$" + product.getPrice());
            Picasso.get().load(product.getImage()).into(holder.imageView);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ProductsActivity.this, ProductDetailsActivity.class);
                    intent.putExtra("product_id", product.getId());
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return products.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameTextView;
            TextView priceTextView;
            ImageView imageView;

            ViewHolder(View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.nameTextView);
                priceTextView = itemView.findViewById(R.id.priceTextView);
                imageView = itemView.findViewById(R.id.imageView);
            }
        }
    }

    private void updateCartItemCount() {
        Call<List<CartItem>> call = apiService.getCart(1); // Pasar el userId (en este caso, 1 como ejemplo)
        call.enqueue(new Callback<List<CartItem>>() {
            @Override
            public void onResponse(Call<List<CartItem>> call, Response<List<CartItem>> response) {
                if (response.isSuccessful()) {
                    List<CartItem> cartItems = response.body();
                    if (cartItems != null) {
                        cartItemCount.setText(String.valueOf(cartItems.size()));
                    } else {
                        cartItemCount.setText("0");
                    }
                } else {
                    cartItemCount.setText("0");
                }
            }

            @Override
            public void onFailure(Call<List<CartItem>> call, Throwable t) {
                cartItemCount.setText("0");
            }
        });
    }
}