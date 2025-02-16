/* eslint-disable */


import {UserFcmRepository} from "../../repository/UserFcmRepository";
import {Chat} from "../../models/chat/Chat";
import {messaging} from "firebase-admin";
import BatchResponse = messaging.BatchResponse;

const functions = require("firebase-functions");

const userFcmRepository:UserFcmRepository = new UserFcmRepository();


export const adminAcceptedRequesterNotifyHim = functions.region('europe-west1').firestore
  .document('chats/{chatId}')
  .onWrite(async (change: any, context: any) => {

      if (!change.before.exists)
          return null;

        const chatMessageBatchBefore: Chat = Chat.makeInstanceFromSnapshot(change.before.data());
        const chatMessageBatchAfter: Chat = Chat.makeInstanceFromSnapshot(change.after.data());

        const beforeMembersIds: string[] = chatMessageBatchBefore.getMembersIds() || [];
        const afterMembersIds: string[] = chatMessageBatchAfter.getMembersIds() || [];

        if (beforeMembersIds.length + 1 === afterMembersIds.length) {
            const requesterId:string = afterMembersIds.filter((userIdTemp:string) => !beforeMembersIds.includes(userIdTemp))[0];

            const fcmTokens:string[] = await userFcmRepository.getUserFcmTokens(requesterId);
            if (fcmTokens.length === 0)
                return null;


            const payload = {
                data: {
                    pushNotificationType: "notifyAcceptedRequester",
                    match: JSON.stringify(chatMessageBatchAfter),
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

                const response: BatchResponse = await userFcmRepository.sendEachForMulticast(fcmTokens,payload);

                console.log(`Sent ${response.successCount} messages successfully.`);
                if (response.failureCount > 0) {
                    console.error("Some messages failed:", response.responses);
                }
            } catch (error) {
                console.error("Error sending notification:", error);
            }
        }

        return null;

    })
