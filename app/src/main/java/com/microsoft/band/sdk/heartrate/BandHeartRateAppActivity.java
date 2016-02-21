// Modified from:
//
//Copyright (c) Microsoft Corporation All rights reserved.
// 
//MIT License: 
// 
//Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
//documentation files (the  "Software"), to deal in the Software without restriction, including without limitation
//the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
//to permit persons to whom the Software is furnished to do so, subject to the following conditions: 
// 
//The above copyright notice and this permission notice shall be included in all copies or substantial portions of
//the Software. 
// 
//THE SOFTWARE IS PROVIDED ""AS IS"", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
//TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
//THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
//CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
//IN THE SOFTWARE.
package com.microsoft.band.sdk.heartrate;

import java.lang.ref.WeakReference;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.BandIOException;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;

import android.os.Bundle;
import android.view.View;
import android.app.Activity;
import android.os.AsyncTask;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import io.smooch.core.Message;
import io.smooch.core.Smooch;
import io.smooch.ui.ConversationActivity;

public class BandHeartRateAppActivity extends Activity {

	private BandClient client = null;
	private Button btnStart, btnStop, btnConsent, btnHelp;
	private TextView txtStatus;
    private EditText low_text;
    private EditText high_text;
    private boolean msgSent = false;
    private int low = 75;
    private int high = 80;
//    private SharedPreferences pref;
//    boolean firstLaunch;

    private BandHeartRateEventListener mHeartRateEventListener = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(final BandHeartRateEvent event) {
            try {
                low = Integer.parseInt(low_text.getText().toString());
            } catch (Exception e) {
                low = 75;
//                low_text.setText("75");
            }
            try {
                high = Integer.parseInt(high_text.getText().toString());
            } catch (Exception e) {
                high = 80;
//                high_text.setText("80");
            }

            if (event != null) {
				int heartRate = event.getHeartRate();
				String statusStr = String.format("Heart Rate = %d beats per minute\n"
						+ "Quality = %s\n", event.getHeartRate(), event.getQuality());
				if (heartRate < low) {
					statusStr += String.format("Heart Rate is lower than %d\n", low);
				} else if (heartRate > high) {
					statusStr += String.format("Heart Rate is higher than %d\n", high);
				}

//            	appendToUI(String.format("Heart Rate = %d beats per minute\n"
//            			+ "Quality = %s\n", event.getHeartRate(), event.getQuality()));
				appendToUI(statusStr);

                if (!msgSent) {
                    if (heartRate < low ) {
                        Smooch.getConversation().sendMessage(new Message("Heart Rate is too low: "+statusStr));
                        msgSent = true;
                    } else if (heartRate > high) {
                        Smooch.getConversation().sendMessage(new Message("Heart Rate is too high: "+statusStr));
                        msgSent = true;
                    }
                } else if ((heartRate > low && heartRate < high) && msgSent) {
                    Smooch.getConversation().sendMessage(new Message("Back to normal: "+statusStr));
                    msgSent = false;
                }

            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        low_text = (EditText) findViewById(R.id.low_text);
        high_text = (EditText) findViewById(R.id.high_text);
        low_text.setText("75");
        high_text.setText("80");


//        pref = getSharedPreferences("Config", MODE_PRIVATE);
//        firstLaunch = pref.getBoolean("firstLaunch", true);
//
//        if (firstLaunch) {
//            pref.edit().putBoolean("firstLaunch", false);
//            Intent intent = new Intent(BandHeartRateAppActivity.this, SetupActivity.class);
//            startActivity(intent);
//        }

//        boolean firstLaunch = preferences.getBoolean("firstLaunch", true);
//        if (firstLaunch) {
//            Smooch.track("init");
//            ConversationActivity.show(this);
//        }


        txtStatus = (TextView) findViewById(R.id.txtStatus);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				txtStatus.setText("");
				new HeartRateSubscriptionTask().execute();
                Smooch.getConversation().sendMessage(new Message("Heart rate monitoring has been turned on"));
			}
		});

        btnStop = (Button) findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (client != null) {
        			try {
        				client.getSensorManager().unregisterHeartRateEventListener(mHeartRateEventListener);
                        txtStatus.setText("Heart rate monitoring has been turned off");
                        Smooch.getConversation().sendMessage(new Message("Heart rate monitoring has been turned off"));

        			} catch (BandIOException e) {
        				appendToUI(e.getMessage());
        			}
        		}
            }
        });
        
        final WeakReference<Activity> reference = new WeakReference<Activity>(this);
        
        btnConsent = (Button) findViewById(R.id.btnConsent);
        btnConsent.setOnClickListener(new OnClickListener() {
			@SuppressWarnings("unchecked")
            @Override
			public void onClick(View v) {
				new HeartRateConsentTask().execute(reference);
			}
		});

        btnHelp = (Button) findViewById(R.id.btnHelp);
        btnHelp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                ConversationActivity.show(BandHeartRateAppActivity.this);
            }
        });

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
        new HeartRateSubscriptionTask().execute();
        Smooch.getConversation().sendMessage(new Message("App has been turned on"));
