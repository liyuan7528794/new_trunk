import java.io.File;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;

/**
 * 自动生成SQL语句, 与GreenDao配合使用
 * @author ldkxingzhe
 *
 */
public class SQLiteGenerator {

	public static void main(String[] args) throws Exception {
        Schema schema = new Schema(3, "com.travel.communication.dao");
        addCommunicationData(schema);

//        addPictureData(schema);
        String path = "imserver/src/main/java";
        File file = new File(path);
        System.out.println(file.getAbsolutePath() + ", and file.exist is " + file.exists());
        new DaoGenerator().generateAll(schema, path);
	}

    private static void addPictureData(Schema schema){
        // 本地资料集合
        Entity localFile = schema.addEntity("LocalFile");
        localFile.addIdProperty();
        localFile.addStringProperty("userId");
        localFile.addStringProperty("fileName");
        localFile.addStringProperty("localPath");
        localFile.addStringProperty("remotePath");
        localFile.addStringProperty("thumbnailPath");
        localFile.addStringProperty("orderId");
        localFile.addIntProperty("type");
        localFile.addLongProperty("createTime");
        localFile.addStringProperty("address");
        localFile.addStringProperty("longitudeLatitude");
        localFile.addBooleanProperty("isUpLoaded");
        localFile.addLongProperty("duration");
        localFile.addStringProperty("others");

/*        Entity voteEntity = schema.addEntity("voteEntity");
        voteEntity.addIdProperty();
        voteEntity.addStringProperty("userId");
        voteEntity.addStringProperty("orderId");
        voteEntity.addStringProperty("files");
        voteEntity.addBooleanProperty("hasUpload");*/
    }

	private static void addCommunicationData(Schema schema) {
        // 用户信息
        Entity userData = schema.addEntity("UserData");
        userData.addStringProperty("id").notNull().primaryKey();
        userData.addStringProperty("nickName").notNull();
        userData.addStringProperty("imgUrl");

        // 消息列表
        Entity message = schema.addEntity("Message");
        message.addIdProperty();
        Property senderIdProperty = message.addStringProperty("senderId").getProperty();
        Property receiverIdProperty = message.addStringProperty("receiverId").getProperty();
        Property currentOwnerIdProperty = message.addStringProperty("ownerId").getProperty();
        message.addIntProperty("state");     // 状态      发送中, 发送成功, 发送失败 (为了省事, 加载一些文件也放在这里)
        message.addIntProperty("chatType"); // 聊天类型, 0 - 单聊, 1 -- 群聊
        message.addDateProperty("create").notNull();
        message.addToOne(userData, senderIdProperty, "sender");
        message.addToOne(userData, receiverIdProperty, "receiver");
        message.addToOne(userData, currentOwnerIdProperty, "owner");
        message.addStringProperty("content");
        message.addIntProperty("messageType"); // 消息类型:语音，　文字，　图片
        message.addLongProperty("timeLong");   // 消息类型为语音视频时的时长

        // 最后一条消息
        /**
         * 此数据库中的发送者id与接收者Id, 
         * 接收者Id是真实的接受者, 而发送者Id可以为房间号或者什么, 
         * 将其模拟成一个用户, 用于以后群聊.
         * 而且此发送者Id与接受者Id仅仅是用来表示此聊天房间的, 并不一定是真实的消息发送者, 
         * 如果需要获得真实的发送者, 请使用message数据中的senderId
         */
        Entity lastMessage = schema.addEntity("LastMessage");
        lastMessage.addIdProperty();
        Property lastMessageSenderIdProperty = lastMessage.addStringProperty("senderId").getProperty();
        Property lastMessageReceiverIdProperty = lastMessage.addStringProperty("receiverId").getProperty();
        lastMessage.addToOne(userData, lastMessageReceiverIdProperty, "receiver");
        lastMessage.addToOne(userData, lastMessageSenderIdProperty, "sender");
        lastMessage.addIntProperty("unReadNumber");
        Property lastMessageMsgProperty = lastMessage.addLongProperty("messageId").getProperty();
        lastMessage.addToOne(message, lastMessageMsgProperty);
        lastMessage.addBooleanProperty("isVisible");
    }
}
