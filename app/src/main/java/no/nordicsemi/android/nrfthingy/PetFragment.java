package no.nordicsemi.android.nrfthingy;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.JsonWriter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import no.nordicsemi.android.nrfthingy.common.ScannerFragmentListener;
import no.nordicsemi.android.nrfthingy.common.Utils;
import no.nordicsemi.android.nrfthingy.database.DatabaseContract;
import no.nordicsemi.android.nrfthingy.database.DatabaseHelper;
import no.nordicsemi.android.thingylib.ThingyListener;
import no.nordicsemi.android.thingylib.ThingyListenerHelper;
import no.nordicsemi.android.thingylib.ThingySdkManager;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static no.nordicsemi.android.nrfthingy.common.Utils.PET_FRAGMENT;
import static no.nordicsemi.android.nrfthingy.common.Utils.REQUEST_ENABLE_BT;
import static no.nordicsemi.android.nrfthingy.common.Utils.TAG;

/**
 * Created by dongsung on 2018-02-01.
 * Edited by HyoungHo on 2018-08-14.
 */

public class PetFragment extends Fragment implements ScannerFragmentListener {

    private TextView mDetectionTextViewMotion;
    private TextView mDetectionTextViewSound;

    private BluetoothDevice mDevice;

    private ThingySdkManager mThingySdkManager = null;
    private DatabaseHelper mDatabaseHelper;
    private Switch mDetectionSwitch;

    private ListView mHistoryView;
    private ListView mFeatureVectorView;

    private ListViewAdapter mDetectionAdapter;
    private ListViewAdapter mFeatureVectorAdapter;

    private FileWriter fw;
    private BufferedWriter bw;

    private ArrayList<String> mHistoryLog = new ArrayList<String>();
    private ArrayList<String> mFeatureLog = new ArrayList<String>();

    private String time;

    OkHttpClient client;
    MediaType JSON;

    // PME result
    private ImageView result_img_motion;
    private ImageView result_img_sound;

