package no.nordicsemi.android.nrfthingy;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

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

import static no.nordicsemi.android.nrfthingy.common.Utils.REQUEST_ENABLE_BT;

/**
 * Created by dongsung on 2018-02-01.
 */

public class PMEFragment extends Fragment implements ScannerFragmentListener {

    private TextView mDetectionTextView;

    private BluetoothDevice mDevice;

    private ThingySdkManager mThingySdkManager = null;
    private DatabaseHelper mDatabaseHelper;
    private Switch mDetectionSwitch;

    private ListView mHistoryView;
    private ListView mFeatureVectorView;
    private ListView mResultVectorView;

    private ListViewAdapter mDetectionAdapter;
    private ListViewAdapter mFeatureVectorAdapter;
    private ListViewAdapter mResultVectorAdapter;

    private FileWriter fw;
    private BufferedWriter bw;

    private ArrayList<String> mHistoryLog = new ArrayList<String>();
    private ArrayList<String> mFeatureLog = new ArrayList<String>();
    private static ArrayList<String> mResultLog = new ArrayList<String>();

    public static String time;

    public boolean mIsFragmentAttached = false;
    public CheckReceive mCheckReceive = new CheckReceive();
    public CheckConnection mCheckConnection = new CheckConnection();
    public boolean isRun = false;
    OkHttpClient client;
    MediaType JSON;

    // PME result
    private ImageView result_img;

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
        public void onKnowledgePackValueChangedEvent(BluetoothDevice bluetoothDevice, String status) {

            mDetectionTextView.setText("Result: "+status);
            time = new SimpleDateFormat("HH:mm:ss").format(new Date(System.currentTimeMillis()));
            mDetectionAdapter.addItem(status, "time : " + time);
            mDetectionAdapter.notifyDataSetChanged();
            mHistoryLog.add(status);

            // set the result image as status change
            // later lets make an array of picture and get image with matched index
            if(status.equals("0"))
                result_img.setImageResource(R.drawable.pme_unknown);
            else if(status.equals("1"))
                result_img.setImageResource(R.drawable.pme_sleep);
            else if(status.equals("2"))
                result_img.setImageResource(R.drawable.pme_study);
            else if(status.equals("3"))
                result_img.setImageResource(R.drawable.pme_phone);
            else if(status.equals("4"))
                result_img.setImageResource(R.drawable.pme_eat);
            else if(status.equals("5"))
                result_img.setImageResource(R.drawable.pme_walk);

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
        public void onResultVectorValueChangedEvent(BluetoothDevice bluetoothDevice, String len, String R_0, String R_1, String R_2, String R_3
                , String R_4, String R_5, String R_6, String R_7, String R_8, String R_9, String R_10, String R_11, String R_12, String R_13
                , String R_14, String R_15) {
            if (R_1 != null) {

                time = new SimpleDateFormat("HH:mm:ss").format(new Date(System.currentTimeMillis()));
                mResultVectorAdapter.addResult(len + "  " + R_0 + "  " + R_1 + "  " + R_3 + "  " + R_4 + "  " + R_5 + "  " + R_6 + "  " + R_7
                                + "  " + R_8 + "  " + R_9 + "  " + R_10 + "  " + R_11 + "  " + R_12 + "  " + R_13 + "  " + R_14 + "  " + R_15
                        , "time : " + time);
                mResultVectorAdapter.notifyDataSetChanged();

                time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(System.currentTimeMillis()));
                mResultLog.add(R_0 + "," + R_1 + "," + R_3 + "," + R_4 + "," + R_5 + "," + R_6 + "," + R_7
                        + "," + R_8 + "," + R_9 + "," + R_10 + "," + R_11 + "," + R_12 + "," + R_13 + "," + R_14 + "," + R_15 + "," + time);
                Log.d("PME_result_change : ", mResultLog.toString());

                // set history list view focus to the bottom
                // (set selection to the last element)
                mResultVectorView.setSelection(mResultVectorAdapter.getCount() - 1);


                if(!isRun && mResultLog.size() == 1)
                    mCheckReceive.execute();
            }
            else {
                mThingySdkManager.enableResultVectorNotifications(mDevice, false);
//                saveData();
            }
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
            Log.d("PME connection check ", "start thread start");
            mCheckConnection.execute();
        }
    };

    public static PMEFragment newInstance(final BluetoothDevice device) {
        PMEFragment fragment = new PMEFragment();
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

        View rootView = inflater.inflate(R.layout.fragment_pme, container, false);

        final Toolbar mDetectionToolbar = rootView.findViewById(R.id.pme_detect_toolbar);

        final Toolbar mHistoryToolbar = rootView.findViewById(R.id.pme_history_toolbar);
        mHistoryToolbar.inflateMenu(R.menu.menu_submit);
        mHistoryToolbar.inflateMenu(R.menu.menu_save);

        final Toolbar mFeatureToolbar = rootView.findViewById(R.id.pme_featurevector_toolbar);
        final Toolbar mResultToolbar = rootView.findViewById(R.id.pme_resultvector_toolbar);

        mDetectionSwitch = rootView.findViewById(R.id.switch_detect);
        mDetectionTextView = rootView.findViewById(R.id.detect_text);

        mDatabaseHelper = new DatabaseHelper(getActivity());

        mHistoryView = rootView.findViewById(R.id.history_list);
        mDetectionAdapter = new ListViewAdapter(getActivity());
        mHistoryView.setAdapter(mDetectionAdapter);

        mFeatureVectorView = rootView.findViewById(R.id.featurevector_list);
        mFeatureVectorAdapter = new ListViewAdapter(getActivity());
        mFeatureVectorView.setAdapter(mFeatureVectorAdapter);

        mResultVectorView = rootView.findViewById(R.id.resultvector_list);
        mResultVectorAdapter = new ListViewAdapter(getActivity());
        mResultVectorView.setAdapter(mResultVectorAdapter);

        // PME result
        result_img = rootView.findViewById(R.id.result_img);

        if (mDetectionToolbar != null) {
            mDetectionToolbar.setTitle(R.string.result_title);

            mDetectionSwitch.setChecked(mDatabaseHelper.getNotificationsState(mDevice.getAddress(),
                    DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_CLASSIFICATION));

            mDetectionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (mThingySdkManager.isConnected(mDevice)) {
                        Log.d("PME FRAGEMENT: ", "switch on for " + mDevice.getAddress());

                        mDetectionSwitch.setChecked(isChecked);

                        mThingySdkManager.enableClassificationNotifications(mDevice, isChecked);
                        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), isChecked,
                                DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_CLASSIFICATION);

                        mThingySdkManager.enableFeatureVectorNotifications(mDevice, isChecked);
                        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), isChecked,
                                DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_FEATUREVECTOR);

                        //resultDB
