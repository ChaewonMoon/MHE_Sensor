package no.nordicsemi.android.nrfthingy;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

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
 * {@link HallaBongFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HallaBongFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HallaBongFragment extends Fragment {


    private ListViewAdapter mResultAdapter;
    private ArrayList<BluetoothDevice> mBleList;
    private ListView mResultListView;
    private OnFragmentInteractionListener mListener;
    private ThingySdkManager mThingySdkManager = null;
    private DatabaseHelper mDatabaseHelper;
    private int num = 0;

    MediaType JSON;

    public HallaBongFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ResultFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HallaBongFragment newInstance(ArrayList<BluetoothDevice> mDeviceList) {
        HallaBongFragment fragment = new HallaBongFragment();
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

        View rootView = inflater.inflate(R.layout.fragment_halla_bong, container, false);

        mResultListView = rootView.findViewById(R.id.hallabong_list);
        mResultAdapter = new ListViewAdapter();
        mResultListView.setAdapter(mResultAdapter);

        mDatabaseHelper = new DatabaseHelper(getActivity());

        for(int i = 0; i < mBleList.size(); i++) {
            mResultAdapter.addDevice(mBleList.get(i));
            ThingyListenerHelper.registerThingyListener(getContext(), mThingyListener, mBleList.get(i));
        }

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
            inflater = HallaBongFragment.this.getLayoutInflater();
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
                Log.d("HallaBongFragment : ", device.getAddress());
                mListData.add(addInfo);
            }
            dataChange();
        }

        public void changeImg(BluetoothDevice device, String status) {
            Log.d("HallaBongFragment : ", String.valueOf(mBleList.indexOf(device)));
            MediaPlayer mp = MediaPlayer.create(HallaBongFragment.this.getContext(), R.raw.daehanminkook);
            if(status.equals("0"))
                mHolderList.get(mBleList.indexOf(device)).mResultImage.setImageResource(R.drawable.pme_unknown);
            else if(status.equals("1"))
                mHolderList.get(mBleList.indexOf(device)).mResultImage.setImageResource(R.drawable.pme_sleep);
            else if(status.equals("2"))
                mHolderList.get(mBleList.indexOf(device)).mResultImage.setImageResource(R.drawable.pme_study);
            else if(status.equals("3"))
                mHolderList.get(mBleList.indexOf(device)).mResultImage.setImageResource(R.drawable.pme_phone);
            else if(status.equals("4"))
                mHolderList.get(mBleList.indexOf(device)).mResultImage.setImageResource(R.drawable.pme_eat);
            else if(status.equals("5")) {
                mHolderList.get(mBleList.indexOf(device)).mResultImage.setImageResource(R.drawable.pme_walk);
                //MediaPlayer mp = MediaPlayer.create(HallaBongFragment.this.getContext(), R.raw.clap);
                //mp.start();
            }
            else if(status.equals("6") && !mp.isPlaying()) {
                num++;
                mHolderList.get(mBleList.indexOf(device)).mResultImage.setImageResource(R.drawable.guitar);
                if(num > 5) {
                    mp.start();
                    num = 0;
                }
            }
            /*
            else if(status.equals("7"))
                mHolderList.get(mBleList.indexOf(device)).mResultImage.setImageResource(R.drawable.saxophone);
            else if(status.equals("8")) {
                mHolderList.get(mBleList.indexOf(device)).mResultImage.setImageResource(R.drawable.flute);
                MediaPlayer mp = MediaPlayer.create(HallaBongFragment.this.getContext(), R.raw.daehanminkook);
                mp.start();
            }
            else if(status.equals("9")) {
                mHolderList.get(mBleList.indexOf(device)).mResultImage.setImageResource(R.drawable.flute);
                MediaPlayer mp = MediaPlayer.create(HallaBongFragment.this.getContext(), R.raw.clap);
                mp.start();
            }
*/
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
            mResultAdapter.notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final HallaBongFragment.ViewHolder holder;
            if (convertView == null) {
                Log.d("ListView : ", mListData.get(position).mDevice.getAddress());
                holder = new HallaBongFragment.ViewHolder();

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
                        Log.d("HallaBongFragment", mListData.get(holder.index).mDevice.getAddress() + "// clicked // " + holder.index);
                        mThingySdkManager.enableClassificationNotifications(mListData.get(holder.index).mDevice, isChecked);
                        mDatabaseHelper.updateNotificationsState(mListData.get(holder.index).mDevice.getAddress(), isChecked,
                                DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_CLASSIFICATION);
                    }
                });
            }
            else {
                Log.d("HallaBongFragment", "button is null");
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
            Log.d("ResultFragment : ", bluetoothDevice.getAddress() + " // " + status);
            mResultAdapter.changeImg(bluetoothDevice, status);
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
}
