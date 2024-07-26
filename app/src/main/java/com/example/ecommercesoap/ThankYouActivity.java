package com.example.ecommercesoap;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ThankYouActivity extends AppCompatActivity {

    private TextView thankYouTextView;
    private static final int REDIRECT_DELAY = 3000; // 3 segundos de delay

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_thank_you);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        thankYouTextView = findViewById(R.id.thankYouTextView);

        // Animación de fade in y fade out
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(1000);
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setStartOffset(2000);
        fadeOut.setDuration(1000);
        thankYouTextView.startAnimation(fadeIn);
        thankYouTextView.startAnimation(fadeOut);

        // Redirigir a CategoriesActivity después de 3 segundos
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(ThankYouActivity.this, CategoriesActivity.class);
                startActivity(intent);
                finish(); // Cerrar la actividad actual
            }
        }, REDIRECT_DELAY);
    }
}