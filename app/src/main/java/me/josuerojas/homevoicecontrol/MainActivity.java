package me.josuerojas.homevoicecontrol;

import android.content.Intent;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends ActionBarActivity {

    private static final int REQUEST_CODE = 1234;
    TextToSpeech tts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                   // tts.setLanguage(Locale.getDefault());
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

}

