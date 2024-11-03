package com.aniketjain.weatherapp.connection;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.aniketjain.weatherapp.R;
import com.aniketjain.weatherapp.SplashScreen;

// Cette activité permet à l'utilisateur de s'enregistrer dans l'application.
public class Registration extends Activity {

    // Déclaration des éléments de l'interface utilisateur.
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText confirmEditText;
    private Button registrationButton;
    private ConstraintLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Associe le layout XML de cette activité.
        setContentView(R.layout.registration);

        // Initialise les éléments de l'interface utilisateur.
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.PasswordEditText);
        confirmEditText = findViewById(R.id.PasswordEditText2);
        registrationButton = findViewById(R.id.loginButton);

        // Écouteur d'événement sur le bouton d'inscription pour déclencher la méthode registerUser().
        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        // Applique le thème choisi par l'utilisateur (clair ou sombre) à partir des SharedPreferences.
        layout = findViewById(R.id.layout);
        SharedPreferences sharedPreferences = getSharedPreferences("AppSettingsPrefs", MODE_PRIVATE);
        boolean isThemeDark = sharedPreferences.getBoolean("ThemeChoice", false);
        if (isThemeDark) {
            layout.setBackgroundColor(getResources().getColor(R.color.grey));
        } else {
            layout.setBackgroundColor(getResources().getColor(R.color.mainBGColor));
        }
    }

    // Méthode pour enregistrer l'utilisateur.
    private void registerUser() {
        // Récupère les textes saisis dans les champs.
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmEditText.getText().toString();

        // Vérifie si les mots de passe saisis correspondent.
        if (password.equals(confirmPassword)) {
            // Si les mots de passe correspondent, l'utilisateur est ajouté à la base de données.
            DBHelper helper = new DBHelper(Registration.this);
            helper.addUser(username, password);

            // Affiche un toast de succès et navigue vers l'écran de SplashScreen.
            Toast.makeText(Registration.this, "Utilisateur enregistré avec succès", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Registration.this, SplashScreen.class);
            startActivity(intent);
        } else {
            // Si les mots de passe ne correspondent pas, affiche un message d'erreur.
            Toast.makeText(Registration.this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
            // Colore les champs de texte en rouge pour indiquer l'erreur.
            usernameEditText.setTextColor(getResources().getColor(R.color.red));
            passwordEditText.setTextColor(getResources().getColor(R.color.red));
            confirmEditText.setTextColor(getResources().getColor(R.color.red));
        }
    }
}
