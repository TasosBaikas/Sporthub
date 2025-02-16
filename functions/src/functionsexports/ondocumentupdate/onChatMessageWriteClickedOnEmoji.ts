/* eslint-disable */



import {ChatMessageBatch} from "../../models/chat/ChatMessageBatch";

const functions = require("firebase-functions");
import { logger } from "firebase-functions";
import {UserRepository} from "../../repository/UserRepository";
import {UserFcmRepository} from "../../repository/UserFcmRepository";
import {ChatMessage} from "../../models/chat/ChatMessage";
import {UserShortForm} from "../../models/user/UserShortForm";
import {UserNotificationsRepository} from "../../repository/UserNotificationsRepository";
import {UserNotification} from "../../models/user/usernotification/UserNotification";


const userRepository: UserRepository = new UserRepository();
const userFcmRepository: UserFcmRepository = new UserFcmRepository();
const userNotificationsRepository: UserNotificationsRepository = new UserNotificationsRepository();


export const onChatMessageWriteClickedOnEmoji = functions.region('europe-west1').firestore
  .document('chatMessageBatch/{messageId}/chatMessageBatchesSpecificChat/{batchId}')
  .onWrite(async (change: any, context: any) => {

    // Check if 'before' document exists
    if (!change.before.exists) {
        return null;
    }

    if (!change.after.exists) {
        return null;
    }

    const beforeBatch: ChatMessageBatch = ChatMessageBatch.makeInstanceFromSnapshot(change.before.data());
    const afterBatch: ChatMessageBatch =  ChatMessageBatch.makeInstanceFromSnapshot(change.after.data());

    const beforeChatMessages: ChatMessage[] = beforeBatch.getChatMessages();
    const afterChatMessages: ChatMessage[] = afterBatch.getChatMessages();

    // Check for change in the chatMessages list size
    if (beforeChatMessages.length !== afterChatMessages.length) {
        return null;
    }

    const messageTheEmojisUpdated: { key: string; userIdThatClickedEmoji: string; messageIndex: number } | undefined = findWhichMessageTheEmojisIsUpdated(beforeChatMessages, afterChatMessages);
    logger.info("main","firstDifference",messageTheEmojisUpdated);

    if (!messageTheEmojisUpdated){
        logger.info("main","firstDifference If",messageTheEmojisUpdated);
        return null;
    }

    // Check if there was no previous data (document created)
    console.log('New chat batch created.');

    const messageThatEmojiUpdated: ChatMessage = ChatMessage.makeInstanceFromSnapshot(afterChatMessages[messageTheEmojisUpdated.messageIndex]);
    const userIdOwnerOfTheEmoji: string = messageThatEmojiUpdated.getUserShortForm().getUserId();

    if (messageTheEmojisUpdated.userIdThatClickedEmoji === userIdOwnerOfTheEmoji){
        return null;
    }
    const chatId: string = afterBatch.getChatId();

    if (!(await isUserNotificationEnabled(userIdOwnerOfTheEmoji,chatId))){
        return null;
    }
    logger.info("main",userIdOwnerOfTheEmoji);
    logger.info("main!","after isUserNotificationEnabled");

    logger.info("messageTheEmojisUpdated.userIdThatClickedEmoji",messageTheEmojisUpdated.userIdThatClickedEmoji);


    const [userDevicesTokens, userThatClickedEmoji] = await Promise.all([
        userFcmRepository.getUserFcmTokens(userIdOwnerOfTheEmoji),
        userRepository.getUserByIdPromise(messageTheEmojisUpdated.userIdThatClickedEmoji, "", false),
    ]);
    logger.info("main","userTokens",userDevicesTokens);
    logger.info("main","userIdThatClickedEmoji",userThatClickedEmoji);
    if (userThatClickedEmoji === null)
        return null;

    const userShortForm:UserShortForm = UserShortForm.makeShortForFromUser(userThatClickedEmoji,"");

    await sendNotificationToUsers(userDevicesTokens, userShortForm, messageThatEmojiUpdated, messageTheEmojisUpdated.key);  // Send notification for the new chat batch

    return null;
});

