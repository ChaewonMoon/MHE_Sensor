/*
 * Copyright (c) 2010 - 2017, Nordic Semiconductor ASA
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form, except as embedded into a Nordic
 *    Semiconductor ASA integrated circuit in a product or a software update for
 *    such product, must reproduce the above copyright notice, this list of
 *    conditions and the following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. Neither the name of Nordic Semiconductor ASA nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * 4. This software, with or without modification, must only be used with a
 *    Nordic Semiconductor ASA integrated circuit.
 *
 * 5. Any software provided in binary form under this license must not be reverse
 *    engineered, decompiled, modified and/or disassembled.
 *
 * THIS SOFTWARE IS PROVIDED BY NORDIC SEMICONDUCTOR ASA "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY, NONINFRINGEMENT, AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NORDIC SEMICONDUCTOR ASA OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.nrfthingy;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.rajawali3d.surface.RajawaliSurfaceView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import no.nordicsemi.android.nrfthingy.common.ScannerFragmentListener;
import no.nordicsemi.android.nrfthingy.common.Utils;
import no.nordicsemi.android.nrfthingy.configuration.IFTTTokenDialogFragment;
import no.nordicsemi.android.nrfthingy.database.DatabaseContract;
import no.nordicsemi.android.nrfthingy.database.DatabaseHelper;
import no.nordicsemi.android.nrfthingy.widgets.Renderer;
import no.nordicsemi.android.thingylib.Thingy;
import no.nordicsemi.android.thingylib.ThingyListener;
import no.nordicsemi.android.thingylib.ThingyListenerHelper;
import no.nordicsemi.android.thingylib.ThingySdkManager;
import no.nordicsemi.android.thingylib.utils.ThingyUtils;

public class MotionServiceFragment extends Fragment implements ScannerFragmentListener {

    private static final int REQUEST_ENABLE_BT = 1021;

    private Toolbar mQuaternionToolbar;
    private Toolbar mCameraToolbar;
    private Toolbar mMotionToolbar;
    private Toolbar mGravityToolbar;
    private Toolbar mGravityToolbar2;
    private LinearLayout mCameraLayout;

    private boolean recordon = true;
    FileWriter fw;
    BufferedWriter bw;

    private TextView mTapCount;
    private TextView mTapDirection;
    private TextView mOrientation;
    private TextView mHeading;
    private TextView mPedometerSteps;
    private TextView mPedometerDuration;
    private TextView mHeadingDirection;

    private FrameLayout cameraLayout;
    private FloatingActionButton imgTakeBtn;
    private boolean mCameraState;
    private boolean isRecording = false;
    private LocalView sv;
    private Switch mCameraSwitch;
    private Switch mChartSwitch;
    private Switch m3dSwitch;
    private ImageView mPortraitImage;

    private RajawaliSurfaceView mGlSurfaceView;
    private BluetoothDevice mDevice;

    private DatabaseHelper mDatabaseHelper;
    private ThingySdkManager mThingySdkManager = null;
    private MotionFragmentListener mListener;
    private boolean mIsConnected = false;

    private ImageView mHeadingImage;
    private LineChart mLineChartGravityVector;
    private LineChart mLineChartGravityVector2;
    private boolean mIsFragmentAttached = false;
    private Renderer mRenderer;
    private int count = -1;

    private ThingyListener mThingyListener = new ThingyListener() {
        /*public float mCurrentDegree = 0.0f;
        private float mHeadingDegrees;
        private RotateAnimation mHeadingAnimation;*/

        @Override
        public void onDeviceConnected (BluetoothDevice device,int connectionState){
            //Connectivity callbacks handled by main activity
        }

        @Override
        public void onDeviceDisconnected (BluetoothDevice device,int connectionState){
            if (mDevice.equals(device)) {
                mRenderer.setConnectionState(false);
                if (Utils.checkIfVersionIsAboveJellyBean()) {
                    mRenderer.setNotificationEnabled(false);
                }
            }
        }

        @Override
        public void onServiceDiscoveryCompleted (BluetoothDevice device){
            if (mDevice.equals(device)) {
                mIsConnected = true;
                if (Utils.checkIfVersionIsAboveJellyBean()) {
                    mRenderer.setConnectionState(true);
                    if (mDatabaseHelper.getNotificationsState(mDevice.getAddress(), DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_QUATERNION)) {
                        mRenderer.setNotificationEnabled(true);
                    }
                }
            }
        }

        @Override
        public void onBatteryLevelChanged ( final BluetoothDevice bluetoothDevice,
        final int batteryLevel){
        }

        @Override
        public void onTemperatureValueChangedEvent (BluetoothDevice bluetoothDevice, String
        temperature){
        }

        @Override
        public void onPressureValueChangedEvent (BluetoothDevice bluetoothDevice,
        final String pressure){
        }

        @Override
        public void onHumidityValueChangedEvent (BluetoothDevice bluetoothDevice,
        final String humidity){
        }

        @Override
        public void onAirQualityValueChangedEvent (BluetoothDevice bluetoothDevice,final int eco2,
        final int tvoc){
        }

        @Override
        public void onColorIntensityValueChangedEvent (BluetoothDevice bluetoothDevice,
        final float red, final float green, final float blue, final float alpha){
        }

        @Override
        public void onButtonStateChangedEvent (BluetoothDevice bluetoothDevice,int buttonState){

        }

        @Override
        public void onTapValueChangedEvent (BluetoothDevice bluetoothDevice,int direction,
        int tapCount){

        }

        @Override
        public void onOrientationValueChangedEvent (BluetoothDevice bluetoothDevice,int orientation)
        {

        }

        @Override //MHE// motion fragment에서 3D 표현할때 사용합니다.
        public void onQuaternionValueChangedEvent (BluetoothDevice bluetoothDevice,float w, float x,
        float y, float z){
            if (mIsFragmentAttached) {
                if (mGlSurfaceView != null) {
                    mRenderer.setQuaternions(x, y, z, w);
                }
            }
        }

        @Override//MHE//사용하지 않습니다.
        public void onPedometerValueChangedEvent (BluetoothDevice bluetoothDevice,int steps,
        long duration){

        }

        @Override //MHE//PME fragment에서 Last event detected 에 나타낼때 사용합니다.
        public void onKnowledgePackValueChangedEvent (BluetoothDevice bluetoothDevice, String status, String indicator, String cla4)
        {

        }

        @Override //MHE//PME fragment에서 FeatureVector 에 나타낼때 사용합니다.
        public void onFeatureVectorValueChangedEvent (BluetoothDevice bluetoothDevice, String
        len, String x, String y, String z){

        }

        @Override
        public void onResultVectorValueChangedEvent(BluetoothDevice bluetoothDevice, String R_0, String R_1, String R_2, String R_3, String R_4, String R_5, String R_6, String R_7, String R_8, String R_9, String R_10, String R_11, String R_12, String R_13, String R_14, String R_15) {

        }

        @Override
        public void onAccelerometerValueChangedEvent (BluetoothDevice bluetoothDevice,
        float accelerometerX, float accelerometerY, float accelerometerZ){

        }

        @Override//MHE// motionfragmnet 에서 차트에 나타낼때 사용합니다.
        public void onAcelGyroValueChangedEvent (BluetoothDevice bluetoothDevice,float ax, float ay,
        float az, float gx, float gy, float gz){
            addGravityVectorEntry(ax, ay, az, gx, gy, gz);

            if (bw != null) {
                try {
                    bw.write(
                            String.valueOf(count) + "," + String.valueOf(ax) + "," +
                                    String.valueOf(ay) + "," + String.valueOf(az) + "," +
                                    String.valueOf(gx) + "," + String.valueOf(gy) + "," +
                                    String.valueOf(gz)
                            );
                    bw.write('\n');
                    count++;
                } catch (IOException e) {
                }
            }
        }


        @Override
        public void onCompassValueChangedEvent (BluetoothDevice bluetoothDevice,float x, float y,
        float z){

        }

        @Override
        public void onEulerAngleChangedEvent (BluetoothDevice bluetoothDevice,float roll,
        float pitch, float yaw){
        }

        @Override
        public void onRotationMatixValueChangedEvent (BluetoothDevice bluetoothDevice,byte[] matrix)
        {

        }

        @Override //MHE// 사용하지 않습니다.
        public void onHeadingValueChangedEvent (BluetoothDevice bluetoothDevice,float heading){

        }

        @Override //MHE//원래는 차트를 나타낼때
        // addGravityVectorEntry를 썼었는데 위에 onAcelGyroValueChangedEvent 쪽으로 옮겼습니다.
        public void onGravityVectorChangedEvent (BluetoothDevice bluetoothDevice,float x, float y,
        float z, float x2, float y2, float z2){

        }

        @Override
        public void onSpeakerStatusValueChangedEvent (BluetoothDevice bluetoothDevice,int status){

        }

        @Override
        public void onMicrophoneValueChangedEvent (BluetoothDevice bluetoothDevice,final byte[] data)
        {

        }

        @Override
        public void connectionCheck() {

        }
    };


    public interface MotionFragmentListener {
    }

    public MotionServiceFragment() {
        // Required empty public constructor
    }

    public static MotionServiceFragment newInstance(final BluetoothDevice device) {
        MotionServiceFragment fragment = new MotionServiceFragment();
        final Bundle args = new Bundle();
        args.putParcelable(Utils.CURRENT_DEVICE, device);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabaseHelper = new DatabaseHelper(getActivity());
        if (getArguments() != null) {
            mDevice = getArguments().getParcelable(Utils.CURRENT_DEVICE);
        }
    }

    @Override//MHE// Camera 관련된거는 전부 새로 추가됐습니다.
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_motion, container, false);
        mThingySdkManager = ThingySdkManager.getInstance();
        mMotionToolbar = rootView.findViewById(R.id.card_toolbar_motion);
        mGravityToolbar = rootView.findViewById(R.id.card_toolbar_gravity);
        mGravityToolbar2 = rootView.findViewById(R.id.card_toolbar_gravity2); //MHE//차트 두개를 따로 사용하기 위해서 새로 만들었습니다.

        mCameraLayout = rootView.findViewById(R.id.camera_layout);
        mCameraToolbar = rootView.findViewById(R.id.card_toolbar_camera);
        cameraLayout = rootView.findViewById(R.id.Shooting_FrameLayout);
        mCameraSwitch = rootView.findViewById(R.id.switch_camera);

        mChartSwitch = rootView.findViewById(R.id.switch_chart);
        m3dSwitch = rootView.findViewById(R.id.switch_3d);

        sv = new LocalView(getActivity().getBaseContext());
        imgTakeBtn = rootView.findViewById(R.id.take_picture);

        sv.startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
        cameraLayout.addView(sv);

        sv.closeCamera();

        mCameraLayout.setVisibility(View.GONE);
        mCameraSwitch.setChecked(mCameraState);
        mCameraSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked && mCameraState) {
                    sv.closeCamera();
                    mCameraLayout.setVisibility(View.GONE);
                } else if (isChecked && !mCameraState) {
                    sv.startCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                    mCameraLayout.setVisibility(View.VISIBLE);
                }
                mCameraState = !mCameraState;
            }
        });

        imgTakeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {  //MHE// motionfragment에서 촬영버튼 누를때
                if (recordon) { // start recording
                    sv.startRecord();

                    // change the image and color to stop recording
                    imgTakeBtn.setImageResource(R.drawable.ic_pause_white);
                    imgTakeBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorRed)));

                    if (mIsConnected) {

                        File tempDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MHE_RAW");

                        if (!tempDir.exists())
                            tempDir.mkdirs();   // mhevideo 디렉터리생성
                        try {
                            String time = new SimpleDateFormat("/MMdd_HHmmss").format(new Date(System.currentTimeMillis()));
                            fw = new FileWriter(tempDir + time + ".csv");
                            bw = new BufferedWriter(fw);
                            bw.write("sequence, AccelerometerX, AccelerometerY, AccelerometerZ, GyroscopeX, GyroscopeY, GyroscopeZ\n");

                        } catch (IOException e) {

                        }
                    }
                    count = 0;
                } else {
                    // stop recording
                    sv.stopRecord();

                    // change the image and color to start recording
                    imgTakeBtn.setImageResource(R.drawable.record_camera);
                    imgTakeBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));

                    if (mIsConnected) {
                        try {
                            bw.close();
                        } catch (IOException e) {

                        }
                    }
                }
                //enableRawDataNotifications(recordon);
                recordon = !recordon;
            }
        });

        //MHE//차트 두개사용을 위함입니다.
        mLineChartGravityVector = rootView.findViewById(R.id.line_chart_gravity_vector);
        mLineChartGravityVector2 = rootView.findViewById(R.id.line_chart_gravity_vector2);

        mIsConnected = isConnected(mDevice);
        if (Utils.checkIfVersionIsAboveJellyBean()) {
            mQuaternionToolbar = rootView.findViewById(R.id.card_toolbar_euler);
            mGlSurfaceView = rootView.findViewById(R.id.rajwali_surface);
            mRenderer = new Renderer(getActivity());
            mGlSurfaceView.setSurfaceRenderer(mRenderer);
            mRenderer.setConnectionState(mIsConnected);
            if (mDatabaseHelper.getNotificationsState(mDevice.getAddress(), DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_QUATERNION)) {
                mRenderer.setNotificationEnabled(true);
            }
        }
        if (mCameraToolbar != null) {
            mCameraToolbar.setLogo(R.drawable.ic_camera);
            mCameraToolbar.setTitle(getString(R.string.camera_title));

        }
        if (mQuaternionToolbar != null) {
            mQuaternionToolbar.setLogo(R.drawable.ic_animation);
            mQuaternionToolbar.setTitle(getString(R.string.euler_title));
            //mQuaternionToolbar.inflateMenu(R.menu.); //MHE//원래는 항목마다 cardmenu가있었는데 전부 스위치로 교체했습니다.

            if (mDevice != null) {
                //updateQuaternionCardOptionsMenu(mQuaternionToolbar.getMenu());
            }

            //MHE//사용하지 않습니다.

            //MHE//3D 키고 끄기 위함입니다.
            m3dSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {
                        m3dSwitch.setChecked(true);
                    } else {
                        m3dSwitch.setChecked(false);
                    }
                    enableQuaternionNotifications(m3dSwitch.isChecked());
                }
            });
        }

        //MHE//사용하지 않습니다.
        if (mGravityToolbar != null) {
            mGravityToolbar.setLogo(R.drawable.ic_apple);
            mGravityToolbar.setTitle(getString(R.string.title_gravity_vector));
            // mGravityToolbar.inflateMenu(R.menu.gravity_card_menu);

            if (mDevice != null) {
                // updateGravityCardOptionsMenu(mGravityToolbar.getMenu());
            }
            mChartSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {
                        mChartSwitch.setChecked(true);
                    } else {
                        mChartSwitch.setChecked(false);
                    }
                    enableRawDataNotifications(isChecked);

                }
            });
            //MHE//사용하지 않습니다.
        }
        if (mGravityToolbar2 != null) {//MHE//두번째 그래프를 위함입니다.
            mGravityToolbar2.setLogo(R.drawable.ic_apple);
            mGravityToolbar2.setTitle(getString(R.string.title_gravity_vector2));
        }
        prepareGravityVectorChart();
        prepareGravityVectorChart2();
