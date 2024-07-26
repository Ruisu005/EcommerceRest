package com.example.ecommercesoap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProductDetailsActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.0.2.2/ecommerce-api/";
    private static final String TAG = "ProductDetailsActivity";
    private ApiService apiService;
    private int productId;
    private int quantity = 1;

    private ImageView productImageView;
    private TextView productNameTextView;
    private TextView productCategoryTextView;
    private TextView productPriceTextView;
    private TextView productDescriptionTextView;
    private TextView productQuantityTextView;
    private TextView cartItemCount;
    private Button addToCartButton;
    private Button increaseQuantityButton;
    private Button decreaseQuantityButton;
    private ImageView cartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        productId = getIntent().getIntExtra("product_id", 0);

        productImageView = findViewById(R.id.productImageView);
        productNameTextView = findViewById(R.id.productNameTextView);
        productCategoryTextView = findViewById(R.id.productCategoryTextView);
        productPriceTextView = findViewById(R.id.productPriceTextView);
        productDescriptionTextView = findViewById(R.id.productDescriptionTextView);
        productQuantityTextView = findViewById(R.id.productQuantityTextView);
        cartItemCount = findViewById(R.id.cartItemCount);
        addToCartButton = findViewById(R.id.addToCartButton);
        increaseQuantityButton = findViewById(R.id.increaseQuantityButton);
        decreaseQuantityButton = findViewById(R.id.decreaseQuantityButton);
        cartButton = findViewById(R.id.cartButton);


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        getProductDetails(productId);
        updateCartItemCount();

        increaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantity++;
                productQuantityTextView.setText(String.valueOf(quantity));
            }
        });

        decreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity > 1) {
                    quantity--;
                    productQuantityTextView.setText(String.valueOf(quantity));
                }
            }
        });

        addToCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Adding to cart: userId=1, productId=" + productId + ", quantity=" + quantity);
                addToCart(1, productId, quantity); // Pasar el userId (en este caso, 1 como ejemplo)
            }
        });

        cartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProductDetailsActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });

        apiService = retrofit.create(ApiService.class);

        cartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProductDetailsActivity.this, CartActivity.class);
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

    private void getProductDetails(int productId) {
        Call<Product> call = apiService.getProductDetails(productId);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful()) {
                    Product product = response.body();
                    if (product != null) {
                        Picasso.get().load(product.getImage()).into(productImageView);
                        productNameTextView.setText(product.getName());
                        productCategoryTextView.setText(product.getCategoryName());
                        productPriceTextView.setText("$" + product.getPrice());
                        productDescriptionTextView.setText(product.getDescription());
                    } else {
                        Toast.makeText(ProductDetailsActivity.this, "Failed to load product details", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProductDetailsActivity.this, "Response error: " + response.message(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Response error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                Log.e(TAG, "Failure: " + t.getMessage());
                Toast.makeText(ProductDetailsActivity.this, "Failed to load product details: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addToCart(int userId, int productId, int quantity) {
        Call<ApiResponse> call = apiService.addToCart(userId, productId, quantity);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                Log.d(TAG, "Response: " + response);
                if (response.isSuccessful()) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse != null && apiResponse.getStatus().equals("success")) {
                        Toast.makeText(ProductDetailsActivity.this, "Product added to cart", Toast.LENGTH_SHORT).show();
                        updateCartItemCount();
                    } else {
                        String errorMessage = apiResponse != null ? apiResponse.getMessage() : "Unknown error";
                        Toast.makeText(ProductDetailsActivity.this, "Failed to add product to cart: " + errorMessage, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failed to add product to cart: " + errorMessage);
                    }
                } else {
                    Toast.makeText(ProductDetailsActivity.this, "Response error: " + response.message(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Response error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e(TAG, "Failure: " + t.getMessage());
                Toast.makeText(ProductDetailsActivity.this, "Failed to add product to cart: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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