package no.nordicsemi.android.nrfthingy;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.nrfthingy.common.ScannerFragmentListener;
import no.nordicsemi.android.nrfthingy.common.Utils;
import no.nordicsemi.android.thingylib.ThingyListenerHelper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Dae on 2018-02-08.
 */

public class ReceiveFragment extends Fragment implements ScannerFragmentListener {

    private ListView mReceiveView;
    private ListViewAdapter mReceiveAdapter;

    private BluetoothDevice mDevice;
    OkHttpClient client;
    MediaType JSON;
    String[] responses;
    String[] word;

    private String mSignal;

    public static ReceiveFragment newInstance(final BluetoothDevice device) {
        ReceiveFragment fragment = new ReceiveFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_receive, container, false);
        final Toolbar mReceiveToolbar = rootView.findViewById(R.id.pme_receive_toolbar);
        mReceiveToolbar.inflateMenu(R.menu.menu_receive);
        mReceiveView = rootView.findViewById(R.id.receive_list);
        mReceiveAdapter = new ListViewAdapter(getActivity());
        mReceiveView.setAdapter(mReceiveAdapter);
        if (mReceiveToolbar != null) {
            mReceiveToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    final int id = item.getItemId();
                    switch (id) {
                        case R.id.action_submit:
                            try {
                                mSignal = "0";
                                mReceiveAdapter.clear();
                                makePostRequest();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        case R.id.action_submit2:
                            try {
                                mSignal = "1";
                                mReceiveAdapter.clear();
                                makePostRequest();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                    return true;
                }
            });
        }
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
                JSONObject sObject = new JSONObject();//배열 내에 들어갈 json

                sObject.put("signal", mSignal);
                sObject.put("Mac",mDevice.getAddress());

                String getResponse = post("http://ec2-13-125-160-227.ap-northeast-2.compute.amazonaws.com/pme/get", sObject);

                return getResponse;

            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute(String getResponse) {
            if (getResponse != null) {

                responses = getResponse.split("\\{");
                for (int i = 2; i < responses.length; i++) {
                    String getdata = "";
                    responses[i] = responses[i].replaceAll("\"", "");
                    word = responses[i].split(",");

                    for (int j = 0; j < word.length; j++) {
                        getdata = word[1] + "\n" + word[2] + "\n" + word[3] + "\n" + word[4] + "\n" + word[5]+"\n"+word[6];
                    }

                    mReceiveAdapter.addItem(getdata);

                }
                mReceiveAdapter.notifyDataSetChanged();

            }
        }

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
        public TextView getdata;

    }

    private class ListViewAdapter extends BaseAdapter {
        private Context mContext = null;
        private ArrayList<ReceiveFragment.ListViewAdapter.ListData> mListData = new ArrayList<ReceiveFragment.ListViewAdapter.ListData>();

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

        public void addItem(String getdata) {
            ReceiveFragment.ListViewAdapter.ListData addInfo = null;
            addInfo = new ListViewAdapter.ListData();
            addInfo.getdata = getdata;


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
            mReceiveAdapter.notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ReceiveFragment.ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_get, null);


                holder.getdata = (TextView) convertView.findViewById(R.id.get_data);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ListViewAdapter.ListData mData = mListData.get(position);


            holder.getdata.setText(mData.getdata);

            return convertView;
        }

        private class ListData {
            public String getdata;


        }
    }

    @Override
    public void onDeviceSelected(BluetoothDevice device, String name) {

    }

    @Override
    public void onNothingSelected() {

    }
}
