/* eslint-disable */

import * as admin from "firebase-admin";
import {Chat} from "../models/chat/Chat";
import {ChatTypes} from "../models/constants/ChatTypes";
import {firestore} from "firebase-admin";
import CollectionReference = firestore.CollectionReference;

export class ChatFirebaseDao {

    public getChatByIdPromise(chatId:string): Promise<FirebaseFirestore.DocumentSnapshot<FirebaseFirestore.DocumentData>> {
        return admin.firestore().collection('chats').doc(chatId).get();
    }

    public getChatThatShouldChangeToIrrelevantPromise(timeNowInEpochMilli:number): Promise<FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData>> {
        return admin.firestore().collection('chats')
            .where("chatMatchIsRelevant", "==", true)
            .where("chatType","==","match_conversation")
            .where("matchDateInUTC", "<", timeNowInEpochMilli)
            .get();
    }


    public seeIfPrivateChatAlreadyExist(user1:string , user2:string): Promise<FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData>> {
        const col:CollectionReference  = admin.firestore().collection("chats");

        return col
            .where("chatType", "==", ChatTypes.PRIVATE_CONVERSATION)
            .where("adminId", "==", user1)
            .where("membersIds", "array-contains", user2)
            .get();
    }

    public saveChat(chat: Chat): Promise<FirebaseFirestore.WriteResult> {
        return admin.firestore().collection('chats').doc(chat.getId()).set(chat.toObject());
    }

    saveChatTransaction(transaction: FirebaseFirestore.Transaction, chat: Chat) {
        const doc =  admin.firestore().collection('chats').doc(chat.getId());

        return transaction.set(doc,chat.toObject());
    }

    public updateChat(chat: Chat): Promise<FirebaseFirestore.WriteResult> {
        return this.saveChat(chat);
    }

    public getChatByIdTransaction(transaction: FirebaseFirestore.Transaction, chatId:string): Promise<FirebaseFirestore.DocumentSnapshot<FirebaseFirestore.DocumentData>> {

        const doc = admin.firestore().collection('chats').doc(chatId);

        return transaction.get(doc);
    }

    public updateChatTransactional(transaction: FirebaseFirestore.Transaction, chat: Chat): FirebaseFirestore.Transaction {

        const doc: FirebaseFirestore.DocumentReference<FirebaseFirestore.DocumentData> = admin.firestore().collection('chats').doc(chat.getId());

        return transaction.set(doc,chat.toObject());
    }

    public deleteChatTransaction(transaction: FirebaseFirestore.Transaction, chatId: string): FirebaseFirestore.Transaction {

        const doc: FirebaseFirestore.DocumentReference<FirebaseFirestore.DocumentData> = admin.firestore().collection('chats').doc(chatId);

        return transaction.delete(doc);
    }


    public getChatsByUserThatSendLastMessage(userId: string) {

        const col: FirebaseFirestore.CollectionReference<FirebaseFirestore.DocumentData> = admin.firestore().collection('chats');

        return col
            .where('lastChatMessage.userShortForm.userId', '==', userId)
            .get();
    }

    async getPrivateChatsByUserId(userId: string) {
        const col: FirebaseFirestore.CollectionReference<FirebaseFirestore.DocumentData> = admin.firestore().collection('chats');

        return col
            .where("membersIds", "array-contains", userId)
            .where('chatType', '==', ChatTypes.PRIVATE_CONVERSATION)
            .get();
    }


}
