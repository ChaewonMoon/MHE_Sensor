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

package no.nordicsemi.android.thingylib;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import no.nordicsemi.android.thingylib.utils.ThingyUtils;


public class ThingyListenerHelper {
    private static boolean aa = true;
    private static ThingyBroadcastReceiver mThingyBroadcastReceiver;

    public static class ThingyBroadcastReceiver extends BroadcastReceiver {

        private ThingyListener mGlobalThingyListener;
        private Map<BluetoothDevice, ThingyListener> mListeners = new HashMap<>();

        public void setThingyListener(final ThingyListener listener) {
            this.mGlobalThingyListener = listener;
        }

        public void setThingyListener(final BluetoothDevice device, final ThingyListener listener) {
            this.mListeners.put(device, listener);
        }

        public boolean removeThingyListener(final ThingyListener listener) {

            if (mGlobalThingyListener == listener) {
                mGlobalThingyListener = null;
            }

            // We do it 2 times as the listener was added for 2 addresses
            for (final Map.Entry<BluetoothDevice, ThingyListener> entry : mListeners.entrySet()) {
                if (entry.getValue() == listener) {
                    mListeners.remove(entry.getKey());
                    break;
                }
            }

            return mGlobalThingyListener == null && mListeners.isEmpty();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final BluetoothDevice device = intent.getParcelableExtra(ThingyUtils.EXTRA_DEVICE);
            // Find proper listeners
            final ThingyListener globalListener = mGlobalThingyListener;
            final ThingyListener thingyListener = mListeners.get(device);

            if (globalListener == null && thingyListener == null)
                return;

            final String action = intent.getAction();

            //MHE//6개의 값을 사용합니다.
            final float accelerometerX = intent.getExtras().getFloat(ThingyUtils.EXTRA_DATA_ACCELEROMETER_X);
            final float accelerometerY = intent.getExtras().getFloat(ThingyUtils.EXTRA_DATA_ACCELEROMETER_Y);
            final float accelerometerZ = intent.getExtras().getFloat(ThingyUtils.EXTRA_DATA_ACCELEROMETER_Z);

            final float gyroscopeX = intent.getExtras().getFloat(ThingyUtils.EXTRA_DATA_GYROSCOPE_X);
            final float gyroscopeY = intent.getExtras().getFloat(ThingyUtils.EXTRA_DATA_GYROSCOPE_Y);
            final float gyroscopeZ = intent.getExtras().getFloat(ThingyUtils.EXTRA_DATA_GYROSCOPE_Z);

            switch (action) {
                case ThingyUtils.ACTION_DEVICE_CONNECTED:
                    if (globalListener != null) {
                        globalListener.onDeviceConnected(device, BluetoothGatt.STATE_CONNECTED);
                    }

                    if (thingyListener != null) {
                        thingyListener.onDeviceConnected(device, 1);
                    }
                    break;
                case ThingyUtils.ACTION_DEVICE_DISCONNECTED:
                    if (globalListener != null) {
                        globalListener.onDeviceDisconnected(device, BluetoothGatt.STATE_DISCONNECTED);
                    }

                    if (thingyListener != null) {
                        thingyListener.onDeviceDisconnected(device, 1);
                    }
                    break;
                case ThingyUtils.ACTION_SERVICE_DISCOVERY_COMPLETED:
                    if (globalListener != null) {
                        globalListener.onServiceDiscoveryCompleted(device);
                    }

                    if (thingyListener != null) {
                        thingyListener.onServiceDiscoveryCompleted(device);
                    }
                    break;
                case ThingyUtils.ACTION_DATA_RECEIVED:
                    break;
                case ThingyUtils.BATTERY_LEVEL_NOTIFICATION:
                    final int batteryLevel = intent.getExtras().getInt(ThingyUtils.EXTRA_DATA);
                    if (globalListener != null) {
                        globalListener.onBatteryLevelChanged(device, batteryLevel);
                    }

                    if (thingyListener != null) {
                        thingyListener.onBatteryLevelChanged(device, batteryLevel);
                    }
                    break;
                case ThingyUtils.TEMPERATURE_NOTIFICATION:
                    final String temperature = intent.getExtras().getString(ThingyUtils.EXTRA_DATA);
                    if (globalListener != null) {
                        globalListener.onTemperatureValueChangedEvent(device, temperature);
                    }

                    if (thingyListener != null) {
                        thingyListener.onTemperatureValueChangedEvent(device, temperature);
                    }
                    break;
                case ThingyUtils.CLASSIFICATION_NOTIFICATION:
                    final String classification = intent.getExtras().getString(ThingyUtils.EXTRA_DATA);
                    if (globalListener != null) {
                        globalListener.onKnowledgePackValueChangedEvent(device, classification);
                    }
                    if (thingyListener != null) {
                        thingyListener.onKnowledgePackValueChangedEvent(device, classification);
                    }
                case ThingyUtils.FEATUREVECTOR_NOTIFICATION:
                    final String vectorLength = intent.getExtras().getString(ThingyUtils.EXTRA_DATA_VECTORLENGTH);
                    final String feature_X = intent.getExtras().getString(ThingyUtils.EXTRA_DATA_FEATUREVECTOR_0);
                    final String feature_Y = intent.getExtras().getString(ThingyUtils.EXTRA_DATA_FEATUREVECTOR_1);
                    final String feature_Z = intent.getExtras().getString(ThingyUtils.EXTRA_DATA_FEATUREVECTOR_2);

                    if (globalListener != null) {
                        globalListener.onFeatureVectorValueChangedEvent(device, vectorLength, feature_X, feature_Y, feature_Z);
                    }
                    if (thingyListener != null) {
                        thingyListener.onFeatureVectorValueChangedEvent(device, vectorLength, feature_X, feature_Y, feature_Z);
                    }
                case ThingyUtils.RESULTVECTOR_NOTIFICATION:
                    Log.d("PME_result_history: ", "RESULTVECTOR_NOTIFICATION start");
                    final String vectorLength_R = intent.getExtras().getString(ThingyUtils.EXTRA_DATA_RVECTORLENGTH);
                    final String feature_0_R = intent.getExtras().getString(ThingyUtils.EXTRA_DATA_RESULTVECTOR_0);
                    final String feature_1_R = intent.getExtras().getString(ThingyUtils.EXTRA_DATA_RESULTVECTOR_1);
                    final String feature_2_R = intent.getExtras().getString(ThingyUtils.EXTRA_DATA_RESULTVECTOR_2);
                    final String feature_3_R = intent.getExtras().getString(ThingyUtils.EXTRA_DATA_RESULTVECTOR_3);
                    final String feature_4_R = intent.getExtras().getString(ThingyUtils.EXTRA_DATA_RESULTVECTOR_4);
                    final String feature_5_R = intent.getExtras().getString(ThingyUtils.EXTRA_DATA_RESULTVECTOR_5);
                    final String feature_6_R = intent.getExtras().getString(ThingyUtils.EXTRA_DATA_RESULTVECTOR_6);
                    final String feature_7_R = intent.getExtras().getString(ThingyUtils.EXTRA_DATA_RESULTVECTOR_7);
                    final String feature_8_R = intent.getExtras().getString(ThingyUtils.EXTRA_DATA_RESULTVECTOR_8);
                    final String feature_9_R = intent.getExtras().getString(ThingyUtils.EXTRA_DATA_RESULTVECTOR_9);
                    final String feature_10_R = intent.getExtras().getString(ThingyUtils.EXTRA_DATA_RESULTVECTOR_10);
                    final String feature_11_R = intent.getExtras().getString(ThingyUtils.EXTRA_DATA_RESULTVECTOR_11);
                    final String feature_12_R = intent.getExtras().getString(ThingyUtils.EXTRA_DATA_RESULTVECTOR_12);
                    final String feature_13_R = intent.getExtras().getString(ThingyUtils.EXTRA_DATA_RESULTVECTOR_13);
                    final String feature_14_R = intent.getExtras().getString(ThingyUtils.EXTRA_DATA_RESULTVECTOR_14);
                    final String feature_15_R = intent.getExtras().getString(ThingyUtils.EXTRA_DATA_RESULTVECTOR_15);
                    Log.d("PME_result_history: ", vectorLength_R + " " + feature_0_R + " " + feature_1_R + " " + feature_2_R + " " + feature_3_R
                            + " " + feature_4_R + " " + feature_5_R + " " + feature_6_R + " " + feature_7_R + " " + feature_8_R + " " + feature_9_R + " " + feature_10_R
                            + " " + feature_11_R + " " + feature_12_R + " " + feature_13_R + " " + feature_14_R + " " + feature_15_R);
                    if (globalListener != null) {
                        globalListener.onResultVectorValueChangedEvent(device, vectorLength_R, feature_0_R, feature_1_R, feature_2_R
                                , feature_3_R, feature_4_R, feature_5_R, feature_6_R, feature_7_R, feature_8_R, feature_9_R, feature_10_R, feature_11_R
                                , feature_12_R, feature_13_R, feature_14_R, feature_15_R);
                    }
                    if (thingyListener != null) {
                        thingyListener.onResultVectorValueChangedEvent(device, vectorLength_R, feature_0_R, feature_1_R, feature_2_R
                                , feature_3_R, feature_4_R, feature_5_R, feature_6_R, feature_7_R, feature_8_R, feature_9_R, feature_10_R, feature_11_R
                                , feature_12_R, feature_13_R, feature_14_R, feature_15_R);
                    }
                case ThingyUtils.PRESSURE_NOTIFICATION:
                    final String pressure = intent.getExtras().getString(ThingyUtils.EXTRA_DATA);
                    if (globalListener != null) {
                        globalListener.onPressureValueChangedEvent(device, pressure);
                    }

                    if (thingyListener != null) {
                        thingyListener.onPressureValueChangedEvent(device, pressure);
                    }
                    break;
                case ThingyUtils.HUMIDITY_NOTIFICATION:
                    final String humidity = intent.getExtras().getString(ThingyUtils.EXTRA_DATA);
                    if (globalListener != null) {
                        globalListener.onHumidityValueChangedEvent(device, humidity);
                    }

                    if (thingyListener != null) {
                        thingyListener.onHumidityValueChangedEvent(device, humidity);
                    }
                    break;
                case ThingyUtils.AIR_QUALITY_NOTIFICATION:
                    final int eco2 = intent.getExtras().getInt(ThingyUtils.EXTRA_DATA_ECO2);
                    final int tvoc = intent.getExtras().getInt(ThingyUtils.EXTRA_DATA_TVOC);
                    if (globalListener != null) {
                        globalListener.onAirQualityValueChangedEvent(device, eco2, tvoc);
                    }

                    if (thingyListener != null) {
                        thingyListener.onAirQualityValueChangedEvent(device, eco2, tvoc);
                    }
                    break;
                case ThingyUtils.COLOR_NOTIFICATION:
                    final float r = intent.getExtras().getFloat(ThingyUtils.EXTRA_DATA_RED);
                    final float g = intent.getExtras().getFloat(ThingyUtils.EXTRA_DATA_GREEN);
                    final float b = intent.getExtras().getFloat(ThingyUtils.EXTRA_DATA_BLUE);
                    final float a = intent.getExtras().getFloat(ThingyUtils.EXTRA_DATA_CLEAR);
                    if (globalListener != null) {
                        globalListener.onColorIntensityValueChangedEvent(device, r, g, b, a);
                    }

                    if (thingyListener != null) {
                        thingyListener.onColorIntensityValueChangedEvent(device, r, g, b, a);
                    }
                    break;
                case ThingyUtils.BUTTON_STATE_NOTIFICATION:
                    final int state = intent.getIntExtra(ThingyUtils.EXTRA_DATA_BUTTON, -1);
                    if (globalListener != null) {
                        globalListener.onButtonStateChangedEvent(device, state);
                    }

                    if (thingyListener != null) {
                        thingyListener.onButtonStateChangedEvent(device, state);
                    }
                    break;
                case ThingyUtils.TAP_NOTIFICATION:
                    final int direction = intent.getExtras().getInt(ThingyUtils.EXTRA_DATA_TAP_DIRECTION);
                    final int tapCount = intent.getExtras().getInt(ThingyUtils.EXTRA_DATA_TAP_COUNT);
                    if (globalListener != null) {
                        globalListener.onTapValueChangedEvent(device, direction, tapCount);
                    }

                    if (thingyListener != null) {
                        thingyListener.onTapValueChangedEvent(device, direction, tapCount);
                    }
                    break;
                case ThingyUtils.ORIENTATION_NOTIFICATION:
                    final int orientation = intent.getExtras().getInt(ThingyUtils.EXTRA_DATA);
                    if (globalListener != null) {
                        globalListener.onOrientationValueChangedEvent(device, orientation);
                    }

                    if (thingyListener != null) {
                        thingyListener.onOrientationValueChangedEvent(device, orientation);
                    }
                    break;
                case ThingyUtils.QUATERNION_NOTIFICATION:
                    final float w = intent.getExtras().getFloat(ThingyUtils.EXTRA_DATA_QUATERNION_W);
                    final float x = intent.getExtras().getFloat(ThingyUtils.EXTRA_DATA_QUATERNION_X);
                    final float y = intent.getExtras().getFloat(ThingyUtils.EXTRA_DATA_QUATERNION_Y);
                    final float z = intent.getExtras().getFloat(ThingyUtils.EXTRA_DATA_QUATERNION_Z);
                    if (globalListener != null) {
                        globalListener.onQuaternionValueChangedEvent(device, w, x, y, z);
                    }

                    if (thingyListener != null) {
                        thingyListener.onQuaternionValueChangedEvent(device, w, x, y, z);
                    }
                    break;
                case ThingyUtils.PEDOMETER_NOTIFICATION:
                    final int stepCount = intent.getExtras().getInt(ThingyUtils.EXTRA_DATA_STEP_COUNT);
                    final long duration = intent.getExtras().getLong(ThingyUtils.EXTRA_DATA_DURATION);
                    if (globalListener != null) {
                        globalListener.onPedometerValueChangedEvent(device, stepCount, duration);
                    }

                    if (thingyListener != null) {
                        thingyListener.onPedometerValueChangedEvent(device, stepCount, duration);
                    }
                    break;
                case ThingyUtils.RAW_DATA_NOTIFICATION:

                    float compassX = intent.getExtras().getFloat(ThingyUtils.EXTRA_DATA_COMPASS_X);
                    float compassY = intent.getExtras().getFloat(ThingyUtils.EXTRA_DATA_GYROSCOPE_Y);
                    float compassZ = intent.getExtras().getFloat(ThingyUtils.EXTRA_DATA_GYROSCOPE_Z);

                    if (globalListener != null) {
                        globalListener.onAcelGyroValueChangedEvent(device, accelerometerX, accelerometerY, accelerometerZ, gyroscopeX, gyroscopeY, gyroscopeZ);
                        /* globalListener.onAccelerometerValueChangedEvent( device, accelerometerX, accelerometerY, accelerometerZ);
                       globalListener.onCompassValueChangedEvent(device, compassX, compassY, compassZ); */
                    }
                    //MHE//6개의 값 변화 listener입니다 global 보다는 thingy쪽에서 쓰이는듯합니다
                    if (thingyListener != null) {
                        thingyListener.onAcelGyroValueChangedEvent(device, accelerometerX, accelerometerY, accelerometerZ, gyroscopeX, gyroscopeY, gyroscopeZ);
                        /*thingyListener.onAccelerometerValueChangedEvent(device, accelerometerX, accelerometerY, accelerometerZ);
                        thingyListener.onCompassValueChangedEvent(device, compassX, compassY, compassZ);*/
                    }

                    break;
                case ThingyUtils.EULER_NOTIFICATION:
                    final float roll = intent.getExtras().getFloat(ThingyUtils.EXTRA_DATA_ROLL);
                    final float pitch = intent.getExtras().getFloat(ThingyUtils.EXTRA_DATA_PITCH);
                    final float yaw = intent.getExtras().getFloat(ThingyUtils.EXTRA_DATA_YAW);
                    if (globalListener != null) {
                        globalListener.onEulerAngleChangedEvent(device, roll, pitch, yaw);
                    }

                    if (thingyListener != null) {
                        thingyListener.onEulerAngleChangedEvent(device, roll, pitch, yaw);
                    }
                    break;
                case ThingyUtils.ROTATION_MATRIX_NOTIFICATION:
                    final byte[] rotationMatrix = intent.getExtras().getByteArray(ThingyUtils.EXTRA_DATA_ROTATION_MATRIX);
                    if (globalListener != null) {
                        globalListener.onRotationMatixValueChangedEvent(device, rotationMatrix);
                    }

                    if (thingyListener != null) {
                        thingyListener.onRotationMatixValueChangedEvent(device, rotationMatrix);
                    }
                    break;
                case ThingyUtils.HEADING_NOTIFICATION:
                    final float heading = intent.getExtras().getFloat(ThingyUtils.EXTRA_DATA);
                    if (globalListener != null) {
                        globalListener.onHeadingValueChangedEvent(device, heading);
                    }

                    if (thingyListener != null) {
                        thingyListener.onHeadingValueChangedEvent(device, heading);
                    }
                    break;
                case ThingyUtils.GRAVITY_NOTIFICATION:

                    if (globalListener != null) {//MHE//사용하지 않습니다.
                        //globalListener.onGravityVectorChangedEvent(device, accelerometerX, accelerometerY, accelerometerZ, gyroscopeX, gyroscopeY, gyroscopeZ);
                        //globalListener.onGyroscopeValueChangedEvent(device, accelerometerX, accelerometerY, accelerometerZ, gyroscopeX, gyroscopeY, gyroscopeZ);
                        /*globalListener.onAccelerometerValueChangedEvent(device, accelerometerX, accelerometerY, accelerometerZ);
                       globalListener.onCompassValueChangedEvent(device, compassX, compassY, compassZ);*/
                    }

                    if (thingyListener != null) {
                        //thingyListener.onGravityVectorChangedEvent(device, accelerometerX, accelerometerY, accelerometerZ, gyroscopeX, gyroscopeY, gyroscopeZ);
                        //thingyListener.onGyroscopeValueChangedEvent(device, accelerometerX, accelerometerY, accelerometerZ, gyroscopeX, gyroscopeY, gyroscopeZ);
                        /*thingyListener.onAccelerometerValueChangedEvent(device, accelerometerX, accelerometerY, accelerometerZ);
                        thingyListener.onCompassValueChangedEvent(device, compassX, compassY, compassZ);*/
                    }
                    break;
                case ThingyUtils.SPEAKER_STATUS_NOTITIFCATION:
                    final int status = intent.getExtras().getInt(ThingyUtils.EXTRA_DATA_SPEAKER_STATUS_NOTITIFCATION);
                    if (globalListener != null) {
                        globalListener.onSpeakerStatusValueChangedEvent(device, status);
                    }

                    if (thingyListener != null) {
                        thingyListener.onSpeakerStatusValueChangedEvent(device, status);
                    }
                    break;
                case ThingyUtils.MICROPHONE_NOTITIFCATION:
                    final byte[] data = intent.getExtras().getByteArray(ThingyUtils.EXTRA_DATA_PCM);
                    if (globalListener != null) {
                        globalListener.onMicrophoneValueChangedEvent(device, data);
                    }

                    if (thingyListener != null) {
                        thingyListener.onMicrophoneValueChangedEvent(device, data);
                    }
                    break;
            }
        }
    }

