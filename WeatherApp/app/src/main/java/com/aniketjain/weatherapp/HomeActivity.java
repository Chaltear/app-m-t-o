package com.aniketjain.weatherapp;
// Importation des bibliothèques nécessaires pour le fonctionnement de l'activité.
import static com.aniketjain.weatherapp.location.CityFinder.getCityNameUsingNetwork;
import static com.aniketjain.weatherapp.location.CityFinder.setLongitudeLatitude;
import static com.aniketjain.weatherapp.network.InternetConnectivity.isInternetConnected;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.aniketjain.weatherapp.adapter.DaysAdapter;
import com.aniketjain.weatherapp.connection.Parametre;
import com.aniketjain.weatherapp.databinding.ActivityHomeBinding;
import com.aniketjain.weatherapp.location.LocationCord;
import com.aniketjain.weatherapp.toast.Toaster;
import com.aniketjain.weatherapp.update.UpdateUI;
import com.aniketjain.weatherapp.url.URL;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

// Définition de la classe HomeActivity qui étend AppCompatActivity.
public class HomeActivity extends AppCompatActivity {

    // Déclaration des constantes et variables utilisées dans l'activité.
    private final int WEATHER_FORECAST_APP_UPDATE_REQ_CODE = 101;   /// Code de requête pour la mise à jour de l'application.
    private static final int PERMISSION_CODE = 1;                   // Code de permission pour l'accès à la localisation de l'utilisateur.
    private String name, updated_at, description, temperature, min_temperature, max_temperature, pressure, wind_speed, humidity;
    private int condition;

    private RelativeLayout layout1;
    private ImageView engrenage;// Icône des paramètres.
    private long update_time, sunset, sunrise;// Variables pour stocker les données météorologiques.
    private String city = "";// Variable pour stocker le nom de la ville.
    private final int REQUEST_CODE_EXTRA_INPUT = 101;// Code de requête pour l'entrée vocale.
    private ActivityHomeBinding binding;// Utilisation de ViewBinding pour accéder aux vues.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialisation du ViewBinding.
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Appel des méthodes pour configurer l'UI et les fonctionnalités.
        // set navigation bar color
        setNavigationBarColor();

        //check for new app update
        checkUpdate();

        // set refresh color schemes
        setRefreshLayoutColor();

        // when user do search and refresh
        listeners();

        // getting data using internet connection
        getDataUsingNetwork();

