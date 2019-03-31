package co.recyclesolutions.fbtest;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {


    int count = 0;
    boolean res;

    public static final String TAG = "FB";

    private FirebaseAuth mAuth;

    TextView tvValue;

    String strValue;
    String email;
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvValue = findViewById(R.id.tvValue);
        final EditText etValue = findViewById(R.id.etDataEntry);
        final TextView etEmail = findViewById(R.id.etEmail);
        final TextView etPass = findViewById(R.id.etPass);




        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();




        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Sending data to the Firebase!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                strValue = etValue.getText().toString();
                email = etEmail.getText().toString();
                password = etPass.getText().toString();

                Log.d(TAG, "Email = " + email);
                Log.d(TAG, "Password = " + password);

                // Check user/pass, if exists write into FB, else create user



                FirebaseUser user = mAuth.getCurrentUser();
                Log.d(TAG, "CurrentUser = " + user);
                String userUI = mAuth.getUid();
                Log.d(TAG, "UserID = " + userUI);


                // Let's use switch/cas to figure the multiplus situations out

                try {
                    if (user == null){
                        if ((password == null) || password.equals("")) {
                            Toast.makeText(MainActivity.this, "Digie seu email e senha!",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else
                            signInUser();
                        if (!res)
                            createUser(email,password);

                    }
                    if (user.getEmail().equals(email)) {    // Check if the user typed his/her email
                        if ((password == null) || password.equals("")) {
                            Toast.makeText(MainActivity.this, "Digie seu email e senha!",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else
                            signInUser();
                        if (!res)
                            createUser(email,password);
                    }
                    if (user.getEmail() == null){
                            createUser(email, password);

                    }
                    if (user.getEmail().equals("")){        // Checked if the user didn't type any email
                        Toast.makeText(MainActivity.this, "Digie seu email e senha!",
                                Toast.LENGTH_SHORT).show();
                    }

                }
                catch(NullPointerException e){
                    Log.d(TAG, "Erro 1: " + e);
                }


            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
       // updateUI(currentUser);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        mAuth.signOut();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            finishAffinity();
            System.exit(0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateUI(FirebaseUser user){

        Log.d(TAG, "The User is: " + user);
        //mAuth.updateCurrentUser(user);
        //createUser(email,password);
        if (user != null) {
            res = true;
            Log.d(TAG, "Res = " + res);
            writeIntoFB(count);
            mAuth.signOut();
        }
        else {
            res = false;
            Log.d(TAG, "Res = " + res);
            mAuth.signOut();
        }

    }


    public  void  createUser(String email, String password) {


            // Create new user
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "Cadastrado com sucesso!");
                                FirebaseUser user = mAuth.getCurrentUser();
                                updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.d(TAG, "Falha ao cadastrar!", task.getException());
                                Toast.makeText(MainActivity.this, "Email ou senha inválidos! (C)",
                                        Toast.LENGTH_SHORT).show();
                                //updateUI(null);
                                signInUser();
                            }

                            // ...
                        }
                    });



    }

    public  boolean  checkUser(String email, String password) {

        //mAuth = FirebaseAuth.getInstance();

        try {
            if (mAuth.getCurrentUser().toString() == email) {
                Log.d(TAG, "Usuário está correto!: ");
                return true;
            }
        }
        catch(NullPointerException e){
                Log.d(TAG, "Erro: " + e);
                return  false;
        }


        if (mAuth.getCurrentUser().toString() == email){
            Log.d(TAG, "Usuário está correto!: ");
            return true;
        }
        return false;
    }

    public void writeIntoFB(int c){

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message/" + count);

        myRef.setValue(strValue + ": " + c);

        count++;



        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d(TAG, "Data wrote is: " + value);

                tvValue.setText(value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }

    public void signInUser(){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "Autenticado com Sucesso!");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "Falha ao autenticar!", task.getException());
                            Toast.makeText(MainActivity.this, "Email ou senha inválidos! (A)",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);

                        }

                        // ...
                    }
                });

    }
}
