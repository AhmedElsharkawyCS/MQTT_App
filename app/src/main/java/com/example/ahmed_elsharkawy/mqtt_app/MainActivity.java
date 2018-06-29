package com.example.ahmed_elsharkawy.mqtt_app;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.QuickContactBadge;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.net.URI;

public class MainActivity extends AppCompatActivity {

   private Button connect_button,publish_button,sub_button,disconButton,unsbscribeButton;
    private EditText port,clientId,Url,publish_topic,publish_message,sub_topic,qos,unsbscribe_topic;
    private CheckBox publish_retain;
    private NotificationCompat.Builder mBuilder;
    private NotificationManagerCompat notificationManager;
    private Intent intent;
    private static final int notificationId=456178;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);

        //get reference of items
         intent = new Intent(this,MainActivity.class);
        //connection
        connect_button=(Button)findViewById (R.id.connct);
        port=(EditText)findViewById (R.id.port);
        clientId=(EditText)findViewById (R.id.cientid_c);
        Url=(EditText)findViewById (R.id.hosturl);
        clientId.setText ( MqttClient.generateClientId());
        Url.setText ("mqtt://m12.cloudmqtt.com");
        port.setText ("15149 ");
        final String get_portNum=port.getText ().toString ().trim ();
        final String get_server_url=Url.getText ().toString ().trim ();
        final String get_clientId=clientId.getText ().toString ();

        //publishing
        publish_button=(Button)findViewById (R.id.publish_button);
        publish_message=(EditText)findViewById (R.id.publish_message);
        publish_retain=(CheckBox)findViewById (R.id.retain);
        publish_topic=(EditText)findViewById (R.id.publish_topic);

        //subscribing
        sub_button=(Button)findViewById (R.id.sub_button);
        sub_topic=(EditText)findViewById (R.id.sub_topic);
        qos=(EditText)findViewById (R.id.sub_qos);

        //disconnecting
        disconButton=(Button)findViewById (R.id.disconnected);

        //unsubscribe
        unsbscribeButton=(Button)findViewById (R.id.ubsbscribe_button);
        unsbscribe_topic=(EditText)findViewById (R.id.ubsbscribe_topic);


        //create notification object
         mBuilder = new NotificationCompat.Builder(this);
         notificationManager = NotificationManagerCompat.from(this);


        //prepare connection
        final MqttAndroidClient client_con =
                new MqttAndroidClient(getBaseContext (), get_server_url+":"+get_portNum,
                        get_clientId);




        //end preparation

        //set action of connection button
        connect_button.setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {

               // Log.e ("sd",get_server_url+get_portNum);
                _connect (client_con);
            }
        });

        //Publish action
        publish_button.setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                String get_topic=publish_topic.getText ().toString ();
                String get_message=publish_message.getText ().toString ();
                boolean retain=publish_retain.isChecked ();
                _publish (client_con,get_topic,get_message,retain);
            }
        });

        //subscribe action
        sub_button.setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                String get_topic=sub_topic.getText ().toString ();
                int get_qos=Integer.getInteger (qos.getText ().toString ());
                _subscribe (client_con,get_topic,get_qos);
            }
        });

        //disconnected action
        disconButton.setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                _disconnected (client_con);
            }
        });

        //unsubscribe action
        unsbscribeButton.setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {

                String get_topic=unsbscribe_topic.getText ().toString ().trim ();
                _unsubscribe (client_con,get_topic);
            }
        });

    }



    void _connect(MqttAndroidClient client){

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                   // Log.d("Done", "onSuccess");
                    Toast.makeText (getBaseContext (),"You are connected",Toast.LENGTH_SHORT).show ();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText (getBaseContext (),"Connection Field",Toast.LENGTH_SHORT).show ();


                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }


    void _publish(MqttAndroidClient client,String topic,String message_pass ,boolean retain){

        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = message_pass.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setRetained(retain);
            client.publish(topic, message);
            Toast.makeText (getBaseContext (),message_pass+"\n published successfully",Toast.LENGTH_SHORT).show ();

        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
            Toast.makeText (getBaseContext (),"publishing field",Toast.LENGTH_SHORT).show ();
        }
    }


    void _subscribe(MqttAndroidClient client,String topic ,int qos){

        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
                    Toast.makeText (getBaseContext ()," The message was published",Toast.LENGTH_SHORT).show ();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                    Toast.makeText (getBaseContext ()," The subscription could not be performed",Toast.LENGTH_SHORT).show ();
                }
            });

            client.setCallback (new MqttCallback ( ) {
                @Override
                public void connectionLost(Throwable cause) {

                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {

                 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                 PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext (), 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);

                 mBuilder.setSmallIcon (R.drawable.ic_notifications_black_24dp);
                 mBuilder.setWhen (System.currentTimeMillis ());
                 mBuilder.setContentTitle (topic);
                 mBuilder.setContentText ( new String (message.getPayload ()));
                 mBuilder.setPriority (NotificationCompat.PRIORITY_DEFAULT);
                 mBuilder.setContentIntent (pendingIntent);
                 mBuilder.setAutoCancel (true);

                 notificationManager.notify(notificationId, mBuilder.build());



                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    void _disconnected(MqttAndroidClient client){

        try {
            IMqttToken disconToken = client.disconnect();
            disconToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // we are now successfully disconnected
                    Toast.makeText (getBaseContext ()," successfully disconnected",Toast.LENGTH_SHORT).show ();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // something went wrong, but probably we are disconnected anyway
                    Toast.makeText (getBaseContext ()," something went wrong, but probably we are disconnected anyway",Toast.LENGTH_SHORT).show ();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    void _unsubscribe(MqttAndroidClient client,String topic){

        try {
            IMqttToken unsubToken = client.unsubscribe(topic);
            unsubToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The subscription could successfully be removed from the client
                    Toast.makeText (getBaseContext ()," The subscription could successfully be removed from the client",Toast.LENGTH_SHORT).show ();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // some error occurred, this is very unlikely as even if the client
                    // did not had a subscription to the topic the unsubscribe action
                    // will be successfully
                    Toast.makeText (getBaseContext (),"did not had a subscription or some error occurred ",Toast.LENGTH_SHORT).show ();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
    }
    }



}
