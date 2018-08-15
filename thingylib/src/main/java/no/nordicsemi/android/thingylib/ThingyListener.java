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
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.List;

public interface ThingyListener  {
    //Connection state listener callbacks
    void onDeviceConnected(BluetoothDevice device, int connectionState);

    void onDeviceDisconnected(BluetoothDevice device, int connectionState);

    void onServiceDiscoveryCompleted(BluetoothDevice device);

    void onBatteryLevelChanged(final BluetoothDevice bluetoothDevice, final int batteryLevel);

    void onTemperatureValueChangedEvent(final BluetoothDevice bluetoothDevice, final String temperature);

    void onPressureValueChangedEvent(final BluetoothDevice bluetoothDevice, final String pressure);

    void onHumidityValueChangedEvent(final BluetoothDevice bluetoothDevice, final String humidity);

    void onAirQualityValueChangedEvent(final BluetoothDevice bluetoothDevice, final int eco2, final int tvoc);

    void onColorIntensityValueChangedEvent(final BluetoothDevice bluetoothDevice, final float red, final float green, final float blue, final float alpha);

    void onButtonStateChangedEvent(final BluetoothDevice bluetoothDevice, final int buttonState);

    void onTapValueChangedEvent(final BluetoothDevice bluetoothDevice, final int direction, final int count);

    void onOrientationValueChangedEvent(final BluetoothDevice bluetoothDevice, final int orientation);

    void onQuaternionValueChangedEvent(final BluetoothDevice bluetoothDevice, final float w, final float x, final float y, final float z );

    void onPedometerValueChangedEvent(final BluetoothDevice bluetoothDevice,final int steps, final long duration);

    void onAccelerometerValueChangedEvent(final BluetoothDevice bluetoothDevice, final float accelerometerX, final float accelerometerY, final float accelerometerZ);
    //MHE//petfragment 에서 사용합니다.
    void onKnowledgePackValueChangedEvent(final BluetoothDevice bluetoothDevice, final String status, final String indicator);
    //MHE//pmefragment 에서 사용합니다.
    void onFeatureVectorValueChangedEvent(final BluetoothDevice bluetoothDevice, final String len, final String x, final String y, final String z);
    void onResultVectorValueChangedEvent(final BluetoothDevice bluetoothDevice, final String R_0, final String R_1, final String R_2
            , final String R_3, final String R_4, final String R_5, final String R_6, final String R_7, final String R_8, final String R_9
            , final String R_10, final String R_11, final String R_12, final String R_13, final String R_14, final String R_15);
    //MHE//원래 onAccelerometer와 onGyroscope를 합쳤습니다.
    void onAcelGyroValueChangedEvent(final BluetoothDevice bluetoothDevice, final float ax, final float ay, final float az, final float gx, final float gy, final float gz);

    void onCompassValueChangedEvent(final BluetoothDevice bluetoothDevice, final float x, final float y, final float z);

    void onEulerAngleChangedEvent(final BluetoothDevice bluetoothDevice, final float roll, final float pitch, final float yaw);

    void onRotationMatixValueChangedEvent(final BluetoothDevice bluetoothDevice, final byte [] matrix);

    void onHeadingValueChangedEvent(final BluetoothDevice bluetoothDevice, final float heading);

    void onGravityVectorChangedEvent(final BluetoothDevice bluetoothDevice, final float x, final float y, final float z
            ,final float x2,final float y2,final float z2);

    void onSpeakerStatusValueChangedEvent(final BluetoothDevice bluetoothDevice, final int status);

    void onMicrophoneValueChangedEvent(final BluetoothDevice bluetoothDevice, final byte [] data);

    void connectionCheck();
}
