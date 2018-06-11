package com.tencent.qcloud.suixinbo.model;


/**
 * 当前直播信息页面
 */
public class CurLiveInfo {
    private int members;
    private int admires;
    private String title;
    private double latitude = -1;
    private double longitude = -1;
    private String address = "";
    private String coverurl = "";
    private String coverId = "-1";
    private String share;
    private String activityId;
    private String rtmpStreamUrl;
    private String rtmpAddress;
    private String goodsId = "-1";
    private int liveType = 1;
    private String streamId;

    private int roomNum;
    private int mAVRoomNum;

    private  String hostID;
    private int mHLLXUserId;
    private String hostName;
    private String hostAvator;
    private String m3u8Address;

    private int currentRequestCount = 0;

    private static CurLiveInfo s_Instance;

    public static CurLiveInfo getInstance(){
        if(s_Instance == null){
            synchronized (CurLiveInfo.class){
                if(s_Instance == null){
                    s_Instance = new CurLiveInfo();
                }
            }
        }

        return s_Instance;
    }

    public static void destroyInstance(){
        if(s_Instance != null){
            synchronized (CurLiveInfo.class){
                if(s_Instance != null){
                    s_Instance = null;
                }
            }
        }
    }
    private CurLiveInfo(){}

    public int getCurrentRequestCount() {
        return currentRequestCount;
    }

    public int getIndexView() {
        return indexView;
    }

    public void setIndexView(int indexView) {
        this.indexView = indexView;
    }

    public int indexView = 0;

    public String getM3u8Address() {
        return m3u8Address;
    }

    public void setM3u8Address(String rtmpAddress) {
        this.m3u8Address = rtmpAddress;
    }

    public int getLiveType() {
        return liveType;
    }

    public void setLiveType(int liveType) {
        this.liveType = liveType;
    }

    public void setCurrentRequestCount(int currentRequestCount) {
        this.currentRequestCount = currentRequestCount;
    }

    public String getHostID() {
        return hostID;
    }

    public void setHostID(String hostID) {
        this.hostID = hostID;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostAvator() {
        return hostAvator;
    }

    public void setHostAvator(String hostAvator) {
        this.hostAvator = hostAvator;
    }

    public int getMembers() {
        return members;
    }

    public void setMembers(int members) {
        this.members = members;
    }

    public int getAdmires() {
        return admires;
    }

    public void setAdmires(int admires) {
        this.admires = admires;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getRoomNum() {
        return roomNum;
    }

    public void setRoomNum(int roomNum) {
        this.roomNum = roomNum;
    }

    public String getCoverurl() {
        return coverurl;
    }

    public void setCoverurl(String coverurl) {
        this.coverurl = coverurl;
    }

    public String getChatRoomId() {
        return "" + roomNum;
    }

    public String getShare() {
        return share;
    }

    public void setShare(String share) {
        this.share = share;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getRtmpAddress() {
        return rtmpAddress;
    }

    public void setRtmpAddress(String rtmpAddress) {
        this.rtmpAddress = rtmpAddress;
    }

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public String getCoverId() {
        return coverId;
    }

    public void setCoverId(String coverId) {
        this.coverId = coverId;
    }

    public int getHostHLLXUserId() {
        return mHLLXUserId;
    }

    public void setHostHLLXUserId(int mHLLXUserId) {
        this.mHLLXUserId = mHLLXUserId;
    }

    public int getAVRoomNum() {
        return mAVRoomNum;
    }

    public void setAVRoomNum(int avRoomNum){
        mAVRoomNum = avRoomNum;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getRtmpStreamUrl() {
        return rtmpStreamUrl;
    }

    public void setRtmpStreamUrl(String rtmpStreamUrl) {
        this.rtmpStreamUrl = rtmpStreamUrl;
    }
}
