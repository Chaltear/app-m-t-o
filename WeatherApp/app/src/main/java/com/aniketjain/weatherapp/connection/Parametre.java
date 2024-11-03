package com.aniketjain.weatherapp.connection;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.aniketjain.weatherapp.HomeActivity;
import com.aniketjain.weatherapp.R;

// La classe Parametre étend Activity pour fournir une page de paramètres où les utilisateurs peuvent modifier le thème de l'application.
public class Parametre extends Activity {

    // Déclaration des composants de l'interface utilisateur.
    private Button validerbutton;
    private ConstraintLayout layout;
    private Switch switchtheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Associe cette activité à son layout XML.
        setContentView(R.layout.parametre);

        // Initialisation des composants de l'interface utilisateur.
        layout = findViewById(R.id.layout);
        switchtheme = findViewById(R.id.switchtheme);
        validerbutton = findViewById(R.id.validerbutton);

        // Définit un écouteur sur le switch pour changer la couleur de fond du layout en fonction de son état.
        switchtheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    // Si le switch est activé, change la couleur de fond en gris.
                    layout.setBackgroundColor(getResources().getColor(R.color.grey));
                } else {
                    // Si le switch est désactivé, rétablit la couleur de fond originale.
                    layout.setBackgroundColor(getResources().getColor(R.color.mainBGColor));
                }
            }
        });

        // Définit un écouteur sur le bouton valider pour sauvegarder l'état du switch et naviguer vers l'activité HomeActivity.
        validerbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Sauvegarde l'état du switch dans les SharedPreferences.
                boolean isSwitchChecked = switchtheme.isChecked();
                SharedPreferences sharedPreferences = getSharedPreferences("AppSettingsPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("ThemeChoice", isSwitchChecked);
                editor.apply();

                // Navigue vers HomeActivity.
                navigateToHomeActivity();
            }
        });

        // Récupère l'état du thème depuis les SharedPreferences pour initialiser correctement le switch.
        SharedPreferences sharedPreferences = getSharedPreferences("AppSettingsPrefs", MODE_PRIVATE);
        boolean isThemeDark = sharedPreferences.getBoolean("ThemeChoice", false);
        // Définit l'état du switch basé sur les préférences sauvegardées.
        switchtheme.setChecked(isThemeDark);

        // Applique immédiatement le thème basé sur l'état récupéré lors de l'ouverture de l'activité.
        if(isThemeDark) {
            // Applique le thème sombre si le switch était en position activée.
            layout.setBackgroundColor(getResources().getColor(R.color.grey));
        } else {
            // Applique le thème clair si le switch était en position désactivée.
            layout.setBackgroundColor(getResources().getColor(R.color.mainBGColor));
        }
    }

    // Méthode pour naviguer vers l'activité HomeActivity.
    private void navigateToHomeActivity() {
        Intent intent = new Intent(Parametre.this, HomeActivity.class);
        startActivity(intent);
    }
}