    private ThingyListener mThingyListener = new ThingyListener() {
        @Override
        public void onDeviceConnected(BluetoothDevice device, int connectionState) {

        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device, int connectionState) {

        }

        @Override
        public void onServiceDiscoveryCompleted(BluetoothDevice device) {

        }

        @Override
        public void onBatteryLevelChanged(BluetoothDevice bluetoothDevice, int batteryLevel) {

        }

        @Override
        public void onTemperatureValueChangedEvent(BluetoothDevice bluetoothDevice, String temperature) {

        }

        @Override
        public void onPressureValueChangedEvent(BluetoothDevice bluetoothDevice, String pressure) {

        }

        @Override
        public void onHumidityValueChangedEvent(BluetoothDevice bluetoothDevice, String humidity) {

        }

        @Override
        public void onAirQualityValueChangedEvent(BluetoothDevice bluetoothDevice, int eco2, int tvoc) {

        }

        @Override
        public void onColorIntensityValueChangedEvent(BluetoothDevice bluetoothDevice, float red, float green, float blue, float alpha) {

        }

        @Override
        public void onButtonStateChangedEvent(BluetoothDevice bluetoothDevice, int buttonState) {

        }

        @Override
        public void onTapValueChangedEvent(BluetoothDevice bluetoothDevice, int direction, int count) {

        }

        @Override
        public void onOrientationValueChangedEvent(BluetoothDevice bluetoothDevice, int orientation) {

        }

        @Override
        public void onQuaternionValueChangedEvent(BluetoothDevice bluetoothDevice, float w, float x, float y, float z) {

        }

        @Override
        public void onPedometerValueChangedEvent(BluetoothDevice bluetoothDevice, int steps, long duration) {

        }


        /**
         * Knowledge Pack value means user's current behavior
         *
         * @param bluetoothDevice
         * @param status
         */

        @Override
        public void onKnowledgePackValueChangedEvent(BluetoothDevice bluetoothDevice, String status, String indicator) {


            time = new SimpleDateFormat("HH:mm:ss").format(new Date(System.currentTimeMillis()));
            //mDetectionAdapter.addItem(indicator + "_" + status, "time : " + time);
            //mDetectionAdapter.notifyDataSetChanged();

            if(indicator.equals("0")) { // sound
                if(status.equals("0"))
                    mDetectionAdapter.addItem(indicator + ":unknown", "time : " + time);
                else if(status.equals("1"))
                    mDetectionAdapter.addItem(indicator + ":barking", "time : " + time);
                else if(status.equals("2"))
                    mDetectionAdapter.addItem(indicator + ":growling", "time : " + time);
                else if(status.equals("3"))
                    mDetectionAdapter.addItem(indicator + ":drinking", "time : " + time);
                else if(status.equals("4"))
                    mDetectionAdapter.addItem(indicator + ":eating", "time : " + time);
                else if(status.equals("5"))
                    mDetectionAdapter.addItem(indicator + ":whining", "time : " + time);
                else if(status.equals("6"))
                    mDetectionAdapter.addItem(indicator + ":howling", "time : " + time);
            } else if(indicator.equals("1")) { // motion
                if(status.equals("0"))
                    mDetectionAdapter.addItem(indicator + ":unknown", "time : " + time);
                else if(status.equals("1"))
                    mDetectionAdapter.addItem(indicator + ":sitting", "time : " + time);
                else if(status.equals("2"))
                    mDetectionAdapter.addItem(indicator + ":walking", "time : " + time);
                else if(status.equals("3"))
                    mDetectionAdapter.addItem(indicator + ":running", "time : " + time);
                else if(status.equals("4"))
                    mDetectionAdapter.addItem(indicator + ":lying", "time : " + time);
                else if(status.equals("5"))
                    mDetectionAdapter.addItem(indicator + ":ddong)", "time : " + time);
            }
            mDetectionAdapter.notifyDataSetChanged();
            mHistoryLog.add(status);

            // check if the classification is for motion or sound
            if(indicator.equals("0")) { // sound
                // set the result image as status change
                // later lets make an array of picture and get image with matched index
                mDetectionTextViewSound.setText("Result: " + status);

                if(status.equals("0"))
                    result_img_sound.setImageResource(R.drawable.pme_pet_unknown);
                else if(status.equals("1"))
                    result_img_sound.setImageResource(R.drawable.pme_pet_barking);
                else if(status.equals("2"))
                    result_img_sound.setImageResource(R.drawable.pme_pet_growling);
                else if(status.equals("3"))
                    result_img_sound.setImageResource(R.drawable.pme_pet_drinking);
                else if(status.equals("4"))
                    result_img_sound.setImageResource(R.drawable.pme_pet_eating);
                else if(status.equals("5"))
                    result_img_sound.setImageResource(R.drawable.pme_pet_whining);
                else if(status.equals("6"))
                    result_img_sound.setImageResource(R.drawable.pme_pet_howling);

            } else if(indicator.equals("1")) { // motion
                Log.d("PME_INDICATOR ", indicator);
                // set the result image as status change
                // later lets make an array of picture and get image with matched index
                mDetectionTextViewMotion.setText("Result: " + status);

                if(status.equals("0"))
                    result_img_motion.setImageResource(R.drawable.pme_pet_unknown);
                else if(status.equals("1"))
                    result_img_motion.setImageResource(R.drawable.pme_pet_sitting);
                else if(status.equals("2"))
                    result_img_motion.setImageResource(R.drawable.pme_pet_walking);
                else if(status.equals("3"))
                    result_img_motion.setImageResource(R.drawable.pme_pet_running);
                else if(status.equals("4"))
                    result_img_motion.setImageResource(R.drawable.pme_pet_lying);
                else if(status.equals("5"))
                    result_img_motion.setImageResource(R.drawable.pme_pet_ddong);
            }


            // set history list view focus to the bottom
            // (set selection to the last element)
            mHistoryView.setSelection(mDetectionAdapter.getCount() - 1);

            Log.d("PME_FRAGMENT_history: ", mHistoryLog.toString() + " " + time);
        }

        /**
         * Feature vector has four number
         * (Length, X, Y, Z)
         *
         * @param bluetoothDevice
         * @param len
         * @param x
         * @param y
         * @param z
         */

        @Override
        public void onFeatureVectorValueChangedEvent(BluetoothDevice bluetoothDevice, String len, String x, String y, String z) {
            if (len != null) {

                time = new SimpleDateFormat("HH:mm:ss").format(new Date(System.currentTimeMillis()));
                mFeatureVectorAdapter.addItem(len + "  " + x + "  " + y + "  " + z, "time : " + time);
                mFeatureVectorAdapter.notifyDataSetChanged();

                time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(System.currentTimeMillis()));
                mFeatureLog.add(x + "," + y + "," + z + "," + time);
                Log.d("PME_FRAGMENT_feature: ", mFeatureLog.toString());

                // set history list view focus to the bottom
                // (set selection to the last element)
                mFeatureVectorView.setSelection(mFeatureVectorAdapter.getCount() - 1);
            }
        }

