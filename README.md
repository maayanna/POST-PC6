# POST-PC-EX6

I pledge the highest level of ethical principles in support of academic excellence.  I ensure that all of my work reflects my own abilities and not those of someone else.

ANSWER TO QUESTION :

In order to know when the SMS was delivered, while calling the method sendTextMessage from the SmsManager we can add as the fourth argument a PendindItent for delivery. Specially a broadcast receiver that will check if the transmission was successfull.
If not NULL this PendingIntent is broadcast when the message is delivered to the recipient (successfully sent or failed).
It means that it checks for the sms transmission and send result depending on what append, so we could update the notification we want to send. Like that, we know if the notificaton was received or not.

We can use this code to get the updates and add a brodcast as the PendingIntent :

PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), PendingIntent.FLAG_NO_CREATE);
deliverActivity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(getBaseContext(), "SMS delivered", 
                    Toast.LENGTH_LONG).show();
                    finishActivity();
                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(getBaseContext(), "SMS not delivered", 
                    Toast.LENGTH_LONG).show();
                    finishActivity();
                    break;           

            }
        }
      

We also have as the third argument a PendingIntent to know if the sms was sent.
The result code will be Activity.RESULT_OK for success, or one of these errors:
RESULT_ERROR_GENERIC_FAILURE
RESULT_ERROR_RADIO_OFF
RESULT_ERROR_NULL_PDU
