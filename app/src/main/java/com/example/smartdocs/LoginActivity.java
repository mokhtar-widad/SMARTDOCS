package com.example.smartdocs;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText usernameEditText;
    private TextInputEditText passwordEditText;
    private MaterialButton loginButton;
    private ProgressBar loadingProgressBar;
    private SessionManager sessionManager;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(getApplicationContext());
        requestQueue = Volley.newRequestQueue(this);

        usernameEditText = findViewById(R.id.username_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);
        loadingProgressBar = findViewById(R.id.loading_progress_bar);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                    return;
                }

                loginUser(username, password);
            }
        });
    }

    private void loginUser(String username, String password) {
        loadingProgressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        String url = "https://ged.smart4apps.com/Account/Authenticate?user=" + username + "&password=" + password;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loadingProgressBar.setVisibility(View.GONE);
                        loginButton.setEnabled(true);
                        try {
                            String token = response.getString("token");
                            String returnedUsername = response.getString("username");

                            sessionManager.createLoginSession(returnedUsername, token);

                            Toast.makeText(LoginActivity.this, "Connexion réussie", Toast.LENGTH_SHORT).show();
                            // Rediriger vers l'activité principale
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(LoginActivity.this, "Erreur de parsing: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingProgressBar.setVisibility(View.GONE);
                        loginButton.setEnabled(true);
                        Log.e("LoginActivity", "Login Error: " + error.toString());
                        Toast.makeText(LoginActivity.this, "Erreur de connexion: Identifiants incorrects ou problème de réseau", Toast.LENGTH_LONG).show();
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }
}
