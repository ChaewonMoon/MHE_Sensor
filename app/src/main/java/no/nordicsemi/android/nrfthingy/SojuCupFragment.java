package no.nordicsemi.android.nrfthingy;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
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
import android.widget.Switch;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Random;

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
 * {@link SojuCupFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SojuCupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SojuCupFragment extends Fragment {


    private ListViewAdapter mResultAdapter;
    private ArrayList<BluetoothDevice> mBleList;
    private ListView mResultListView;
    private OnFragmentInteractionListener mListener;
    private ThingySdkManager mThingySdkManager = null;
    private DatabaseHelper mDatabaseHelper;
    private Button mRandom;
    private Button mKing;
    private Button mKingButton;
    private RandomGame mRandomGame;
    private KingGame mKingGame;
    private TextView mServant;

    private boolean setServant = true;

    private String[] ledString = {"02016300", "02026300", "02036300", "02046300", "02056300", "02066300"};
    MediaType JSON;

    public SojuCupFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SojuCupFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SojuCupFragment newInstance(ArrayList<BluetoothDevice> mDeviceList) {
        SojuCupFragment fragment = new SojuCupFragment();
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

        View rootView = inflater.inflate(R.layout.fragment_soju_cup, container, false);

        mResultListView = rootView.findViewById(R.id.soju_list);
        mResultAdapter = new ListViewAdapter();
        mResultListView.setAdapter(mResultAdapter);
        mRandom = (Button) rootView.findViewById(R.id.random);
        mKing = (Button) rootView.findViewById(R.id.king);
        mServant = (TextView) rootView.findViewById(R.id.servant_list);
        mKingButton = (Button) rootView.findViewById(R.id.king_button);
        mDatabaseHelper = new DatabaseHelper(getActivity());

        for(int i = 0; i < mBleList.size(); i++) {
            mResultAdapter.addDevice(mBleList.get(i));
            ThingyListenerHelper.registerThingyListener(getContext(), mThingyListener, mBleList.get(i));
        }

        mRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRandomGame = new RandomGame();
                mRandomGame.execute();
            }
        });

        mKing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKing.setVisibility(View.GONE);
                mKingButton.setVisibility(View.VISIBLE);
                mKingGame = new KingGame();
                mKingGame.execute();
                String list = "Servant List : ";
                for(int i = 1; i < mBleList.size(); i++) {
                    if(i == 1)
                        list += "Red";
                    else if(i == 2)
                        list += ", Green";
                    else if(i == 3)
                        list += ", Yellow";
                    else if(i == 4)
                        list += "Blue";
                    else if(i == 5)
                        list += "Purple";
                    else if(i == 6)
                        list += "Cyan";
                }
                mServant.setText(list);
            }
        });

        mKingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKing.setVisibility(View.VISIBLE);
                mKingButton.setVisibility(View.GONE);
                setServant = false;
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
            inflater = SojuCupFragment.this.getLayoutInflater();
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
                Log.d("SojuCupFragment : ", device.getAddress());
                mListData.add(addInfo);
            }
            dataChange();
        }

        public void changeImg(BluetoothDevice device, String status) {
            Log.d("SojuCupFragment : ", String.valueOf(mBleList.indexOf(device)));
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
            else if(status.equals("5"))
                mHolderList.get(mBleList.indexOf(device)).mResultImage.setImageResource(R.drawable.pme_walk);

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
            final SojuCupFragment.ViewHolder holder;
            if (convertView == null) {
                Log.d("ListView : ", mListData.get(position).mDevice.getAddress());
                holder = new SojuCupFragment.ViewHolder();

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
                        Log.d("SojuCupFragment", mListData.get(holder.index).mDevice.getAddress() + "// clicked // " + holder.index);
                        mThingySdkManager.enableClassificationNotifications(mListData.get(holder.index).mDevice, isChecked);
                        mDatabaseHelper.updateNotificationsState(mListData.get(holder.index).mDevice.getAddress(), isChecked,
                                DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_CLASSIFICATION);
                    }
                });
            }
            else {
                Log.d("SojuCupFragment", "button is null");
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
            Log.d("SojuCupFragment : ", bluetoothDevice.getAddress() + " // " + status);
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

    class RandomGame extends AsyncTask<Integer, Integer, Integer> {
        @Override
        protected Integer doInBackground(Integer... integers) {
            for(int j = 0; j < mBleList.size(); j++) {
                mThingySdkManager.setLED(mBleList.get(j), "00");
            }
            MediaPlayer mp = MediaPlayer.create(SojuCupFragment.this.getContext(), R.raw.beep1);
            MediaPlayer mp2 = MediaPlayer.create(SojuCupFragment.this.getContext(), R.raw.beep1);
            MediaPlayer mp3 = MediaPlayer.create(SojuCupFragment.this.getContext(), R.raw.beep2);
            MediaPlayer mp4 = MediaPlayer.create(SojuCupFragment.this.getContext(), R.raw.pangpare);
            MediaPlayer intro = MediaPlayer.create(SojuCupFragment.this.getContext(), R.raw.soju_intro);
            MediaPlayer end = MediaPlayer.create(SojuCupFragment.this.getContext(), R.raw.soju_end);
            mp.setLooping(true);
            intro.start();
            for(int i = 0; i < mBleList.size(); i++) {
                mThingySdkManager.setLED(mBleList.get(i), "03076300");
            }
            while(intro.isPlaying()) {

            }
            for(int i = 200; i <= 600; i += 200) {
                if(i == 200)
                    mp.start();
                if(i == 600)
                    mp.stop();
                for (int k = 2; k < 6; k++) {
                    for (int j = 0; j < mBleList.size(); j++) {
                        if(i >= 600 && k >= 3)
                            break;
                        mThingySdkManager.setLED(mBleList.get(j), ledString[k]);
                        if(i >= 600) {
                            mp2.start();
                        }
                        try {
                            Thread.sleep(i);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            for(int j = 0; j < mBleList.size(); j++) {
                mThingySdkManager.setLED(mBleList.get(j), "02066300");
                mp3.start();
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for(int j = 0; j < mBleList.size(); j++) {
                mThingySdkManager.setLED(mBleList.get(j), "02076300");
                mp3.start();
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Random random = new Random();
            int n = random.nextInt(mBleList.size());
            mThingySdkManager.setLED(mBleList.get(n), "03016300");
            mp4.start();
            end.start();
            return null;
        }
    }

    class KingGame extends AsyncTask<Integer, Integer, Integer> {
        @Override
        protected Integer doInBackground(Integer... integers) {
            for(int j = 0; j < mBleList.size(); j++) {
                mThingySdkManager.setLED(mBleList.get(j), "00");
            }
            if(mBleList.size() > 7)
                return null;

            MediaPlayer mp = MediaPlayer.create(SojuCupFragment.this.getContext(), R.raw.beep1);
            MediaPlayer mp2 = MediaPlayer.create(SojuCupFragment.this.getContext(), R.raw.beep1);
            MediaPlayer mp3 = MediaPlayer.create(SojuCupFragment.this.getContext(), R.raw.beep2);
            MediaPlayer mp5 = MediaPlayer.create(SojuCupFragment.this.getContext(), R.raw.kingsang);
            mp.setLooping(true);
            //intro.start();
            //while(intro.isPlaying());
            for(int i = 200; i <= 600; i += 200) {
                if(i == 200)
                    mp.start();
                if(i == 600)
                    mp.stop();
                for (int k = 2; k < 6; k++) {
                    for (int j = 0; j < mBleList.size(); j++) {
                        if(i >= 600 && k >= 3)
                            break;
                        mThingySdkManager.setLED(mBleList.get(j), ledString[k]);
                        if(i >= 600) {
                            mp2.start();
                        }
                        try {
                            Thread.sleep(i);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            for(int j = 0; j < mBleList.size(); j++) {
                mThingySdkManager.setLED(mBleList.get(j), "02066300");
                mp3.start();
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for(int j = 0; j < mBleList.size(); j++) {
                mThingySdkManager.setLED(mBleList.get(j), "02076300");
                mp3.start();
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Random random = new Random();
            int n = random.nextInt(mBleList.size());
            mThingySdkManager.setLED(mBleList.get(n), "03076300");
            mp5.start();
            //end.start();

            boolean[] check = {false, false, false, false, false, false, false};
            check[n] = true;

            while(setServant);

            for(int i = 1; i < mBleList.size(); i++) {
                if(i == 1) {
                    while(check[n]) {
                        n = random.nextInt(mBleList.size());
                    }
                    check[n] = true;
                    mThingySdkManager.setLED(mBleList.get(n), "03016300");
                }
                else if(i == 2) {
                    while(check[n]) {
                        n = random.nextInt(mBleList.size());
                    }
                    check[n] = true;
                    mThingySdkManager.setLED(mBleList.get(n), "03026300");
                }
                else if(i == 3) {
                    while(check[n]) {
                        n = random.nextInt(mBleList.size());
                    }
                    check[n] = true;
                    mThingySdkManager.setLED(mBleList.get(n), "03036300");
                }
                else if(i == 4) {
                    while(check[n]) {
                        n = random.nextInt(mBleList.size());
                    }
                    check[n] = true;
                    mThingySdkManager.setLED(mBleList.get(n), "03046300");
                }
                else if(i == 5) {
                    while(check[n]) {
                        n = random.nextInt(mBleList.size());
                    }
                    check[n] = true;
                    mThingySdkManager.setLED(mBleList.get(n), "03056300");
                }
                else if(i == 6) {
                    while(check[n]) {
                        n = random.nextInt(mBleList.size());
                    }
                    check[n] = true;
                    mThingySdkManager.setLED(mBleList.get(n), "03066300");
                }
            }
            return null;
        }
    }
}