async function sendNotificationToUsers(userTokens: string[], userThatClickedEmoji:UserShortForm , messageThatEmojiUpdated:ChatMessage, emojiTypeClicked:string) {

    logger.info("getUserFromMessage","firstName","userthatclicked",userThatClickedEmoji.getFirstName());
    logger.info("getUserFromMessage","lastname","userthatclicked",userThatClickedEmoji.getLastName());

    const payload = {
      data: {
            pushNotificationType: "emojiUpdate",
            emojiTypeClicked: emojiTypeClicked,
            userThatClickedEmoji: JSON.stringify(userThatClickedEmoji),
            chatMessage: JSON.stringify(messageThatEmojiUpdated),
      },
        android: {
            priority: 'high',
        },
        apns: {
            headers: {
                'apns-priority': '10',
            },
        },
    };


    try {
        // Use sendEachForMulticast to send the notification to all tokens
        const response = await userFcmRepository.sendEachForMulticast(userTokens,payload);

        console.log(`Sent ${response.successCount} messages successfully.`);
        if (response.failureCount > 0) {
            console.error("Some messages failed:", response.responses);
        }
    } catch (error) {
        console.error("Error sending notification:", error);
    }
  }

function findWhichMessageTheEmojisIsUpdated(chatMessagesBefore: ChatMessage[], chatMessagesAfter: ChatMessage[]):{ key: string, userIdThatClickedEmoji: string, messageIndex: number } | undefined {
    for (let messageIndex = 0; messageIndex < chatMessagesBefore.length; messageIndex++) {
        if (chatMessagesBefore[messageIndex].getId() !== chatMessagesAfter[messageIndex].getId()){
            continue;
        }

        const emojisMapBefore:Map<string,string[]> = chatMessagesBefore[messageIndex].getEmojisMap();
        const emojisMapAfter:Map<string,string[]> = chatMessagesAfter[messageIndex].getEmojisMap();
        logger.info("findWhichMessageTheEmojisIsUpdated","emojisMapBefore",emojisMapBefore);
        logger.info("findWhichMessageTheEmojisIsUpdated","emojisMapAfter",emojisMapAfter);

        const firstDifference = findFirstDifferenceInMaps(emojisMapBefore, emojisMapAfter);
        if (firstDifference) {
            console.log(`First difference found at result`, {
                key: firstDifference.key,
                userIdThatClickedEmoji: firstDifference.userIdThatClickedEmoji,
                messageIndex: messageIndex
            });
            return {
                key: firstDifference.key,
                userIdThatClickedEmoji: firstDifference.userIdThatClickedEmoji,
                messageIndex: messageIndex
            };
        }

    }
    console.log(`First difference not found`);
    return undefined;
}




function findFirstDifferenceInMaps(map1: Map<string, string[]>, map2: Map<string, string[]>): { key: string, userIdThatClickedEmoji: string } | undefined {
    for (const [key, array1] of map1) {
        const array2 = map2.get(key);
        if (!array2) {
            // If array2 doesn't exist for a key, continue to the next key
            continue;
        }

        const userIdThatClickedEmoji = array2.find(item => !array1.includes(item));
        if (userIdThatClickedEmoji) {
            console.log(`First difference found at key "${key}" user that clicked ${userIdThatClickedEmoji}.`);
            return { key, userIdThatClickedEmoji };
        }
    }

    // No differences found
    return undefined;
}



async function isUserNotificationEnabled(userToSendTheNotification: string, chatId: string): Promise<boolean | undefined> {

    const userNotification:UserNotification | undefined = await userNotificationsRepository.getUserNotificationsByIdPromise(userToSendTheNotification);
    if (userNotification === undefined)
        return undefined;


    const timeNowInEpochMilli:number = Date.now();

    logger.info("userToSendTheNotification",userToSendTheNotification);


    const snoozeChatMessageTime:number = userNotification.getGeneralNotificationOptions().getSnoozeChatMessages();
    if (timeNowInEpochMilli > snoozeChatMessageTime){

        const chatNotificationsSettings:any | undefined = userNotification.getNotificationsBasedOnChats().get(chatId);
        if (chatNotificationsSettings === undefined){
            return true;
        }
        logger.info("chatNotificationsSettings",chatNotificationsSettings)

        if (timeNowInEpochMilli > chatNotificationsSettings.snoozeChatMessages){
            return true;
        }
    }

    return false;
  }

