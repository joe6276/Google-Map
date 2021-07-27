package com.example.gmp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity {
    private static final String TAG ="MainActivity";
    private static final int ERROR_DIALOG_CHECK=9001;
    private Button nmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nmap=(Button)findViewById(R.id.nmap);

        if(isServicesOk()){
            init();
        }
    }



    private void init() {
        nmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent( MainActivity.this,MapActivity.class);
                startActivity(i);
            }
        });
    }

    public boolean isServicesOk(){

        int available= GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
        if(available== ConnectionResult.SUCCESS){
            return true;
        }else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            Dialog dialog= GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this,available,ERROR_DIALOG_CHECK);
            dialog.show();
        }else{
            Toast.makeText(MainActivity.this,"Cannot connect",Toast.LENGTH_LONG).show();
        }
        return false;
    }
}