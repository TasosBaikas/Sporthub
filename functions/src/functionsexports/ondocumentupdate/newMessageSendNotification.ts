/* eslint-disable */

import {UserFcmRepository} from "../../repository/UserFcmRepository";

const functions = require("firebase-functions");
import { logger } from "firebase-functions";
import {UserNotificationsRepository} from "../../repository/UserNotificationsRepository";
import {UserNotification} from "../../models/user/usernotification/UserNotification";
import {ChatRepository} from "../../repository/ChatRepository";
import {Chat} from "../../models/chat/Chat";
import {ChatMessageBatch} from "../../models/chat/ChatMessageBatch";
import {ChatMessage} from "../../models/chat/ChatMessage";

const userFcmRepository:UserFcmRepository = new UserFcmRepository();
const userNotificationRepository:UserNotificationsRepository = new UserNotificationsRepository();
const chatRepository:ChatRepository = new ChatRepository();

export const newMessageSendNotification = functions.region('europe-west1').firestore
  .document('chatMessageBatch/{messageId}/chatMessageBatchesSpecificChat/{batchId}')
  .onWrite(async (change:any, context: any) => {

    let beforeMessages: ChatMessage[] | null = null;
    if (change.before.exists) {
        const chatMessageBatchBefore: ChatMessageBatch = ChatMessageBatch.makeInstanceFromSnapshot(change.before.data());
        beforeMessages = chatMessageBatchBefore.getChatMessages() || [];
    }

    const chatMessageBatchAfter: ChatMessageBatch = ChatMessageBatch.makeInstanceFromSnapshot(change.after.data());
    const afterMessages: ChatMessage[] = chatMessageBatchAfter.getChatMessages() || [];

    console.log(`1`);
    // Check if there was no previous data (document created)
    if (!change.before.exists || afterMessages.length === beforeMessages!.length + 1) {

        const chatId: string = chatMessageBatchAfter.getChatId();
        console.log(`2`);

        const chat:Chat | undefined = await chatRepository.getChatById(chatId);
        console.log(`3`);

        if (chat === undefined){
            return null;
        }
        console.log(`4`);

        const newChatMessage:ChatMessage = afterMessages[0];
        if (newChatMessage.isFirstMessage())
            return null;

        const userThatSendMessage:string = newChatMessage.getUserShortForm().getUserId();

        const userIdsExceptTheMessenger:string[] = chat.getMembersIds().filter((userId:any) => userId !== userThatSendMessage);
        const usersIds:string[] = await usersThatNotificationIsEnabled(userIdsExceptTheMessenger,chatId);
        console.log(`5`);


        const promisesGetFcmTokensFromUsers:Promise<string[]>[] = []
        usersIds.forEach((userId:string) => promisesGetFcmTokensFromUsers.push(userFcmRepository.getUserFcmTokens(userId)));


        logger.info("chat",chat);
        const userTokens = await Promise.all(promisesGetFcmTokensFromUsers);

        await sendNotificationToUsers(userTokens.flat(), chat);  // Send notification for the new chat batch
        return null;
    }

    return null;
});

async function sendNotificationToUsers(tokens: string[], chat:any) {

  logger.info("chat",chat);

  const payload = {
    data: {
        pushNotificationType: "newMessage",
        chat: JSON.stringify(chat),
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
      const response = await userFcmRepository.sendEachForMulticast(tokens,payload);

      console.log(`Sent ${response.successCount} messages successfully.`);
      if (response.failureCount > 0) {
          console.error("Some messages failed:", response.responses);
      }
  } catch (error) {
      console.error("Error sending notification:", error);
  }
}

async function usersThatNotificationIsEnabled(userIdsExceptTheMesseger: string[], chatId: string): Promise<string[]> {

  const userNotifications:(UserNotification | undefined)[] = await Promise.all(userIdsExceptTheMesseger.map((userId:string) => {
      return userNotificationRepository.getUserNotificationsByIdPromise(userId);
  }));

  if (userNotifications === undefined)
      return [];

  const timeNowInEpochMilli:number = Date.now();

  const usersToSendNotification:string[] = [];
    userNotifications.forEach(userNotification => {

        if (!userNotification || !userNotification.getUserId())
            return;

        const snoozeChatMessageTime:number = userNotification.getGeneralNotificationOptions().getSnoozeChatMessages();
        if (timeNowInEpochMilli > snoozeChatMessageTime){

          const chatNotificationsSettings:any | undefined = userNotification.getNotificationsBasedOnChats().get(chatId);
          logger.info("chatNotificationsSettings",chatNotificationsSettings)

          if (chatNotificationsSettings === undefined){
              usersToSendNotification.push(userNotification.getUserId());
              return;
          }

          if (timeNowInEpochMilli > chatNotificationsSettings.snoozeChatMessages){
              usersToSendNotification.push(userNotification.getUserId());
              return;
          }
        }
      });

  return usersToSendNotification;

}