//                        mThingySdkManager.enableResultVectorNotifications(mDevice, isChecked);
//                        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), isChecked,
//                                DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_RESULTVECTOR);
                    } else {
                        Log.d("PME FRAGEMENT: ", "switch off for " + mDevice.getAddress());
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
                                Log.d("PATH", Environment.getExternalStorageDirectory().getAbsolutePath());

                                if (!tempDir.exists())
                                    tempDir.mkdirs();

                                time = new SimpleDateFormat("/HH-mm-ss_").format(new Date(System.currentTimeMillis()));

                                try {
                                    fw = new FileWriter(tempDir + time + "Feature.csv");
                                    bw = new BufferedWriter(fw);
                                    bw.write("Activity, Vector0, Vector1, Vector2, Time\n");
                                    for (int i = 0; i < mFeatureLog.size(); i++)
                                        bw.write(mHistoryLog.get(i) + "," + mFeatureLog.get(i) + "\n");
                                    bw.close();
                                    fw.close();

                                    mDetectionTextView.setText(" ");

                                } catch (IOException e) {

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

    public ArrayList<String> getmResultLog(){
        return mResultLog;
    }

    public void saveData() {
        File tempDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MHE_RESULT");
        Log.d("PATH", Environment.getExternalStorageDirectory().getAbsolutePath());
//        Log.e("CW", "Method called on the saveData", new Exception("STACK TRACE"));

        if (!tempDir.exists())
            tempDir.mkdirs();
        Log.d("Saving_Data", "Data save start" + " Log size : " + mResultLog.size());
        time = new SimpleDateFormat("/HH-mm-ss_").format(new Date(System.currentTimeMillis()));

        try {
            fw = new FileWriter(tempDir + time + "Result.csv");
            bw = new BufferedWriter(fw);
            bw.write("MAC, ID, Activity, Vector0, Vector1, Vector2, Vector3, Vector4, Vector5, Vector6, Vector7, Vector8, Vector10, Vector11, Vector12, Vector13, Time\n");
            for (int i = 0; i < mResultLog.size(); i++)
                bw.write(mDevice.getAddress() + "," + mDevice.getName() + "," + mResultLog.get(i) + "\n");
            bw.close();
            fw.close();

//            mDetectionTextView.setText(" ");

        } catch (IOException e) {

        }
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
                JSONArray jArray = new JSONArray();

                for (int i = 0; i < mFeatureLog.size(); i++) {
                    JSONObject sObject = new JSONObject();
                    //배열 내에 들어갈 json

                    String s = mFeatureLog.get(i);
                    String[] array = s.split(",");

                    sObject.put("Mac",mDevice.getAddress());
                    sObject.put("Activity", mHistoryLog.get(i));
                    sObject.put("Vector0",array[0]);
                    sObject.put("Vector1", array[1]);
                    sObject.put("Vector2", array[2]);
                    sObject.put("Time", array[3]);

                    jArray.put(sObject);
                }

                obj.put("PME", jArray);

                // post the json object to EC2 server
                String getResponse = post("http://ec2-13-125-160-227.ap-northeast-2.compute.amazonaws.com/pme/set", obj);
                return getResponse;
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute(String getResponse) {
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

        public void addResult(String result, String time) {
            ListData addInfo = null;
            addInfo = new ListData();
            addInfo.number = result;
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

        public void resultChange() {
            mResultVectorAdapter.notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item_pme, null);

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
        mIsFragmentAttached = true;
        /*
        if (context instanceof EnvironmentServiceListener) {
            mListener = (EnvironmentServiceListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement CloudFragmentListener");
        }
        */
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mIsFragmentAttached = false;
//        mListener = null;
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
    class CheckReceive extends AsyncTask<Integer, Integer, Integer> {
        @Override
        protected Integer doInBackground(Integer... integers) {
            int size = 0;
            isRun = true;

            try {
                while(true)
                {
                    size = mResultLog.size();
                    Thread.sleep(500);
                    if(size == mResultLog.size()) {
                        ThingySdkManager mThingySdkManager = ThingySdkManager.getInstance();
                        mThingySdkManager.enableResultVectorNotifications(mDevice, false);
                        saveData();
                        isRun = false;

                        Log.e("CW","uploadBT");
                        String tempC = new String();
                        ContentValues contentValues = new ContentValues();

                        FileReader fr;
                        BufferedReader br;
                        String sCurrentLine = new String();

                        List<String> saveBuffer = new ArrayList<String>();
                        File tempDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MHE_RESULT");
                        if (!tempDir.exists())
                            tempDir.mkdirs();
                        try {
                            fr = new FileReader(tempDir +time+ "Result.csv");
                            br = new BufferedReader(fr);

                            while((sCurrentLine= br.readLine()) != null){
                                saveBuffer.add(sCurrentLine);
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        saveBuffer.remove(0);

                        contentValues.put("a", saveBuffer.toString());

                        String url = "http://13.230.57.228:8080/create";

                        // AsyncTask를 통해 HttpURLConnection 수행.
                        NetworkTask networkTask = new NetworkTask(url,  contentValues);
                        networkTask.execute();

                        break;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class CheckConnection extends AsyncTask<Integer, Integer, Integer> {
        @Override
        protected Integer doInBackground(Integer... integers) {

            isRun = true;
            try {
                int size = mResultLog.size();
                Thread.sleep(500);
                if (mResultLog.size() == size) {
                    ThingySdkManager mThingySdkManager = ThingySdkManager.getInstance();
                    mThingySdkManager.enableResultVectorNotifications(mDevice, false);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class NetworkTask extends AsyncTask<Void, Void, String> {

        private String url;
        private ContentValues values;

        public NetworkTask(String url, ContentValues values) {

            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... params) {
            Log.e("CW","doInbackground" + values);
            String result; // 요청 결과를 저장할 변수.
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            result = requestHttpURLConnection.request(url, values); // 해당 URL로 부터 결과물을 얻어온다.

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("CW", "onPostExecute");
        }
    }
}