//      loadFeatureDiscoverySequence();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        ThingyListenerHelper.registerThingyListener(getContext(), mThingyListener, mDevice);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mIsFragmentAttached = true;
        if (context instanceof MotionFragmentListener) {
            mListener = (MotionFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement CloudFragmentListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGlSurfaceView != null) {
            mGlSurfaceView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGlSurfaceView != null) {
            mGlSurfaceView.onPause();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mIsFragmentAttached = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        ThingyListenerHelper.unregisterThingyListener(getContext(), mThingyListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

    private boolean isConnected(final BluetoothDevice device) {
        if (mThingySdkManager != null) {
            final List<BluetoothDevice> connectedDevices = mThingySdkManager.getConnectedDevices();
            for (BluetoothDevice dev : connectedDevices) {
                if (device.getAddress().equals(dev.getAddress())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void prepareGravityVectorChart() {
        if (!mLineChartGravityVector.isEmpty()) {
            mLineChartGravityVector.clearValues();
        }
        mLineChartGravityVector.setDescription(getString(R.string.title_gravity_vector));
        mLineChartGravityVector.setTouchEnabled(true);
        mLineChartGravityVector.setVisibleXRangeMinimum(5);
        mLineChartGravityVector.setVisibleXRangeMaximum(5);

        // enable scaling and dragging
        mLineChartGravityVector.setDragEnabled(true);
        mLineChartGravityVector.setPinchZoom(true);
        mLineChartGravityVector.setScaleEnabled(true);
        mLineChartGravityVector.setAutoScaleMinMaxEnabled(true);
        mLineChartGravityVector.setDrawGridBackground(false);
        mLineChartGravityVector.setBackgroundColor(Color.WHITE);

        LineData data = new LineData();
        data.setValueFormatter(new GravityVectorChartValueFormatter());
        data.setValueTextColor(Color.WHITE);
        mLineChartGravityVector.setData(data);

        Legend legend = mLineChartGravityVector.getLegend();
        legend.setEnabled(false);

        XAxis xAxis = mLineChartGravityVector.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);
        xAxis.setAvoidFirstLastClipping(true);

        YAxis leftAxis = mLineChartGravityVector.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setValueFormatter(new GravityVectorYValueFormatter());
        leftAxis.setDrawLabels(true);
        leftAxis.setAxisMinValue(-32768); //MHE//첫번째 그래프에 보여지는 값의 범위입니다
        leftAxis.setAxisMaxValue(32768);
        leftAxis.setLabelCount(6, false); //
        leftAxis.setDrawZeroLine(true);

        YAxis rightAxis = mLineChartGravityVector.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private void prepareGravityVectorChart2() { //MHE//두번째 그래프입니다.
        if (!mLineChartGravityVector2.isEmpty()) {
            mLineChartGravityVector2.clearValues();
        }
        mLineChartGravityVector2.setDescription(getString(R.string.title_gravity_vector2));
        mLineChartGravityVector2.setTouchEnabled(true);
        mLineChartGravityVector2.setVisibleXRangeMinimum(5);
        mLineChartGravityVector2.setVisibleXRangeMaximum(5);

        // enable scaling and dragging
        mLineChartGravityVector2.setDragEnabled(true);
        mLineChartGravityVector2.setPinchZoom(true);
        mLineChartGravityVector2.setScaleEnabled(true);
        mLineChartGravityVector2.setAutoScaleMinMaxEnabled(true);
        mLineChartGravityVector2.setDrawGridBackground(false);
        mLineChartGravityVector2.setBackgroundColor(Color.WHITE);

        LineData data = new LineData();
        data.setValueFormatter(new GravityVectorChartValueFormatter());
        data.setValueTextColor(Color.WHITE);
        mLineChartGravityVector2.setData(data);

        Legend legend = mLineChartGravityVector2.getLegend();
        legend.setEnabled(false);

        XAxis xAxis = mLineChartGravityVector2.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);
        xAxis.setAvoidFirstLastClipping(true);

        YAxis leftAxis = mLineChartGravityVector2.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setValueFormatter(new GravityVectorYValueFormatter());
        leftAxis.setDrawLabels(true);
        leftAxis.setAxisMinValue(-32768);
        leftAxis.setAxisMaxValue(32768);
        leftAxis.setLabelCount(6, false); //
        leftAxis.setDrawZeroLine(true);

        YAxis rightAxis = mLineChartGravityVector2.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private LineDataSet[] createGravityVectorDataSet() {

        //MHE// 원래는 그래프값이 3개였는데 우리는 6개를 사용합니다.
        final LineDataSet[] lineDataSets = new LineDataSet[6];
        LineDataSet lineDataSetX = new LineDataSet(null, getString(R.string.accelerometer_x));
        lineDataSetX.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSetX.setColor(ContextCompat.getColor(getActivity(), R.color.colorRed));
        lineDataSetX.setHighLightColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
        lineDataSetX.setValueFormatter(new GravityVectorChartValueFormatter());
//        lineDataSetX.setDrawValues(true);
//        lineDataSetX.setDrawCircles(true);
        lineDataSetX.setDrawCircles(false);
        lineDataSetX.setDrawCircleHole(false);
        lineDataSetX.setValueTextSize(Utils.CHART_VALUE_TEXT_SIZE);
        lineDataSetX.setLineWidth(Utils.CHART_LINE_WIDTH);
        lineDataSets[0] = lineDataSetX;

        LineDataSet lineDataSetY = new LineDataSet(null, getString(R.string.accelerometer_y));
        lineDataSetY.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSetY.setColor(ContextCompat.getColor(getActivity(), R.color.colorGreen));
        lineDataSetY.setHighLightColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
        lineDataSetY.setValueFormatter(new GravityVectorChartValueFormatter());
//        lineDataSetY.setDrawValues(true);
//        lineDataSetY.setDrawCircles(true);
        lineDataSetY.setDrawCircles(false);
        lineDataSetY.setDrawCircleHole(false);
        lineDataSetY.setValueTextSize(Utils.CHART_VALUE_TEXT_SIZE);
        lineDataSetY.setLineWidth(Utils.CHART_LINE_WIDTH);
        lineDataSets[1] = lineDataSetY;

        LineDataSet lineDataSetZ = new LineDataSet(null, getString(R.string.accelerometer_z));
        lineDataSetZ.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSetZ.setColor(ContextCompat.getColor(getActivity(), R.color.colorBlue));
        lineDataSetZ.setHighLightColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
        lineDataSetZ.setValueFormatter(new GravityVectorChartValueFormatter());
//        lineDataSetZ.setDrawValues(true);
//        lineDataSetZ.setDrawCircles(true);
        lineDataSetZ.setDrawCircles(false);
        lineDataSetZ.setDrawCircleHole(false);
        lineDataSetZ.setValueTextSize(Utils.CHART_VALUE_TEXT_SIZE);
        lineDataSetZ.setLineWidth(Utils.CHART_LINE_WIDTH);
        lineDataSets[2] = lineDataSetZ;

        LineDataSet lineDataSetX2 = new LineDataSet(null, getString(R.string.gyroscope_x));
        lineDataSetX2.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSetX2.setColor(ContextCompat.getColor(getActivity(), R.color.colorNordicOrange));
        lineDataSetX2.setHighLightColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
        lineDataSetX2.setValueFormatter(new GravityVectorChartValueFormatter());
//        lineDataSetX2.setDrawValues(true);
//        lineDataSetX2.setDrawCircles(true);
        lineDataSetX2.setDrawCircles(false);
        lineDataSetX2.setDrawCircleHole(false);
        lineDataSetX2.setValueTextSize(Utils.CHART_VALUE_TEXT_SIZE);
        lineDataSetX2.setLineWidth(Utils.CHART_LINE_WIDTH);
        lineDataSets[3] = lineDataSetX2;

        LineDataSet lineDataSetY2 = new LineDataSet(null, getString(R.string.gyroscope_y));
        lineDataSetY2.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSetY2.setColor(ContextCompat.getColor(getActivity(), R.color.holo_green_light));
        lineDataSetY2.setHighLightColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
        lineDataSetY2.setValueFormatter(new GravityVectorChartValueFormatter());
//        lineDataSetY2.setDrawValues(true);
//        lineDataSetY2.setDrawCircles(true);
        lineDataSetY2.setDrawCircles(false);
        lineDataSetY2.setDrawCircleHole(false);
        lineDataSetY2.setValueTextSize(Utils.CHART_VALUE_TEXT_SIZE);
        lineDataSetY2.setLineWidth(Utils.CHART_LINE_WIDTH);
        lineDataSets[4] = lineDataSetY2;

        LineDataSet lineDataSetZ2 = new LineDataSet(null, getString(R.string.gyroscope_z));
        lineDataSetZ2.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSetZ2.setColor(ContextCompat.getColor(getActivity(), R.color.holo_purple));
        lineDataSetZ2.setHighLightColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
        lineDataSetZ2.setValueFormatter(new GravityVectorChartValueFormatter());
//        lineDataSetZ2.setDrawValues(true);
//        lineDataSetZ2.setDrawCircles(true);
        lineDataSetZ2.setDrawCircles(false);
        lineDataSetZ2.setDrawCircleHole(false);
        lineDataSetZ2.setValueTextSize(Utils.CHART_VALUE_TEXT_SIZE);
        lineDataSetZ2.setLineWidth(Utils.CHART_LINE_WIDTH);
        lineDataSets[5] = lineDataSetZ2;
        return lineDataSets;
    }

    private void addGravityVectorEntry(final float accelX, final float accelY, final float accelZ
            , final float gyroX, final float gyroY, final float gyroZ) {

        LineData data = mLineChartGravityVector.getData();
        LineData data2 = mLineChartGravityVector2.getData();
        if (data != null) {
            ILineDataSet setX = data.getDataSetByIndex(0);
            ILineDataSet setY = data.getDataSetByIndex(1);
            ILineDataSet setZ = data.getDataSetByIndex(2);
            ILineDataSet setX2 = data.getDataSetByIndex(3);
            ILineDataSet setY2 = data.getDataSetByIndex(4);
            ILineDataSet setZ2 = data.getDataSetByIndex(5);
            ILineDataSet setX5 = data2.getDataSetByIndex(0);
            ILineDataSet setY5 = data2.getDataSetByIndex(1);
            ILineDataSet setZ5 = data2.getDataSetByIndex(2);

            if (setX == null || setY == null || setZ == null || setY2 == null || setZ2 == null || setX2 == null) {
                final LineDataSet[] dataSets = createGravityVectorDataSet();
                setX = dataSets[0];
                setY = dataSets[1];
                setZ = dataSets[2];
                setX2 = dataSets[3];
                setY2 = dataSets[4];
                setZ2 = dataSets[5];

                data.addDataSet(setX);
                data.addDataSet(setY);
                data.addDataSet(setZ);
                data.addDataSet(setX2);
                data.addDataSet(setY2);
                data.addDataSet(setZ2);
                final LineDataSet[] dataSets2 = createGravityVectorDataSet();
                setX5 = dataSets2[3];
                setY5 = dataSets2[4];
                setZ5 = dataSets2[5];
                data2.addDataSet(setX5);
                data2.addDataSet(setY5);
                data2.addDataSet(setZ5);
            }

            data.addXValue(ThingyUtils.TIME_FORMAT_PEDOMETER.format(new Date()));
            data.addEntry(new Entry(accelX, setX.getEntryCount()), 0);
            data.addEntry(new Entry(accelY, setY.getEntryCount()), 1);
            data.addEntry(new Entry(accelZ, setZ.getEntryCount()), 2);

            data2.addXValue(ThingyUtils.TIME_FORMAT_PEDOMETER.format(new Date()));
            data2.addEntry(new Entry(gyroX, setX5.getEntryCount()), 0);
            data2.addEntry(new Entry(gyroY, setY5.getEntryCount()), 1);
            data2.addEntry(new Entry(gyroZ, setZ5.getEntryCount()), 2);
            mLineChartGravityVector.notifyDataSetChanged();
            // CHART EDIT // mLineChartGravityVector.setVisibleXRangeMaximum(10);
            mLineChartGravityVector.setVisibleXRangeMaximum(500);
            //mLineChartGravityVector.moveViewToX(data.getXValCount() - 11);
            mLineChartGravityVector.moveViewToX(data.getXValCount() - 501);

            mLineChartGravityVector2.notifyDataSetChanged();
            // CHART EDIT // mLineChartGravityVector2.setVisibleXRangeMaximum(10);
            mLineChartGravityVector2.setVisibleXRangeMaximum(500);
            //mLineChartGravityVector2.moveViewToX(data.getXValCount() - 11);
            mLineChartGravityVector2.moveViewToX(data.getXValCount() - 501);
        }

    }

    public class GravityVectorYValueFormatter implements YAxisValueFormatter {
        private DecimalFormat mFormat;

        public GravityVectorYValueFormatter() {
            mFormat = new DecimalFormat("##,##,#0.00");
        }

        @Override
        public String getFormattedValue(float value, YAxis yAxis) {
            return mFormat.format(value); //
        }
    }

    public class GravityVectorChartValueFormatter implements ValueFormatter {
        private DecimalFormat mFormat;

        public GravityVectorChartValueFormatter() {
            mFormat = new DecimalFormat("#0.00");
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return mFormat.format(value);
        }
    }

    private void updateGravityCardOptionsMenu(final Menu gravityCardMotion) {

        final String address = mDevice.getAddress();
        if (mDatabaseHelper.getNotificationsState(address, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_GRAVITY_VECTOR)) {
            gravityCardMotion.findItem(R.id.action_gravity_vector_notification).setChecked(true);
        } else {
            gravityCardMotion.findItem(R.id.action_gravity_vector_notification).setChecked(false);
        }
    }

    //MHE//저희는 6개의 raw data를 사용합니다.
    public void enableRawDataNotifications(final boolean notificationEnabled) {
        mThingySdkManager.enableRawDataNotifications(mDevice, notificationEnabled);
        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), notificationEnabled, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_RAW_DATA);
    }

    private void enableQuaternionNotifications(final boolean notificationEnabled) {
        mRenderer.setNotificationEnabled(notificationEnabled);
        mThingySdkManager.enableQuaternionNotifications(mDevice, notificationEnabled);
        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), notificationEnabled, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_QUATERNION);
    }

    private void enableGravityVectorNotifications(final boolean notificationEnabled) {
        mThingySdkManager.enableGravityVectorNotifications(mDevice, notificationEnabled);
        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), notificationEnabled, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_GRAVITY_VECTOR);
    }

    private void enableEulerNotifications(final boolean notificationEnabled) {
        mThingySdkManager.enableEulerNotifications(mDevice, notificationEnabled);
        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), notificationEnabled, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_EULER);
    }

    private void loadFeatureDiscoverySequence() {
        if (!Utils.checkIfSequenceIsCompleted(getContext(), Utils.INITIAL_MOTION_TUTORIAL)) {

            final SpannableString desc = new SpannableString(getString(R.string.start_stop_motion_sensors));

            final TapTargetSequence sequence = new TapTargetSequence(getActivity());
            sequence.continueOnCancel(true);
            sequence.targets(
                    TapTarget.forToolbarOverflow(mQuaternionToolbar, desc).
                            dimColor(R.color.greyBg).
                            outerCircleColor(R.color.colorAccent).id(0)).listener(new TapTargetSequence.Listener() {
                @Override
                public void onSequenceFinish() {
                    Utils.saveSequenceCompletion(getContext(), Utils.INITIAL_MOTION_TUTORIAL);
                }

                @Override
                public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

                }

                @Override
                public void onSequenceCanceled(TapTarget lastTarget) {

                }
            }).start();
        }
    }
}