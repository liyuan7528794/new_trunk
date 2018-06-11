package net.sourceforge.simcpux;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.travel.Constants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AppRegister extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		final IWXAPI msgApi = WXAPIFactory.createWXAPI(context, null);

		msgApi.registerApp(Constants.APP_ID);
	}
}