        // Configuration de l'icône des paramètres pour naviguer vers l'activité Parametre.
        engrenage=findViewById(R.id.parametre);
        engrenage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, Parametre.class);
                startActivity(intent);
            }
        });
         layout1 = findViewById(R.id.layoutmain);

        // Application du thème en fonction du choix de l'utilisateur stocké dans SharedPreferences.
        SharedPreferences sharedPreferences = getSharedPreferences("AppSettingsPrefs", MODE_PRIVATE);
        boolean isThemeDark = sharedPreferences.getBoolean("ThemeChoice", false);

        if(isThemeDark) {
            // Appliquer la couleur de fond pour le thème sombre
            layout1.setBackgroundColor(getResources().getColor(R.color.grey));
            binding.layout.mainLayout.setBackgroundColor(getResources().getColor(R.color.grey));

        } else {
            // Appliquer la couleur de fond par défaut
            layout1.setBackgroundColor(getResources().getColor(R.color.mainBGColor));
            binding.layout.mainLayout.setBackgroundColor(getResources().getColor(R.color.mainBGColor));
        }

    }

    // Traitement du résultat après une requête d'entrée vocale.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EXTRA_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> arrayList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                binding.layout.cityEt.setText(Objects.requireNonNull(arrayList).get(0).toUpperCase());
                searchCity(binding.layout.cityEt.getText().toString());
            }
        }
    }


    // Configuration de la couleur de la barre de navigation.
    private void setNavigationBarColor() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.navBarColor));
        }
    }

    // Configuration du RecyclerView pour afficher les prévisions météorologiques des jours à venir.
    private void setUpDaysRecyclerView() {
        DaysAdapter daysAdapter = new DaysAdapter(this);
        binding.dayRv.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        binding.dayRv.setAdapter(daysAdapter);
    }

    // Configuration des écouteurs d'événements pour les interactions de l'utilisateur.
    // Plusieurs écouteurs sont configurés pour traiter les entrées de l'utilisateur, comme les recherches et les interactions avec le clavier virtuel.
    @SuppressLint("ClickableViewAccessibility")
    private void listeners() {
        binding.layout.mainLayout.setOnTouchListener((view, motionEvent) -> {
            hideKeyboard(view);
            return false;
        });
        binding.layout.searchBarIv.setOnClickListener(view -> searchCity(binding.layout.cityEt.getText().toString()));
        binding.layout.searchBarIv.setOnTouchListener((view, motionEvent) -> {
            hideKeyboard(view);
            return false;
        });
        binding.layout.cityEt.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_GO) {
                searchCity(binding.layout.cityEt.getText().toString());
                hideKeyboard(textView);
                return true;
            }
            return false;
        });
        binding.layout.cityEt.setOnFocusChangeListener((view, b) -> {
            if (!b) {
                hideKeyboard(view);
            }
        });
        binding.mainRefreshLayout.setOnRefreshListener(() -> {
            checkConnection();
            Log.i("refresh", "Refresh Done.");
            binding.mainRefreshLayout.setRefreshing(false);  //for the next time
        });
        //Mic Search
        binding.layout.micSearchId.setOnClickListener(view -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, REQUEST_CODE_EXTRA_INPUT);
            try {
                //it was deprecated but still work
                startActivityForResult(intent, REQUEST_CODE_EXTRA_INPUT);
            } catch (Exception e) {
                Log.d("Error Voice", "Mic Error:  " + e);
            }
        });
    }

    // Configuration des couleurs pour le "SwipeRefreshLayout".
    // Configuration des couleurs pour l'animation de rafraîchissement.
    private void setRefreshLayoutColor() {
        binding.mainRefreshLayout.setProgressBackgroundColorSchemeColor(
                getResources().getColor(R.color.textColor)
        );
        binding.mainRefreshLayout.setColorSchemeColors(
                getResources().getColor(R.color.navBarColor)
        );
    }

    // Méthode pour rechercher une ville et obtenir des données météorologiques.
    private void searchCity(String cityName) {
        if (cityName == null || cityName.isEmpty()) {
            Toaster.errorToast(this, "Please enter the city name");
        } else {
            setLatitudeLongitudeUsingCity(cityName);
        }
    }

    // Méthode pour obtenir des données météorologiques en utilisant la localisation de l'utilisateur.
    private void getDataUsingNetwork() {
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        //check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
        } else {
            client.getLastLocation().addOnSuccessListener(location -> {
                setLongitudeLatitude(location);
                city = getCityNameUsingNetwork(this, location);
                getTodayWeatherInfo(city);
            });
        }
    }


    // Méthode pour définir la latitude et la longitude en utilisant le nom de la ville.
    private void setLatitudeLongitudeUsingCity(String cityName) {
        URL.setCity_url(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(HomeActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL.getCity_url(), null, response -> {
            try {
                LocationCord.lat = response.getJSONObject("coord").getString("lat");
                LocationCord.lon = response.getJSONObject("coord").getString("lon");
                getTodayWeatherInfo(cityName);
                // After the successfully city search the cityEt(editText) is Empty.
                binding.layout.cityEt.setText("");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Toaster.errorToast(this, "Please enter the correct city name"));
        requestQueue.add(jsonObjectRequest);
    }

    // Méthode pour mettre à jour l'interface utilisateur avec les données météorologiques obtenues.
    @SuppressLint("DefaultLocale")
    private void getTodayWeatherInfo(String name) {

        URL url = new URL();
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url.getLink(), null, response -> {
            try {
                this.name = name;
                update_time = response.getJSONObject("current").getLong("dt");
                updated_at = new SimpleDateFormat("EEEE hh:mm a", Locale.ENGLISH).format(new Date(update_time * 1000));

                condition = response.getJSONArray("daily").getJSONObject(0).getJSONArray("weather").getJSONObject(0).getInt("id");
                sunrise = response.getJSONArray("daily").getJSONObject(0).getLong("sunrise");
                sunset = response.getJSONArray("daily").getJSONObject(0).getLong("sunset");
                description = response.getJSONObject("current").getJSONArray("weather").getJSONObject(0).getString("main");

                temperature = String.valueOf(Math.round(response.getJSONObject("current").getDouble("temp") - 273.15));
                min_temperature = String.format("%.0f", response.getJSONArray("daily").getJSONObject(0).getJSONObject("temp").getDouble("min") - 273.15);
                max_temperature = String.format("%.0f", response.getJSONArray("daily").getJSONObject(0).getJSONObject("temp").getDouble("max") - 273.15);
                pressure = response.getJSONArray("daily").getJSONObject(0).getString("pressure");
                wind_speed = response.getJSONArray("daily").getJSONObject(0).getString("wind_speed");
                humidity = response.getJSONArray("daily").getJSONObject(0).getString("humidity");

                updateUI();
                hideProgressBar();
                setUpDaysRecyclerView();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, null);
        requestQueue.add(jsonObjectRequest);
        Log.i("json_req", "Day 0");
    }

    // Met à jour l'interface utilisateur avec les données météorologiques.
    @SuppressLint("SetTextI18n")
    private void updateUI() {
        binding.layout.nameTv.setText(name);
        updated_at = translate(updated_at);
        binding.layout.updatedAtTv.setText(updated_at);
        binding.layout.conditionIv.setImageResource(
                getResources().getIdentifier(
                        UpdateUI.getIconID(condition, update_time, sunrise, sunset),
                        "drawable",
                        getPackageName()
                ));
        binding.layout.conditionDescTv.setText(description);
        binding.layout.tempTv.setText(temperature + "°C");
        binding.layout.minTempTv.setText(min_temperature + "°C");
        binding.layout.maxTempTv.setText(max_temperature + "°C");
        binding.layout.pressureTv.setText(pressure + " mb");
        binding.layout.windTv.setText(wind_speed + " km/h");
        binding.layout.humidityTv.setText(humidity + "%");
    }

    // Convertit le jour de la semaine de l'anglais vers la langue locale.
    private String translate(String dayToTranslate) {
        String[] dayToTranslateSplit = dayToTranslate.split(" ");
        dayToTranslateSplit[0] = UpdateUI.TranslateDay(dayToTranslateSplit[0].trim(), getApplicationContext());
        return dayToTranslateSplit[0].concat(" " + dayToTranslateSplit[1]);
    }

    // Cache la barre de progression et affiche le contenu principal de l'interface utilisateur.
    private void hideProgressBar() {
        binding.progress.setVisibility(View.GONE);
        binding.layout.mainLayout.setVisibility(View.VISIBLE);
    }

    // Cache le contenu principal de l'interface utilisateur et affiche la barre de progression.
    private void hideMainLayout() {
        binding.progress.setVisibility(View.VISIBLE);
        binding.layout.mainLayout.setVisibility(View.GONE);
    }

    // Cache le clavier virtuel.
    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // Vérifie si l'appareil est connecté à Internet.
    private void checkConnection() {
        if (!isInternetConnected(this)) {
            hideMainLayout();
            Toaster.errorToast(this, "Please check your internet connection");
        } else {
            hideProgressBar();
            getDataUsingNetwork();
        }
    }

    // Gère le résultat des demandes de permission.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toaster.successToast(this, "Permission Granted");
                getDataUsingNetwork();
            } else {
                Toaster.errorToast(this, "Permission Denied");
                finish();
            }
        }
    }

    // Vérifie à chaque reprise de l'activité si l'appareil est connecté à Internet.
    @Override
    protected void onResume() {
        super.onResume();
        checkConnection();
    }

    // Vérifie si une mise à jour de l'application est disponible.
    private void checkUpdate() {
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(HomeActivity.this);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, HomeActivity.this, WEATHER_FORECAST_APP_UPDATE_REQ_CODE);
                } catch (IntentSender.SendIntentException exception) {
                    Toaster.errorToast(this, "Update Failed");
                }
            }
        });
    }

}