package com.example.smartdocs;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.widget.AdapterView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        // SUPPRIME car l'id 'main' n'existe plus et ce code n'est plus utile avec FrameLayout
        // ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
        //     Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
        //     v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
        //     return insets;
        // });

        // Session
        SessionManager sessionManager = new SessionManager(this);
        String username = sessionManager.getUsername();
        if (username == null) username = "Utilisateur";

        // SUPPRIME : plus de Toolbar dans le layout harmonisé
        // MaterialToolbar toolbar = findViewById(R.id.toolbar);
        // toolbar.setNavigationOnClickListener(v -> {
        //     sessionManager.logoutUser();
        //     Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        //     intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //     startActivity(intent);
        //     finish();
        // });
        // Ajoute la gestion du bouton Déconnexion dans le formulaire :
        ImageButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        tvWelcome.setText("Bienvenue (" + username + ") 👋🏻");

        // Spinners
        Spinner spinnerPeriode = findViewById(R.id.spinnerPeriode);
        Spinner spinnerTypeDoc = findViewById(R.id.spinnerTypeDoc);
        Spinner spinnerEntreprise = findViewById(R.id.spinnerEntreprise);
        // Période 2018-2025
        List<String> periodes = new ArrayList<>();
        for (int i = 2025; i >= 2018; i--) periodes.add(String.valueOf(i));
        ArrayAdapter<String> periodeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, periodes);
        periodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPeriode.setAdapter(periodeAdapter);

        // --- API pour TypeDoc et Entreprise ---
        RequestQueue queue = Volley.newRequestQueue(this);
        List<String> typeDocLabels = new ArrayList<>();
        List<Integer> typeDocIds = new ArrayList<>();
        ArrayAdapter<String> typeDocAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, typeDocLabels);
        typeDocAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTypeDoc.setAdapter(typeDocAdapter);
        queue.add(new JsonArrayRequest(Request.Method.GET,
                "https://ged.smart4apps.com/api/ged/GetClassesTypes?token=kHilQZdorkS4KrHXVhm/Uw==",
                null,
                response -> {
                    typeDocLabels.clear();
                    typeDocIds.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            typeDocLabels.add(obj.getString("lb"));
                            typeDocIds.add(obj.getInt("id"));
                        } catch (JSONException e) { e.printStackTrace(); }
                    }
                    typeDocAdapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(this, "Erreur chargement types doc", Toast.LENGTH_SHORT).show()
        ));
        List<String> entrepriseLabels = new ArrayList<>();
        List<Integer> entrepriseIds = new ArrayList<>();
        ArrayAdapter<String> entrepriseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, entrepriseLabels);
        entrepriseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEntreprise.setAdapter(entrepriseAdapter);
        queue.add(new JsonArrayRequest(Request.Method.GET,
                "https://ged.smart4apps.com/api/ged/GetEntreprises?token=bptvkexEkUOP8t2DnuruDg==",
                null,
                response -> {
                    entrepriseLabels.clear();
                    entrepriseIds.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            entrepriseLabels.add(obj.getString("lb"));
                            entrepriseIds.add(obj.getInt("id"));
                        } catch (JSONException e) { e.printStackTrace(); }
                    }
                    entrepriseAdapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(this, "Erreur chargement entreprises", Toast.LENGTH_SHORT).show()
        ));

        // Dates
        TextView tvDateDebut = findViewById(R.id.tvDateDebut);
        TextView tvDateFin = findViewById(R.id.tvDateFin);
        tvDateDebut.setOnClickListener(v -> showDatePicker(tvDateDebut));
        tvDateFin.setOnClickListener(v -> showDatePicker(tvDateFin));

        // Textbox
        EditText etTextSearch = findViewById(R.id.etTextSearch);

        // Boutons
        Button btnEffacer = findViewById(R.id.btnEffacer);
        Button btnRechercher = findViewById(R.id.btnRechercher);
        btnEffacer.setOnClickListener(v -> {
            tvDateDebut.setText("Sélectionner");
            tvDateFin.setText("Sélectionner");
            spinnerPeriode.setSelection(0);
            spinnerTypeDoc.setSelection(0);
            spinnerEntreprise.setSelection(0);
            etTextSearch.setText("");
        });
        btnRechercher.setOnClickListener(v -> {
            String du = tvDateDebut.getText().toString().equals("Sélectionner") ? null : tvDateDebut.getText().toString();
            String au = tvDateFin.getText().toString().equals("Sélectionner") ? null : tvDateFin.getText().toString();
            String periode = spinnerPeriode.getSelectedItem().toString();
            int idType = spinnerTypeDoc.getSelectedItemPosition() >= 0 && spinnerTypeDoc.getSelectedItemPosition() < typeDocIds.size() ? typeDocIds.get(spinnerTypeDoc.getSelectedItemPosition()) : 0;
            int idClient = spinnerEntreprise.getSelectedItemPosition() >= 0 && spinnerEntreprise.getSelectedItemPosition() < entrepriseIds.size() ? entrepriseIds.get(spinnerEntreprise.getSelectedItemPosition()) : 0;
            String textSearch = etTextSearch.getText().toString().trim();
            String url = "https://ged.smart4apps.com/api/ged/GetDocuments?";
            // Cas 1 : les deux dates sont vides => recherche par période
            if ((du == null || du.isEmpty()) && (au == null || au.isEmpty())) {
                url += "periode=" + periode;
            }
            // Cas 2 : les deux dates sont remplies => recherche par dates
            else if (du != null && !du.isEmpty() && au != null && !au.isEmpty()) {
                url += "du=" + du + "&au=" + au;
            } else {
                Toast.makeText(this, "Veuillez remplir soit les deux dates, soit aucune date pour utiliser la période.", Toast.LENGTH_LONG).show();
                return;
            }
            url += "&idType=" + idType + "&idClient=" + idClient;
            if (!textSearch.isEmpty()) url += "&textSearch=" + textSearch;
            queue.add(new JsonArrayRequest(Request.Method.GET, url, null,
                    response -> {
                        ArrayList<String> images = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                String name = response.getString(i);
                                images.add(name);
                            } catch (JSONException e) { e.printStackTrace(); }
                        }
                        Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                        intent.putStringArrayListExtra("images", images);
                        startActivity(intent);
                    },
                    error -> Toast.makeText(this, "Erreur recherche documents", Toast.LENGTH_SHORT).show()
            ));
        });
    }

    private void showDatePicker(TextView target) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dpd = new DatePickerDialog(this, (view, y, m, d) -> {
            String date = String.format("%04d-%02d-%02d", y, m+1, d);
            target.setText(date);
        }, year, month, day);
        dpd.show();
    }

    // Adapter amélioré pour afficher les images joliment
    public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
        private List<String> imageNames;
        public ImageAdapter(List<String> imageNames) { this.imageNames = imageNames; }
        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
            return new ImageViewHolder(view);
        }
        @Override
        public void onBindViewHolder(ImageViewHolder holder, int position) {
            String name = imageNames.get(position);
            String url = "https://ged.smart4apps.com/Images/Temp/thumbs/" + name;
            Glide.with(holder.imageView.getContext()).load(url).placeholder(R.drawable.ic_launcher_background).into(holder.imageView);
            // SUPPRIME : plus de nom à afficher
            // holder.tvImageName.setText(name);
        }
        @Override
        public int getItemCount() { return imageNames.size(); }
        public class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            // SUPPRIME : plus de TextView tvImageName;
            public ImageViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
                // SUPPRIME : plus de tvImageName
            }
        }
    }
}