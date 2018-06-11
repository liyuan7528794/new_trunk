package com.travel.localfile.pk.others;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.travel.Constants;
import com.travel.communication.entity.UserData;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/10.
 */

public class VoteInfoHelper {
    private final static String TAG = "VoteInfoHelper";
    public final static int VOTED_BUYER = 0;
    public final static int VOTED_SELLER = 1;

    private Context context;
    private int voteId;
    private int sellerId;
    private int buyerId;
    private VoteHttpListener listener;

    public interface VoteHttpListener {
        void OnVoteDetail(VoteDetailsInfo info);

        void OnVotedUsers(HashMap<Integer, Integer> userMap);

        void OnEvidencePackets(JSONArray data);

        void onPublishVoteResult(boolean isSuccess);
        /** 投票是否成功*/
        void onVoteResult(boolean isSuccess);
    }

    public VoteInfoHelper(Context context, int voteId, VoteHttpListener listener) {
        this.context = context;
        this.voteId = voteId;
        this.listener = listener;
    }

    public void setBuyerAndSellerId(int buyerId, int sellerId) {
        this.buyerId = buyerId;
        this.sellerId = sellerId;
    }

    /**
     * 众投发表
     */
    public void publishVote() {
        String url = Constants.Root_Url + "/publicVote/publish.do";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("publicVoteId", voteId);
        NetWorkUtil.postForm(context, url, new MResponseListener(context) {
            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    listener.onPublishVoteResult(true);
                }
            }

            @Override
            protected void onErrorNotZero(int error, String msg) {
                listener.onPublishVoteResult(false);
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onPublishVoteResult(false);
            }
        }, map);
    }

    /**
     * 获取证据列表数据
     */
    public void getVoteDataList() {
        getUsersByVoted();

        String url = Constants.Root_Url + "/VoteData/voteDataList.do";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("publicVoteId", voteId);
        NetWorkUtil.postForm(context, url, new MResponseListener() {

            @Override
            protected void onDataFine(JSONArray data) {
                if (data == null)
                    return;
                listener.OnEvidencePackets(data);
            }
        }, map);
    }

    /* 获取本人是否已经投过票 */
    public void getIsVoted(final String supportUserId) {
        String url = Constants.Root_Url + "/orders/isVote.do";
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("publicVoteId", voteId);
        NetWorkUtil.postForm(context, url, new MResponseListener() {
            @Override
            public void onResponse(JSONObject response) {
                MLog.v(TAG, "onResponse, and response is " + response);
                int error = JsonUtil.getJsonInt(response, "error");
                if (error != 0)
                    return;
                String msg = JsonUtil.getJson(response, "msg");
                if (TextUtils.isEmpty(msg) || !msg.toUpperCase().equals("OK"))
                    return;

                int result = JsonUtil.getJsonInt(response, "data");
                if (result == 1) {
                    Toast.makeText(context, "您已投过票", Toast.LENGTH_SHORT).show();
                } else {
                    vote(supportUserId);
                }
            }
        }, map);
    }

    /* 获取已经投过票的用户 */
    private void getUsersByVoted() {
        String url = Constants.Root_Url + "/Vote/voteList.do";
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("publicVoteId", voteId);
        NetWorkUtil.postForm(context, url, new MResponseListener() {

            @Override
            protected void onDataFine(JSONArray data) {
                super.onDataFine(data);
                HashMap<Integer, Integer> hashMap = new HashMap<Integer, Integer>();
                if (data == null || data.length() == 0) {
                    listener.OnVotedUsers(hashMap);
                    return;
                }

                try {
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject jsonObject = data.getJSONObject(i);
                        int selectedId = jsonObject.getInt("selectedId");
                        if (selectedId == buyerId) {
                            hashMap.put(jsonObject.getInt("voter"), VOTED_BUYER);
                        }
                        if (selectedId == sellerId) {
                            hashMap.put(jsonObject.getInt("voter"), VOTED_SELLER);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    listener.OnVotedUsers(hashMap);
                }

            }
        }, map);
    }

    public void vote(String supportUserId) {
        String url = Constants.Root_Url + "/orders/addVote.do";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("publicVoteId", voteId);
        map.put("selectedId", supportUserId);
        NetWorkUtil.postForm(context, url, new MResponseListener() {
            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    Toast.makeText(context, "投票成功", Toast.LENGTH_SHORT).show();
                    listener.onVoteResult(true);
                }
            }
        }, map);
    }

    public void getPublicVoteDetail() {
        String url = Constants.Root_Url + "/publicVote/detailPublicVote.do";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", voteId);
        NetWorkUtil.postForm(context, url, new MResponseListener() {
            @Override
            protected void onDataFine(JSONObject data) {
                if (data == null)
                    return;
                VoteDetailsInfo info = new VoteDetailsInfo();
                info.setOrderId(JsonUtil.getJsonLong(data, "ordersId"));
                info.setBuyerId(JsonUtil.getJsonInt(data, "sellerId"));
                info.setSellerId(JsonUtil.getJsonInt(data, "buyerId"));
                info.setClaimAmount(JsonUtil.getJsonDouble(data, "claimAmount"));
                info.setReason(JsonUtil.getJson(data, "reason"));
                info.setCreateTime(JsonUtil.getJson(data, "createTime"));
                info.setCheckTime(JsonUtil.getJson(data, "checkTime"));
                info.setBuyerPoll(JsonUtil.getJsonInt(data, "buyerPoll"));
                info.setSellerPoll(JsonUtil.getJsonInt(data, "sellerPoll"));
                info.setTotalPrice(JsonUtil.getJsonDouble(data, "totalMoney"));
                info.setSnapshotId(JsonUtil.getJsonLong(data, "snapshotId"));
                info.setVoteRedCoin(JsonUtil.getJsonInt(data, "voteRedCoin"));
                info.setStatus(JsonUtil.getJsonInt(data, "status"));
                info.setCheckStatus(JsonUtil.getJsonInt(data, "checkStatus"));
                info.setVictory(JsonUtil.getJson(data, "victory"));
                info.setCover(JsonUtil.getJson(data, "cover"));
                info.setSubhead(JsonUtil.getJson(data, "subhead"));
                info.setPaymentPrice(JsonUtil.getJsonDouble(data, "payMoney"));
                try {
                    UserData buyer = UserData.generateUserData((JSONObject) data.get("buyer"));
                    if (buyer != null) {
                        buyer.setId(JsonUtil.getJson(data, "buyerId"));
                    }
                    UserData seller = UserData.generateUserData((JSONObject) data.get("seller"));
                    if (seller != null) {
                        seller.setId(JsonUtil.getJson(data, "sellerId"));
                    }

                    info.setBuyer(buyer);
                    info.setSeller(seller);
                } catch (JSONException e) {
                    MLog.e(TAG, e.getMessage(), e);
                } finally {
                    listener.OnVoteDetail(info);
                }
            }
        }, map);
    }

    public class VoteDetailsInfo {
        private long orderId;
        private int sellerId;
        private int buyerId;
        private double claimAmount;
        private String reason;
        private String createTime;
        private String checkTime;
        private int buyerPoll;
        private int sellerPoll;
        private double totalPrice;
        private long snapshotId;
        private int voteRedCoin;
        private int status;
        private int checkStatus;
        private String victory;
        private String cover;
        private UserData buyer;
        private UserData seller;
        private String subhead;
        private double paymentPrice;

        public long getOrderId() {
            return orderId;
        }

        public void setOrderId(long orderId) {
            this.orderId = orderId;
        }

        public int getSellerId() {
            return sellerId;
        }

        public void setSellerId(int sellerId) {
            this.sellerId = sellerId;
        }

        public int getBuyerId() {
            return buyerId;
        }

        public void setBuyerId(int buyerId) {
            this.buyerId = buyerId;
        }

        public double getClaimAmount() {
            return claimAmount;
        }

        public void setClaimAmount(double claimAmount) {
            this.claimAmount = claimAmount;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }

        public int getBuyerPoll() {
            return buyerPoll;
        }

        public void setBuyerPoll(int buyerPoll) {
            this.buyerPoll = buyerPoll;
        }

        public int getSellerPoll() {
            return sellerPoll;
        }

        public void setSellerPoll(int sellerPoll) {
            this.sellerPoll = sellerPoll;
        }

        public double getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(double totalPrice) {
            this.totalPrice = totalPrice;
        }

        public long getSnapshotId() {
            return snapshotId;
        }

        public void setSnapshotId(long snapshotId) {
            this.snapshotId = snapshotId;
        }

        public int getVoteRedCoin() {
            return voteRedCoin;
        }

        public void setVoteRedCoin(int voteRedCoin) {
            this.voteRedCoin = voteRedCoin;
        }

        public String getCheckTime() {
            return checkTime;
        }

        public void setCheckTime(String checkTime) {
            this.checkTime = checkTime;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public int getCheckStatus() {
            return checkStatus;
        }

        public void setCheckStatus(int checkStatus) {
            this.checkStatus = checkStatus;
        }

        public String getVictory() {
            return victory;
        }

        public void setVictory(String victory) {
            this.victory = victory;
        }

        public String getCover() {
            return cover;
        }

        public void setCover(String cover) {
            this.cover = cover;
        }

        public UserData getBuyer() {
            return buyer;
        }

        public void setBuyer(UserData buyer) {
            this.buyer = buyer;
        }

        public UserData getSeller() {
            return seller;
        }

        public void setSeller(UserData seller) {
            this.seller = seller;
        }

        public String getSubhead() {
            return subhead;
        }

        public void setSubhead(String subhead) {
            this.subhead = subhead;
        }

        public double getPaymentPrice() {
            return paymentPrice;
        }

        public void setPaymentPrice(double paymentPrice) {
            this.paymentPrice = paymentPrice;
        }
    }
}
