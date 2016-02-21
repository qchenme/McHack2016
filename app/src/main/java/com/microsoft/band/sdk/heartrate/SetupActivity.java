package com.microsoft.band.sdk.heartrate;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import io.smooch.core.Message;
import io.smooch.core.Smooch;

public class SetupActivity extends Activity {

    private TextView prompt;
    private Button send;
    private EditText message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        prompt = (TextView) findViewById(R.id.prompt);
        send = (Button) findViewById(R.id.send);
        message = (EditText) findViewById(R.id.message);

        prompt.setText("What is your name?");

        send.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Smooch.getConversation().sendMessage(new Message("Hello, I am " + message.getText()));
                Intent intent = new Intent(SetupActivity.this, BandHeartRateAppActivity.class);
                startActivity(intent);
            }
        });
    }

}
