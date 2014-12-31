package me.yeojoy.microdustwarning.network;

import android.content.Context;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.List;

import me.yeojoy.microdustwarning.DustConstants;
import me.yeojoy.microdustwarning.db.SqliteManager;
import me.yeojoy.microdustwarning.entity.DustInfoDto;
import me.yeojoy.microdustwarning.util.DustLog;
import me.yeojoy.microdustwarning.util.DustSharedPreferences;
import me.yeojoy.microdustwarning.util.TextDataUtil;

/**
 * Created by yeojoy on 14. 12. 22..
 */
public class DustNetworkManager implements DustConstants {
    private static final String TAG = DustNetworkManager.class.getSimpleName();
    private static DustNetworkManager mDustNetworkManager;
    
    private static Context mContext;
    
    public static DustNetworkManager getInstance(Context context) {
        if (mDustNetworkManager == null)
            mDustNetworkManager = new DustNetworkManager();
        
        init(context);
        return mDustNetworkManager;
    }
    
    private static void init(Context context) {
        mContext = context;

        if (!DustSharedPreferences.getInstance().hasPrefs())
            DustSharedPreferences.getInstance().init(mContext);
    }

    public void getMicrodustInfo() {
        DustLog.i(TAG, "getMicrodustInfo()");

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(CLEAN_AIR_API_ADDRESS).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                DustLog.i(TAG, "onFailure()\n" + e.getMessage());

                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                DustLog.i(TAG, "onResponse()");

                if (response.body() == null) {
                    Toast.makeText(mContext, "데이터 body가 없습니다.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }


                List<DustInfoDto> dtoList = TextDataUtil.parseRawXmlString(mContext,
                        response.body().string());

                // DB에 저장
                SqliteManager manager = SqliteManager.getInstance(mContext);
                manager.saveData(dtoList);

                mOnReceiveDataListener.onReceiveData(dtoList);
            }
        });
    }

    public interface OnReceiveDataListener {
        public void onReceiveData(List<DustInfoDto> data);
    }

    public void setOnReceiveDataListener(OnReceiveDataListener onReceiveDataListener) {
        this.mOnReceiveDataListener = onReceiveDataListener;
    }

    private OnReceiveDataListener mOnReceiveDataListener;
    
    
}