//            }
//        }).start();
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		txtStatus.setText("...");
        new HeartRateSubscriptionTask().execute();
	}
	
//    @Override
//	protected void onPause() {
//		super.onPause();
//		if (client != null) {
//			try {
//				client.getSensorManager().unregisterHeartRateEventListener(mHeartRateEventListener);
//			} catch (BandIOException e) {
//				appendToUI(e.getMessage());
//			}
//		}
//	}
	
    @Override
    protected void onDestroy() {
        if (client != null) {
            try {
                client.disconnect().await();
            } catch (InterruptedException e) {
                // Do nothing as this is happening during destroy
            } catch (BandException e) {
                // Do nothing as this is happening during destroy
            }
        }
        super.onDestroy();
    }

	private class HeartRateSubscriptionTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				if (getConnectedBandClient()) {
					if (client.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED) {
						client.getSensorManager().registerHeartRateEventListener(mHeartRateEventListener);
					} else {
						appendToUI("Please press the Pair Wristband button, and then Start monitoring\n");
					}
				} else {
					appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
				}
			} catch (BandException e) {
				String exceptionMessage="";
				switch (e.getErrorType()) {
				case UNSUPPORTED_SDK_VERSION_ERROR:
					exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
					break;
				case SERVICE_ERROR:
					exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
					break;
				default:
					exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
					break;
				}
				appendToUI(exceptionMessage);

			} catch (Exception e) {
				appendToUI(e.getMessage());
			}
			return null;
		}
	}
	
	private class HeartRateConsentTask extends AsyncTask<WeakReference<Activity>, Void, Void> {
		@Override
		protected Void doInBackground(WeakReference<Activity>... params) {
			try {
				if (getConnectedBandClient()) {
					
					if (params[0].get() != null) {
						client.getSensorManager().requestHeartRateConsent(params[0].get(), new HeartRateConsentListener() {
							@Override
							public void userAccepted(boolean consentGiven) {
							}
					    });
					}
				} else {
					appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
				}
			} catch (BandException e) {
				String exceptionMessage="";
				switch (e.getErrorType()) {
				case UNSUPPORTED_SDK_VERSION_ERROR:
					exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
					break;
				case SERVICE_ERROR:
					exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
					break;
				default:
					exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
					break;
				}
				appendToUI(exceptionMessage);

			} catch (Exception e) {
				appendToUI(e.getMessage());
			}
			return null;
		}
	}
	
	private void appendToUI(final String string) {
		this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	txtStatus.setText(string);
            }
        });
	}
    
	private boolean getConnectedBandClient() throws InterruptedException, BandException {
		if (client == null) {
			BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
			if (devices.length == 0) {
				appendToUI("Band isn't paired with your phone.\n");
				return false;
			}
			client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
		} else if (ConnectionState.CONNECTED == client.getConnectionState()) {
			return true;
		}
		
		appendToUI("Band is connecting...\n");
		return ConnectionState.CONNECTED == client.connect().await();
	}
}

