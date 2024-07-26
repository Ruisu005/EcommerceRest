package com.example.ecommercesoap;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @POST("register.php")
    Call<ApiResponse> register(@Body User user);

    @POST("login.php")
    Call<ApiResponse> login(@Body User user);

    @GET("categories.php")
    Call<List<Category>> getCategories();

    @GET("getProducts.php")
    Call<List<Product>> getProducts(@Query("category_id") int categoryId);

    @GET("searchProducts.php")
    Call<List<Product>> searchProducts(@Query("query") String query);

    @GET("getProductDetails.php")
    Call<Product> getProductDetails(@Query("product_id") int productId);

    @FormUrlEncoded
    @POST("addToCart.php")
    Call<ApiResponse> addToCart(@Field("user_id") int userId, @Field("product_id") int productId, @Field("quantity") int quantity);

    @GET("getCart.php")
    Call<List<CartItem>> getCart(@Query("user_id") int userId);

    @FormUrlEncoded
    @POST("clearCart.php")
    Call<ApiResponse> clearCart(@Field("user_id") int userId);

    @FormUrlEncoded
    @POST("updateCartQuantity.php")
    Call<ApiResponse> updateCartQuantity(@Field("product_id") int productId, @Field("user_id") int userId, @Field("quantity") int quantity);

    @FormUrlEncoded
    @POST("recordSale.php")
    Call<ApiResponse> recordSale(
            @Field("user_id") int userId,
            @Field("first_name") String firstName,
            @Field("last_name") String lastName,
            @Field("address") String address,
            @Field("payment_method") String paymentMethod,
            @Field("card_number") String cardNumber,
            @Field("expiry_date") String expiryDate,
            @Field("cvv") String cvv
    );
}
