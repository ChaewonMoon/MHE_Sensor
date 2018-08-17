package no.nordicsemi.android.nrfthingy;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import java.util.ArrayList;

import no.nordicsemi.android.nrfthingy.common.Utils;
import no.nordicsemi.android.nrfthingy.database.DatabaseContract;
import no.nordicsemi.android.nrfthingy.database.DatabaseHelper;
import no.nordicsemi.android.thingylib.ThingyListener;
import no.nordicsemi.android.thingylib.ThingyListenerHelper;
import no.nordicsemi.android.thingylib.ThingySdkManager;
import okhttp3.MediaType;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FusionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FusionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FusionFragment extends Fragment {

    private ListViewAdapter mFusionAdapter;
    private ArrayList<BluetoothDevice> mBleList;
    private ListView mFusionListView;
    private OnFragmentInteractionListener mListener;
    private ThingySdkManager mThingySdkManager = null;
    private DatabaseHelper mDatabaseHelper;
    private Button fusionStart;
    private ImageView mFusionResult;

    private ImageView mP1_cross;
    private ImageView mP1_round;
    private ImageView mP1_touch;
    private ImageView mP2_cross;
    private ImageView mP2_round;
    private ImageView mP2_touch;

    private boolean p1_round = false;
    private boolean p1_cross = false;
    private boolean p1_touch = false;
    private boolean p2_round = false;
    private boolean p2_cross = false;
    private boolean p2_touch = false;

    public CheckTime mCheckTime;

    MediaType JSON;

    public FusionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FusionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FusionFragment newInstance(ArrayList<BluetoothDevice> mDeviceList) {
        FusionFragment fragment = new FusionFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(Utils.CURRENT_DEVICE_LIST, mDeviceList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mBleList = getArguments().getParcelableArrayList(Utils.CURRENT_DEVICE_LIST);
        }
        JSON = MediaType.parse("application/json; charset=utf-8");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mThingySdkManager = ThingySdkManager.getInstance();

        View rootView = inflater.inflate(R.layout.fragment_fusion, container, false);

        mFusionListView = rootView.findViewById(R.id.fusion_list);
        mFusionAdapter = new ListViewAdapter();
        mFusionListView.setAdapter(mFusionAdapter);

        mDatabaseHelper = new DatabaseHelper(getActivity());

        for(int i = 0; i < mBleList.size(); i++) {
            mFusionAdapter.addDevice(mBleList.get(i));
            ThingyListenerHelper.registerThingyListener(getContext(), mThingyListener, mBleList.get(i));
        }

        mFusionResult = (ImageView) rootView.findViewById(R.id.fusion_result);
        fusionStart = (Button) rootView.findViewById(R.id.fusion_start);
        mP1_cross = (ImageView) rootView.findViewById(R.id.p1_cross);
        mP1_round = (ImageView) rootView.findViewById(R.id.p1_round);
        mP1_touch = (ImageView) rootView.findViewById(R.id.p1_touch);
        mP2_cross = (ImageView) rootView.findViewById(R.id.p2_cross);
        mP2_round = (ImageView) rootView.findViewById(R.id.p2_round);
        mP2_touch = (ImageView) rootView.findViewById(R.id.p2_touch);

        fusionStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBleList.size() == 2)
                    mFusionAdapter.setStartImg();
                mCheckTime = new CheckTime();
                mCheckTime.execute();
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        */
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private class ListViewAdapter extends BaseAdapter {
        private ArrayList<ListData> mListData;
        private LayoutInflater inflater;
        private ArrayList<ViewHolder> mHolderList;

        public ListViewAdapter() {
            super();
            mListData = new ArrayList<ListData>();
            mHolderList = new ArrayList<>();
            inflater = FusionFragment.this.getLayoutInflater();
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

        public void addDevice(BluetoothDevice device) {
            ListData addInfo = new ListData();
            addInfo.mDevice = device;
            if(!mListData.contains(addInfo)) {
                Log.d("FusionFragment : ", device.getAddress());
                mListData.add(addInfo);
            }
            dataChange();
        }

        public void setStartImg() {
            mHolderList.get(0).mResultImage.setImageResource(R.drawable.p1_start);
            mHolderList.get(1).mResultImage.setImageResource(R.drawable.p2_start);
        }

        public void changeImg(BluetoothDevice device, String status) {
            Log.d("FusionFragment : ", String.valueOf(mBleList.indexOf(device)));
            if(status.equals("1"))
                mHolderList.get(mBleList.indexOf(device)).mResultImage.setImageResource(R.drawable.p1_cross);
            else if(status.equals("2"))
                mHolderList.get(mBleList.indexOf(device)).mResultImage.setImageResource(R.drawable.p1_round);
            else if(status.equals("3"))
                mHolderList.get(mBleList.indexOf(device)).mResultImage.setImageResource(R.drawable.p1_touch);
            else if(status.equals("4"))
                mHolderList.get(mBleList.indexOf(device)).mResultImage.setImageResource(R.drawable.p2_cross);
            else if(status.equals("5"))
                mHolderList.get(mBleList.indexOf(device)).mResultImage.setImageResource(R.drawable.p2_round);
            else if(status.equals("6"))
                mHolderList.get(mBleList.indexOf(device)).mResultImage.setImageResource(R.drawable.p2_touch);
            else if(status.equals("SUCCESS"))
                mHolderList.get(mBleList.indexOf(device)).mResultImage.setImageResource(R.drawable.fusion_success_mov);
        }

        public void remove(BluetoothDevice device) {
            mListData.remove(mListData.indexOf(device));
            dataChange();
        }

        public void clear() {
            mListData.clear();
            dataChange();
        }

        public void dataChange() {
            mFusionAdapter.notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final FusionFragment.ViewHolder holder;
            if (convertView == null) {
                Log.d("ListView : ", mListData.get(position).mDevice.getAddress());
                holder = new FusionFragment.ViewHolder();

                convertView = inflater.inflate(R.layout.list_result_pme, null);

                holder.mDetectionTextView = (TextView) convertView.findViewById(R.id.result_list_title);
                holder.mDetectionSwitch = (Switch) convertView.findViewById(R.id.switch_result_detect);
                holder.mResultToolbar = (Toolbar) convertView.findViewById(R.id.pme_result_detect_toolbar);
                holder.mResultImage = (ImageView) convertView.findViewById(R.id.result_img_list);
                holder.index = position;
                mHolderList.add(holder);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if(mListData.get(position).mDevice.getAddress() != null)
                holder.mDetectionTextView.setText(mListData.get(position).mDevice.getAddress());
            else
                holder.mDetectionTextView.setText("unKnown");

            if (holder.mResultToolbar != null) {
//            viewHolder.mResultToolbar.setTitle("result_title");
                holder.mDetectionSwitch.setChecked(mDatabaseHelper.getNotificationsState(mListData.get(holder.index).mDevice.getAddress(),
                        DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_CLASSIFICATION));

                holder.mDetectionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
//                        mBluetoothLeService.setCharacteristicNotification(mGattList.get(i), mBluetoothLeService.mClassificationCharacteristic, isChecked);
                        Log.d("FusionFragment", mListData.get(holder.index).mDevice.getAddress() + "// clicked // " + holder.index);
                        mThingySdkManager.enableClassificationNotifications(mListData.get(holder.index).mDevice, isChecked);
                        mDatabaseHelper.updateNotificationsState(mListData.get(holder.index).mDevice.getAddress(), isChecked,
                                DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_CLASSIFICATION);
                    }
                });
            }
            else {
                Log.d("FusionFragment", "button is null");
            }

            return convertView;
        }

        private class ListData {
            public BluetoothDevice mDevice;

        }
    }

    private class ViewHolder {
        public Integer index;
        public Toolbar mResultToolbar;
        public Switch mDetectionSwitch;
        public TextView mDetectionTextView;
        public ImageView mResultImage;
    }

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

        @Override
        public void onAccelerometerValueChangedEvent(BluetoothDevice bluetoothDevice, float accelerometerX, float accelerometerY, float accelerometerZ) {

        }

        @Override
        public void onKnowledgePackValueChangedEvent(BluetoothDevice bluetoothDevice, String status, String indicator, String cla4) {
            Log.d("FusionFragment : ", bluetoothDevice.getAddress() + " // " + status);
            if(!status.equals("0")) {
                if(bluetoothDevice.equals(mBleList.get(0))) {
                    if(!p1_round && !p1_cross && !p1_touch && status.equals("2")) {
                        p1_round = true;
                        MediaPlayer mp = MediaPlayer.create(FusionFragment.this.getContext(), R.raw.fu);
                        mp.start();
                        mP1_round.setVisibility(View.VISIBLE);
                    }
                    else if(p1_round && !p1_cross && !p1_touch && status.equals("1")) {
                        p1_cross = true;
                        MediaPlayer mp = MediaPlayer.create(FusionFragment.this.getContext(), R.raw.sion);
                        mp.start();
                        mP1_cross.setVisibility(View.VISIBLE);
                    }
                    else if(p1_round && p1_cross && !p1_touch && status.equals("3")) {
                        p1_touch = true;
                        MediaPlayer mp = MediaPlayer.create(FusionFragment.this.getContext(), R.raw.hap);
                        mp.start();
                        mP1_touch.setVisibility(View.VISIBLE);
                    }

                    if(status.equals("1") || status.equals("2") || status.equals("3"))
                        mFusionAdapter.changeImg(bluetoothDevice, status);
                }
                else if(bluetoothDevice.equals(mBleList.get(1))) {
                    if(!p2_round && !p2_cross && !p2_touch && status.equals("5")) {
                        p2_round = true;
                        MediaPlayer mp = MediaPlayer.create(FusionFragment.this.getContext(), R.raw.fu);
                        mp.start();
                        mP2_round.setVisibility(View.VISIBLE);
                    }
                    else if(p2_round && !p2_cross && !p2_touch && status.equals("4")) {
                        p2_cross = true;
                        MediaPlayer mp = MediaPlayer.create(FusionFragment.this.getContext(), R.raw.sion);
                        mp.start();
                        mP2_cross.setVisibility(View.VISIBLE);
                    }
                    else if(p2_round && p2_cross && !p2_touch && status.equals("6")) {
                        p2_touch = true;
                        MediaPlayer mp = MediaPlayer.create(FusionFragment.this.getContext(), R.raw.hap);
                        mp.start();
                        mP2_touch.setVisibility(View.VISIBLE);
                    }

                    if(status.equals("4") || status.equals("5") || status.equals("6"))
                        mFusionAdapter.changeImg(bluetoothDevice, status);
                }
            }
        }

        @Override
        public void onFeatureVectorValueChangedEvent(BluetoothDevice bluetoothDevice, String len, String x, String y, String z) {

        }

        @Override
        public void onResultVectorValueChangedEvent(BluetoothDevice bluetoothDevice, String R_0, String R_1, String R_2, String R_3, String R_4, String R_5, String R_6, String R_7, String R_8, String R_9, String R_10, String R_11, String R_12, String R_13, String R_14, String R_15) {

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

    class CheckTime extends AsyncTask<Integer, String, Integer> {
        @Override
        protected Integer doInBackground(Integer... integers) {
            publishProgress("INVISIBLE");
            publishProgress("3");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            publishProgress("2");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            publishProgress("1");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d("FusionFragment", "UUID on!");
            if(mBleList.size() == 2) {
                mThingySdkManager.enableClassificationNotifications(mBleList.get(0), true);
                mDatabaseHelper.updateNotificationsState(mBleList.get(0).getAddress(), true,
                        DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_CLASSIFICATION);
                mThingySdkManager.enableClassificationNotifications(mBleList.get(1), true);
                mDatabaseHelper.updateNotificationsState(mBleList.get(1).getAddress(), true,
                        DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_CLASSIFICATION);
            }
            publishProgress("0");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(mBleList.size() == 2) {
                mThingySdkManager.enableClassificationNotifications(mBleList.get(0), false);
                mDatabaseHelper.updateNotificationsState(mBleList.get(0).getAddress(), false,
                        DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_CLASSIFICATION);
                mThingySdkManager.enableClassificationNotifications(mBleList.get(1), false);
                mDatabaseHelper.updateNotificationsState(mBleList.get(1).getAddress(), false,
                        DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_CLASSIFICATION);
            }

            /*
            p1_cross = true;
            p1_round = true;
            p1_touch = true;
            p2_cross = true;
            p2_round = true;
            p2_touch = true;
            */
            publishProgress("GIF");
            try {
                Thread.sleep(6500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(p1_cross && p1_round && p1_touch && p2_cross && p2_round && p2_touch) {
                publishProgress("SUCCESS");
            }
            else {
                publishProgress("FAIL");
            }

            p1_cross = false;
            p1_round = false;
            p1_touch = false;
            p2_cross = false;
            p2_round = false;
            p2_touch = false;
            return null;
        }

        @Override
        protected void onProgressUpdate(String... str) {
            Log.d("FusionFragment", "UPDATE");
            if(str[0].equals("3")) {
                Log.d("FusionFragment", "3");
                mFusionResult.setImageResource(R.drawable.three);
            }
            else if(str[0].equals("2"))
                mFusionResult.setImageResource(R.drawable.two);
            else if(str[0].equals("1"))
                mFusionResult.setImageResource(R.drawable.one);
            else if(str[0].equals("0"))
                mFusionResult.setImageResource(R.drawable.start);
            else if(str[0].equals("GIF")) {

                GlideDrawableImageViewTarget gifImage = new GlideDrawableImageViewTarget(mFusionResult);
                Glide.with(FusionFragment.this).load(R.drawable.fusion_success_mov).into(gifImage);

                //mFusionResult.setImageResource(R.drawable.blank);
            }
            else if(str[0].equals("SUCCESS"))
                mFusionResult.setImageResource(R.drawable.fusion_success);
            else if(str[0].equals("FAIL"))
                mFusionResult.setImageResource(R.drawable.fusion_fail);
            else if(str[0].equals("INVISIBLE")) {
                mP1_round.setVisibility(View.INVISIBLE);
                mP2_round.setVisibility(View.INVISIBLE);
                mP1_cross.setVisibility(View.INVISIBLE);
                mP2_cross.setVisibility(View.INVISIBLE);
                mP1_touch.setVisibility(View.INVISIBLE);
                mP2_touch.setVisibility(View.INVISIBLE);
            }
        }
    }
}