    /**
     * Registers the {@link ThingyListener}. Registered listener will receive the progress events from the BaseThingy service.
     *
     * @param context  the application context
     * @param listener the listener to register
     */
    public static void registerThingyListener(final Context context, final ThingyListener listener) {
        if (mThingyBroadcastReceiver == null) {
            mThingyBroadcastReceiver = new ThingyBroadcastReceiver();

            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ThingyUtils.ACTION_DEVICE_CONNECTED);
            intentFilter.addAction(ThingyUtils.ACTION_DEVICE_DISCONNECTED);
            intentFilter.addAction(ThingyUtils.ACTION_SERVICE_DISCOVERY_COMPLETED);
            intentFilter.addAction(ThingyUtils.EXTRA_DEVICE);
            intentFilter.addAction(ThingyUtils.BATTERY_LEVEL_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.TEMPERATURE_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.PRESSURE_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.HUMIDITY_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.AIR_QUALITY_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.COLOR_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.BUTTON_STATE_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.TAP_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.ORIENTATION_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.QUATERNION_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.PEDOMETER_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.RAW_DATA_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.EULER_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.ROTATION_MATRIX_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.HEADING_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.GRAVITY_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.SPEAKER_STATUS_NOTITIFCATION);
            intentFilter.addAction(ThingyUtils.MICROPHONE_NOTITIFCATION);
            //MHE//feature를 위해서
            intentFilter.addAction(ThingyUtils.CLASSIFICATION_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.FEATUREVECTOR_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.RESULTVECTOR_NOTIFICATION);

            LocalBroadcastManager.getInstance(context).registerReceiver(mThingyBroadcastReceiver, intentFilter);
        }
        mThingyBroadcastReceiver.setThingyListener(listener);
    }

    /**
     * Registers the {@link ThingyListener}. Registered listener will receive the progress events from the BaseThingy service.
     *
     * @param context  the application context
     * @param listener the listener to register
     */
    public static void registerThingyListener(final Context context, final ThingyListener listener, final BluetoothDevice device) {
        if (mThingyBroadcastReceiver == null) {
            mThingyBroadcastReceiver = new ThingyBroadcastReceiver();

            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ThingyUtils.ACTION_DEVICE_CONNECTED);
            intentFilter.addAction(ThingyUtils.ACTION_DEVICE_DISCONNECTED);
            intentFilter.addAction(ThingyUtils.ACTION_SERVICE_DISCOVERY_COMPLETED);
            intentFilter.addAction(ThingyUtils.EXTRA_DEVICE);
            intentFilter.addAction(ThingyUtils.BATTERY_LEVEL_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.TEMPERATURE_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.PRESSURE_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.HUMIDITY_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.AIR_QUALITY_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.COLOR_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.BUTTON_STATE_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.TAP_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.ORIENTATION_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.QUATERNION_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.PEDOMETER_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.RAW_DATA_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.EULER_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.ROTATION_MATRIX_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.HEADING_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.GRAVITY_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.SPEAKER_STATUS_NOTITIFCATION);
            intentFilter.addAction(ThingyUtils.MICROPHONE_NOTITIFCATION);
            //MHE//feature를 위해서
            intentFilter.addAction(ThingyUtils.CLASSIFICATION_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.FEATUREVECTOR_NOTIFICATION);
            intentFilter.addAction(ThingyUtils.RESULTVECTOR_NOTIFICATION);

            LocalBroadcastManager.getInstance(context).registerReceiver(mThingyBroadcastReceiver, intentFilter);
        }
        mThingyBroadcastReceiver.setThingyListener(device, listener);
    }

    public static void unregisterThingyListener(final Context context, final ThingyListener listener) {
        if (mThingyBroadcastReceiver != null) {
            final boolean empty = mThingyBroadcastReceiver.removeThingyListener(listener);

            if (empty) {
                LocalBroadcastManager.getInstance(context).unregisterReceiver(mThingyBroadcastReceiver);
                mThingyBroadcastReceiver = null;
            }
        }
    }
}
