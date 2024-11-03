package com.aniketjain.weatherapp.connection;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.aniketjain.weatherapp.HomeActivity;
import com.aniketjain.weatherapp.R;
import com.aniketjain.weatherapp.SplashScreen;

// La classe Login hérite d'Activity, fournissant une interface utilisateur pour que l'utilisateur puisse se connecter.
public class Login extends Activity {

    // Déclaration des variables de vue pour les interactions d'interface utilisateur.
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView registerLink;
    private TextView username, password;
    private ConstraintLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Définit le fichier de layout XML à utiliser pour cette activité.
        setContentView(R.layout.login);

        // Initialisation des vues à l'aide de findViewById pour interagir avec les éléments d'interface utilisateur du fichier XML.
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.PasswordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerLink = findViewById(R.id.registerLink);
        username = findViewById(R.id.usernameTextView);
        password = findViewById(R.id.passwordTextView);

        // Définit un écouteur de clic sur le bouton de connexion.
        // Lorsque le bouton est cliqué, la méthode loginUser() est appelée.
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        // Définit un écouteur de clic sur le lien d'inscription.
        // Lorsque le lien est cliqué, l'utilisateur est dirigé vers la page d'inscription.
        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToRegisterPage();
            }
        });

        // Récupère le ConstraintLayout pour appliquer le thème.
        layout = findViewById(R.id.layout);

        // Récupère les préférences partagées pour vérifier si l'utilisateur a activé le thème sombre.
        SharedPreferences sharedPreferences = getSharedPreferences("AppSettingsPrefs", MODE_PRIVATE);
        boolean isThemeDark = sharedPreferences.getBoolean("ThemeChoice", false);

        // Applique la couleur de fond en fonction du choix du thème de l'utilisateur.
        if (isThemeDark) {
            layout.setBackgroundColor(getResources().getColor(R.color.grey));
        } else {
            layout.setBackgroundColor(getResources().getColor(R.color.mainBGColor));
        }
    }

    // Tente de connecter l'utilisateur en utilisant les informations saisies.
    private void loginUser() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        // Crée une instance de DBHelper pour vérifier les informations d'identification de l'utilisateur.
        DBHelper dbHelper = new DBHelper(this);

        // Si les informations d'identification sont correctes, navigue vers l'activité principale.
        // Sinon, affiche une erreur de connexion.
        if (dbHelper.checkUser(username, password)) {
            navigateToHomeActivity();
        } else {
            showLoginError();
        }
    }

    // Affiche un message d'erreur si les informations d'identification sont incorrectes.
    private void showLoginError() {
        Toast.makeText(Login.this, "Identifiants incorrects", Toast.LENGTH_SHORT).show();
        username.setTextColor(getResources().getColor(R.color.red));
        password.setTextColor(getResources().getColor(R.color.red));
    }

    // Navigue vers la page d'inscription.
    private void navigateToRegisterPage() {
        Intent intent = new Intent(Login.this, Registration.class);
        startActivity(intent);
    }

    // Navigue vers l'activité principale après une connexion réussie.
    private void navigateToHomeActivity() {
        Intent intent = new Intent(Login.this, SplashScreen.class);
        startActivity(intent);
    }
}