        @Override
        public void onResultVectorValueChangedEvent(BluetoothDevice bluetoothDevice, String R_0, String R_1, String R_2, String R_3, String R_4, String R_5, String R_6, String R_7, String R_8, String R_9, String R_10, String R_11, String R_12, String R_13, String R_14, String R_15) {

        }

        @Override
        public void onAccelerometerValueChangedEvent(BluetoothDevice bluetoothDevice, float accelerometerX, float accelerometerY, float accelerometerZ) {

        }

        @Override
        public void onAcelGyroValueChangedEvent(BluetoothDevice bluetoothDevice, float ax, float ay, float az, float gx, float gy, float gz) {

        }

        @Override
        public void onCompassValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {

        }

        @Override
        public void onEulerAngleChangedEvent(BluetoothDevice bluetoothDevice, float roll, float pitch, float yaw) {

        }

        @Override
        public void onRotationMatixValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] matrix) {

        }

        @Override
        public void onHeadingValueChangedEvent(BluetoothDevice bluetoothDevice, float heading) {

        }

        @Override
        public void onGravityVectorChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z, float x2, float y2, float z2) {

        }

        @Override
        public void onSpeakerStatusValueChangedEvent(BluetoothDevice bluetoothDevice, int status) {

        }

        @Override
        public void onMicrophoneValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] data) {

        }

        @Override
        public void connectionCheck() {

        }
    };

    public static PetFragment newInstance(final BluetoothDevice device) {
        PetFragment fragment = new PetFragment();
        final Bundle args = new Bundle();
        args.putParcelable(Utils.CURRENT_DEVICE, device);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDevice = getArguments().getParcelable(Utils.CURRENT_DEVICE);

        }
        client = new OkHttpClient();
        JSON = MediaType.parse("application/json; charset=utf-8");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mThingySdkManager = ThingySdkManager.getInstance();

        View rootView = inflater.inflate(R.layout.fragment_pet, container, false);

        final Toolbar mDetectionToolbar = rootView.findViewById(R.id.pet_detect_toolbar);

        final Toolbar mHistoryToolbar = rootView.findViewById(R.id.pet_history_toolbar);
        mHistoryToolbar.inflateMenu(R.menu.menu_submit);
        mHistoryToolbar.inflateMenu(R.menu.menu_save);

        final Toolbar mFeatureToolbar = rootView.findViewById(R.id.pet_featurevector_toolbar);

        mDetectionSwitch = rootView.findViewById(R.id.switch_detect);
        mDetectionTextViewMotion = rootView.findViewById(R.id.detect_text_motion);
        mDetectionTextViewSound = rootView.findViewById(R.id.detect_text_sound);

        mDatabaseHelper = new DatabaseHelper(getActivity());

        mHistoryView = rootView.findViewById(R.id.history_list);
        mDetectionAdapter = new ListViewAdapter(getActivity());
        mHistoryView.setAdapter(mDetectionAdapter);

        mFeatureVectorView = rootView.findViewById(R.id.featurevector_list);
        mFeatureVectorAdapter = new ListViewAdapter(getActivity());
        mFeatureVectorView.setAdapter(mFeatureVectorAdapter);

        // Pet result
        result_img_motion = rootView.findViewById(R.id.result_img_motion);
        result_img_sound = rootView.findViewById(R.id.result_img_sound);

        if (mDetectionToolbar != null) {
            mDetectionToolbar.setTitle(R.string.result_title);

            mDetectionSwitch.setChecked(mDatabaseHelper.getNotificationsState(mDevice.getAddress(),
                    DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_CLASSIFICATION));

            mDetectionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (mThingySdkManager.isConnected(mDevice)) {
                        Log.d("PET FRAGEMENT: ", "switch on for " + mDevice.getAddress());

                        mDetectionSwitch.setChecked(isChecked);

                        mThingySdkManager.enableClassificationNotifications(mDevice, isChecked);
                        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), isChecked,
                                DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_CLASSIFICATION);

                        mThingySdkManager.enableFeatureVectorNotifications(mDevice, isChecked);
                        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), isChecked,
                                DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_FEATUREVECTOR);
                    } else {
                        Log.d("PET FRAGEMENT: ", "switch off for " + mDevice.getAddress());
                        mDetectionSwitch.setChecked(!isChecked);
                    }
                }
            });
        }

        if (mHistoryToolbar != null) {
            mHistoryToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    final int id = item.getItemId();
                    switch (id) {
                        // send to database
                        case R.id.action_send:
                            try {
                                makePostRequest();
                                mDetectionAdapter.clear();
                                mFeatureVectorAdapter.clear();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        // save to local directory
                        case R.id.action_save:
                            if (mFeatureLog.size() > 0) {

                                File tempDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MHE_FEATURE");

                                if (!tempDir.exists())
                                    tempDir.mkdirs();

                                time = new SimpleDateFormat("/HH:mm:ss_").format(new Date(System.currentTimeMillis()));

                                try {
                                    fw = new FileWriter(tempDir + time + "Feature.csv");
                                    bw = new BufferedWriter(fw);
                                    bw.write("Activity, Vector0, Vector1, Vector2, Time\n");
                                    for (int i = 0; i < mFeatureLog.size(); i++)
                                        bw.write(mHistoryLog.get(i) + "," + mFeatureLog.get(i) + "\n");
                                    bw.close();
                                    fw.close();

                                    mDetectionTextViewMotion.setText(" ");
                                    mDetectionTextViewSound.setText(" ");

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                    }
                    return true;
                }
            });
        }

        if (mFeatureToolbar != null) {

        }

        ThingyListenerHelper.registerThingyListener(getContext(), mThingyListener, mDevice);

        return rootView;
    }

    public void makePostRequest() throws IOException {
        PostTask task = new PostTask();
        task.execute();
    }



    public class PostTask extends AsyncTask<String, Void, String> {

        private Exception exception;

        protected String doInBackground(String... urls) {
            try {
                JSONObject obj = new JSONObject();
                JSONObject dev = new JSONObject();
                dev.put("mac_address", mDevice.getAddress());
                obj.put("device", dev);

                JSONArray jArray = new JSONArray();

                for (int i = 0; i < mFeatureLog.size(); i++) {
                    JSONObject sObject = new JSONObject();
                    //배열 내에 들어갈 json

                    String s = mFeatureLog.get(i);
                    String[] array = s.split(",");

                    sObject.put("activity", mHistoryLog.get(i));
                    sObject.put("vec0",array[0]);
                    sObject.put("vec1", array[1]);
                    sObject.put("vec2", array[2]);
                    sObject.put("time", array[3]);

                    jArray.put(sObject);
                }

                obj.put("pet", jArray);

                // post the json object to EC2 server
                String getResponse = post("http://13.209.25.63/rest/pet_save/", obj);
                Log.v("PET_RESPONSE: ", getResponse);
                return getResponse;
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute(String getResponse) {
            mFeatureLog = new ArrayList<>();
            mHistoryLog = new ArrayList<>();
            System.out.println(getResponse);
        }

        /**
         * post the json object to the following url
         * @param url
         * @param json
         * @return
         * @throws IOException
         */

        private String post(String url, JSONObject json) throws IOException {
            RequestBody body = RequestBody.create(JSON, json.toString());
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            Log.d("PET request response: ", response.body().string());
            return response.body().string();
        }

    }


    private class ViewHolder {
        public TextView number;
        public TextView time;
    }

    private class ListViewAdapter extends BaseAdapter {
        private Context mContext = null;
        private ArrayList<ListData> mListData = new ArrayList<ListData>();

        public ListViewAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return mListData.size();
        }

        @Override
        public Object getItem(int position) {
            return mListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void addItem(String number, String time) {
            ListData addInfo = null;
            addInfo = new ListData();
            addInfo.number = number;
            addInfo.time = time;

            mListData.add(addInfo);
        }

        public void remove(int position) {
            mListData.remove(position);
            dataChange();
        }

        public void clear() {
            mListData.clear();
            dataChange();
        }


        public void dataChange() {
            mFeatureVectorAdapter.notifyDataSetChanged();
            mDetectionAdapter.notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item_pet, null);

                holder.number = (TextView) convertView.findViewById(R.id.number);
                holder.time = (TextView) convertView.findViewById(R.id.time);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ListData mData = mListData.get(position);

            holder.number.setText(mData.number);
            holder.time.setText(mData.time);

            return convertView;
        }

        private class ListData {
            public String number;
            public String time;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ThingyListenerHelper.unregisterThingyListener(getContext(), mThingyListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDeviceSelected(BluetoothDevice device, String name) {

    }

    @Override
    public void onNothingSelected() {

    }
}
