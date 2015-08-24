package me.josuerojas.homevoicecontrol;

import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.ParcelUuid;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;


public class MainActivity extends ActionBarActivity {

    private BluetoothAdapter myBluetoothAdapter;
    private OutputStream outputStream;
    private InputStream inStream;
    private ArrayAdapter<String> BTArrayAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CODE = 1234;
    TextToSpeech tts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    // tts.setLanguage(Locale.getDefault());
                }
            }
        });
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (myBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Your device does not support Bluetooth",
                    Toast.LENGTH_LONG).show();
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.action_btnOn:
                onBT();
                return true;
            case R.id.action_btnOff:
                offBT();
                return true;
            case R.id.action_establishCnt:
                establishConnection();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    public void speakButtonClicked(View v) {
        startVoiceRecognitionActivity();
    }


    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Diga un comando...");
        startActivityForResult(intent, REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            char firstCommand = commandFirstLevel(matches.get(0).toLowerCase());
            char secondCommand = commandSecondLevel(matches.get(0).toLowerCase());
            String text = "";
            if (firstCommand == 'i' || secondCommand == 'i'){
                text = "Disculpa, no te entendí.";
            } else {
                if (firstCommand == 'a'){
                    if (secondCommand == 'a'){
                        text = "Activando Alarma";
                    } else if (secondCommand == 'l'){
                        text = "Encendiendo Luces";
                    } else if (secondCommand == 'p'){
                        text = "Abriendo la Puerta";
                    } else {
                        text = "Encendiendo Ventilador";
                    }
                } else if(firstCommand == 'd'){
                    if (secondCommand == 'a'){
                        text = "Desactivando Alarma";
                    } else  if (secondCommand == 'l'){
                        text = "Apagando Luces";
                    }  else if (secondCommand == 'p'){
                        text = "Cerrando la Puerta";
                    } else {
                        text = "Apagando Ventilador";
                    }
                }
            }
            if (Build.VERSION.RELEASE.startsWith("5")) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            }
            else {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // retVal puede tener el valor 'a' si corresponde a un comando de a|ctivar, valor 'd' si corresponde a desactivar y valor de 'i' si esta indefinido
    protected char commandFirstLevel(String command){
        char retVal = 'i';
        if (command.matches("^.*?(apagar|apaga|apagá|desactivar|desactiva|desactivá|cerrar|cierra|cerrá|oscurecer|oscurece|oscurecé|quitar|quita|quitá|no trabajar|no trabaje|" +
                "no funcione|no funcionar|no función|no quiero|no querer|no quiere|silenciar|silencio|silenciá|silencia|deshace|deshacer|deshacé|apagues|apagués|desactives|" +
                "desactivés|cierres|cerres|cerrés|oscurezcas|quites|quités|pare de trabajar|pares de trabajar|deje de funcionar|dejes de funcionar|silencies|silenciés|" +
                "deshagas|deshagás).*$")){
            retVal = 'd';
        } else if (command.matches("^.*?(encender|enciende|encendé|producir|produce|produzca|haz|hace|hacé|agregar|agrega|agregá|prender|prende|prendé|activar|activa|activá|abrir|abre|abrí|" +
                "iluminar|ilumnia|iluminá|pon|poner|poné|trabajar|trabaje|emitir|emita|emite|funcionar|funcione|función|querer|quiero|quiere|" +
                "enciendeme|encendeme|producí|producime|haceme|hacerme|agregame|préndeme|prendeme|activame|actívame|abrime|ábreme|ilumíname|ponme|poneme|emitime).*$")){
            retVal = 'a';
        }
        return retVal;
    }


    protected char commandSecondLevel(String command){
        char retVal = 'i';
        if (command.matches("^.*?(luz|foco|led|bombilla|linterna|iluminación|luminosidad|candil|candela|claridad|bujía|faro|alumbrado|alumbres|sol|luces|alumbrés).*$")){
            retVal = 'l';
        } else if (command.matches("^.*?(alarma|sirena|pitido|pito|silbato|silbo|chiflo|bulla|bullicio|alboroto|escándalo|bochinche|señal|sonido|sonora|sonar|suenes|ruido|incendio).*$")){
            retVal = 'a';
        } else if (command.matches("^.*?(puerta|portón|tranca|cerca|entrar|compuerta|entrada|ingreso|acceso|contrapuerta|ingreso|salida|evacuación|admisión|recepción|paso|recibimiento).*$")){
            retVal = 'p';
        } else if (command.matches("^.*?(ventilador|aire|viento|soplo|abanico|ventilación|brisa|frío|frio|corriente|ventana|fresco|frescura).*$")){
            retVal = 'v';
        }
        return retVal;
    }

    public void onBT(){
        if (!myBluetoothAdapter.isEnabled()) {
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);
            Toast.makeText(getApplicationContext(),"Bluetooth turned on" ,
                    Toast.LENGTH_LONG).show();
        } else{
            Toast.makeText(getApplicationContext(),"Bluetooth is already on",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void offBT(){
        myBluetoothAdapter.disable();
        Toast.makeText(getApplicationContext(),"Bluetooth turned off",
                Toast.LENGTH_LONG).show();
    }

    public void establishConnection(){
        if (myBluetoothAdapter.isEnabled()) {
            Set<BluetoothDevice> bondedDevices = myBluetoothAdapter.getBondedDevices();

            if(bondedDevices.size() > 0){
                Log.e("error", String.valueOf(bondedDevices));
                BluetoothDevice[] devices = (BluetoothDevice[]) bondedDevices.toArray();
                BluetoothDevice device = devices[0];
                ParcelUuid[] uuids = device.getUuids();
                BluetoothSocket socket = null;
                try {
                    socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
                    socket.connect();
                    outputStream = socket.getOutputStream();
                    inStream = socket.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Toast.makeText(getApplicationContext(),"The devices has not been paired yet.",
                    Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(getApplicationContext(),"Bluetooth is disabled.",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void write(String s) throws IOException {
        outputStream.write(s.getBytes());
    }

    final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                BTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    public void find() {
        if (myBluetoothAdapter.isDiscovering()) {
            myBluetoothAdapter.cancelDiscovery();
        }
        else {
            BTArrayAdapter.clear();
            myBluetoothAdapter.startDiscovery();
            registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }

}

