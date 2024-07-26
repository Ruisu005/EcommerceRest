package com.example.ecommercesoap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PaymentSelectionActivity extends AppCompatActivity {

    private RadioGroup paymentMethodRadioGroup;
    private RadioButton radioPayPal;
    private RadioButton radioCreditCard;
    private Button confirmPaymentButton;
    private ApiService apiService;

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText addressEditText;
    private EditText cardNumberEditText;
    private EditText expiryDateEditText;
    private EditText cvvEditText;

    private int userId = 1; // Ejemplo de userId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_selection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        paymentMethodRadioGroup = findViewById(R.id.paymentMethodRadioGroup);
        radioPayPal = findViewById(R.id.radioPayPal);
        radioCreditCard = findViewById(R.id.radioCreditCard);
        confirmPaymentButton = findViewById(R.id.confirmPaymentButton);

        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        addressEditText = findViewById(R.id.addressEditText);
        cardNumberEditText = findViewById(R.id.cardNumberEditText);
        expiryDateEditText = findViewById(R.id.expiryDateEditText);
        cvvEditText = findViewById(R.id.cvvEditText);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2/ecommerce-api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        confirmPaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = firstNameEditText.getText().toString();
                String lastName = lastNameEditText.getText().toString();
                String address = addressEditText.getText().toString();
                String cardNumber = cardNumberEditText.getText().toString();
                String expiryDate = expiryDateEditText.getText().toString();
                String cvv = cvvEditText.getText().toString();

                int selectedId = paymentMethodRadioGroup.getCheckedRadioButtonId();
                String paymentMethod;
                if (selectedId == radioPayPal.getId()) {
                    paymentMethod = "PayPal";
                    // No necesitamos los datos de la tarjeta para PayPal
                    cardNumber = expiryDate = cvv = "";
                } else {
                    paymentMethod = "Credit Card";
                    // Validaciones adicionales pueden añadirse aquí
                }

                // Registrar la venta
                recordSale(userId, firstName, lastName, address, paymentMethod, cardNumber, expiryDate, cvv);
            }
        });
    }

    private void recordSale(int userId, String firstName, String lastName, String address, String paymentMethod, String cardNumber, String expiryDate, String cvv) {
        Call<ApiResponse> call = apiService.recordSale(userId, firstName, lastName, address, paymentMethod, cardNumber, expiryDate, cvv);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse != null && apiResponse.getStatus().equals("success")) {
                        Toast.makeText(PaymentSelectionActivity.this, "Venta registrada", Toast.LENGTH_SHORT).show();
                        clearCart(userId);
                    } else {
                        Toast.makeText(PaymentSelectionActivity.this, "Error al registrar la venta", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PaymentSelectionActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(PaymentSelectionActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearCart(int userId) {
        Call<ApiResponse> call = apiService.clearCart(userId);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse != null && apiResponse.getStatus().equals("success")) {
                        // Carrito limpio, redirigir a la pantalla de agradecimiento
                        Intent intent = new Intent(PaymentSelectionActivity.this, ThankYouActivity.class);
                        startActivity(intent);
                        finish(); // Cierra la actividad actual
                    } else {
                        Toast.makeText(PaymentSelectionActivity.this, "Error al limpiar el carrito", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PaymentSelectionActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(PaymentSelectionActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}