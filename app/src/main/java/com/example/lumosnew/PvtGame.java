package com.example.lumosnew;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class PvtGame extends AppCompatActivity implements View.OnClickListener {
    private Button continue_btn;
    private TextView round_text, lapse_text, false_start_text, response_time_text;
    private int current_round = 1;
    private int false_start = 0;
    private int lapse = 0;
    private int total_response_time = 0;
    private int total_hit = 0;

    private Handler handler;
    private Runnable runnable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.pvt_game_page);
        continue_btn = findViewById(R.id.pvt_continue);
        continue_btn.setOnClickListener(this);

        round_text = findViewById(R.id.current_round);
        lapse_text = findViewById(R.id.total_lapse);
        false_start_text = findViewById(R.id.total_false_start);
        response_time_text = findViewById(R.id.average_response_time);

        resetValues();

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                startGame();
                handler.postDelayed(this, 1000);
            }
        };

        // Start the initial runnable task by posting through the handler
        handler.post(runnable);
    }

    private void startGame() {
        Log.i("PVT", "ready read " + MainActivity.pvt_read_to_read);
        if(MainActivity.pvt_read_to_read == true){
            MainActivity.pvt_read_to_read = false;
            Log.i("PVT", "the value got: " + Integer.toHexString(MainActivity.pvt_value & 0xFF).toUpperCase());
            if(MainActivity.pvt_value == (byte) 0x00){
                false_start++;
                false_start_text.setText("total false start : " + false_start);
            }else if(MainActivity.pvt_value == (byte) 0xFF){

                lapse++;
                Log.i("PVT","enter ff " + lapse);
                lapse_text.setText("total lapse : " + lapse);
            }else{
                total_hit++;
                total_response_time += (int) (100 + (0xff&MainActivity.pvt_value - 1) * (400.0 / 253));
                response_time_text.setText("ave response time : " + (total_response_time/total_hit) + " ms");
            }
        }
        if(current_round == 10){
            //finish game
        }
    }

    @Override
    public void onClick(View view) {
        if(current_round == 10){
            MainActivity.pvtChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            byte[] byteArray = { (byte) 0xCC };
            MainActivity.pvtChar.setValue(byteArray);
            if(MainActivity.LBluetoothGatt.writeCharacteristic(MainActivity.pvtChar)){
                finish();
            }
        }
        MainActivity.pvtChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        byte[] byteArray = { (byte) 0xCC };
        MainActivity.pvtChar.setValue(byteArray);
        if(MainActivity.LBluetoothGatt.writeCharacteristic(MainActivity.pvtChar)){
            current_round++;
            round_text.setText("current round : " + current_round);
            if(current_round == 10){
                continue_btn.setText("Finish game");
            }
        }
    }

    private void resetValues() {
        current_round = 1;
        false_start = 0;
        lapse = 0;
        total_response_time = 0;
        total_hit = 0;
    }
}
