package com.example.diego.geogamespacman;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Button startGameBtn = (Button) findViewById(R.id.start_btn);
        startGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame(v);

            }
        });
    }

    public void startGame(View v){
        Intent gameIntent = new Intent(getBaseContext(), QuestionActivity.class);
        //gameIntent.putExtra("EXTRA_SELECTION", 1);
        startActivity(gameIntent);
        Intent serviceStart = new Intent(getBaseContext(), GameService.class);
        startService(serviceStart);
    }
}
