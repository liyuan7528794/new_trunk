// IMServiceInterface.aidl
package com.travel.imserver;
import android.os.ResultReceiver;
import com.travel.imserver.bean.ClientData;
import com.travel.imserver.BaseMessage;
// Declare any non-default types here with import statements

interface IMServiceInterface {

    void register(in String parserName, in String className, in ResultReceiver resultReceiver);
    void unRegister(in String parseName);
    void login(in String msgId, in ClientData clientData);
    boolean changeRoom(in String msgId, in String roomName);
    boolean sendMessage(in BaseMessage baseMessage);
}
