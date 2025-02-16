/* eslint-disable */

import * as admin from "firebase-admin";
import {ChatMessageBatch} from "../models/chat/ChatMessageBatch";
import {firestore} from "firebase-admin";
import CollectionReference = firestore.CollectionReference;
import QuerySnapshot = firestore.QuerySnapshot;

export class ChatMessageFirebaseDao {

    public getLastChatMessageBatch(chatId:string): Promise<FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData>> {
        const col = admin.firestore().collection("chatMessageBatch").doc(chatId).collection("chatMessageBatchesSpecificChat");

        return col
            .orderBy('createdAtUTC', 'desc')
            .limit(1)
            .get();
    }

    saveChatMessageBatchTransactional(transaction: FirebaseFirestore.Transaction, chatMessageBatch: ChatMessageBatch) {
        const doc = admin.firestore().collection("chatMessageBatch").doc(chatMessageBatch.getChatId())
            .collection("chatMessageBatchesSpecificChat").doc(chatMessageBatch.getId());


        return transaction.set(doc,chatMessageBatch.toObject());
    }

    getChatMessageBatchByIdTransactional(transaction: FirebaseFirestore.Transaction, chatId: string, batchId: string) {
        const doc = admin.firestore().collection("chatMessageBatch").doc(chatId)
            .collection("chatMessageBatchesSpecificChat").doc(batchId);

        return transaction.get(doc);
    }

    updateChatMessageBatchTransactional(transaction: FirebaseFirestore.Transaction, chatMessageBatch: ChatMessageBatch): FirebaseFirestore.Transaction {
        return this.saveChatMessageBatchTransactional(transaction,chatMessageBatch);
    }

    async getChatMessageBatchByPlayerIdSeenBy(transaction: FirebaseFirestore.Transaction, chatId: string, yourId: string): Promise<QuerySnapshot> {
        const col:CollectionReference = admin.firestore().collection("chatMessageBatch").doc(chatId).collection("chatMessageBatchesSpecificChat");

        return transaction.get(col.where("seenByUsersId", "array-contains", yourId));
    }

    async getChatMessageBatchByUserAction(transaction: FirebaseFirestore.Transaction, chatId: string, id: string): Promise<QuerySnapshot> {
        const col:CollectionReference = admin.firestore().collection("chatMessageBatch").doc(chatId).collection("chatMessageBatchesSpecificChat");

        return transaction.get(col.where("usersWithActivity", "array-contains", id));
    }

    async deleteAllChatMessagesBatch(chatId: string): Promise<FirebaseFirestore.Transaction> {
        const col = admin.firestore().collection("chatMessageBatch").doc(chatId).collection("chatMessageBatchesSpecificChat");

        const batchSizeLimit:number = 500;
        const query: FirebaseFirestore.Query<FirebaseFirestore.DocumentData> = col.orderBy('__name__').limit(batchSizeLimit);

        return new Promise((resolve, reject) => {
            this.deleteQueryBatch(query, resolve).catch(reject);
        });
    }

    private async deleteQueryBatch(query: FirebaseFirestore.Query<FirebaseFirestore.DocumentData>, resolve: any) {
        const snapshot = await query.get();

        const batchSize = snapshot.size;
        if (batchSize === 0) {
            // When there are no documents left, we are done
            resolve();
            return;
        }

        // Delete documents in a batch
        const batch = admin.firestore().batch();
        snapshot.docs.forEach((doc) => {
            batch.delete(doc.ref);
        });
        await batch.commit();

        // Recurse on the next process tick, to avoid
        // exploding the stack.
        process.nextTick(() => {
            this.deleteQueryBatch(query, resolve);
        });
    }

}
