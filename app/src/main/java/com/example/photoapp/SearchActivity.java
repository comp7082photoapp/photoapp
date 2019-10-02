package com.example.photoapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SearchActivity extends Activity {
    private Button searchButton;
    private EditText captionEditText;
    private EditText fromEditText;
    private EditText toEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        searchButton = findViewById(R.id.search_button);
        captionEditText = findViewById(R.id.captionEditText);
        fromEditText = findViewById(R.id.fromEditText);
        toEditText = findViewById(R.id.toEditText);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String from = fromEditText.getText().toString();
                String to = toEditText.getText().toString();
                String caption = captionEditText.getText().toString();

                if(from.isEmpty()){
                    from = "0";
                }
                if(to.isEmpty()){
                    to = "0";
                }
                if(caption.isEmpty()){
                    caption = "";
                }

                Intent data = new Intent();
                data.putExtra("from",from);
                data.putExtra("to",to);
                data.putExtra("caption",caption);
                setResult(2,data);
                finish();
            }
        });

    }



}
