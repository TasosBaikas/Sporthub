/* eslint-disable */



import * as admin from "firebase-admin";
import {firestore} from "firebase-admin";
import DocumentReference = firestore.DocumentReference;
import {UserChatAction} from "../models/userprivacy/UserChatAction";
import Transaction = firestore.Transaction;
import {UserBlockedPlayers} from "../models/userprivacy/UserBlockedPlayers";
import {UserRatingList} from "../models/user/UserRatingList";

export class UserPrivacyFirebaseDao {


    async getPhoneNumberPermissionToThatChat(yourId: string, chatId: string) {
        const doc: DocumentReference = admin.firestore()
            .collection('userPrivacy').doc("userPhonePermissions")
            .collection('userById').doc(yourId)
            .collection("byChatId").doc(chatId);

        return doc.get();
    }

    async setPhoneNumberPermissionToThatChat(yourId: string, chatId: string, permitOrNot: boolean) {
        const doc: DocumentReference = admin.firestore()
            .collection('userPrivacy').doc("userPhonePermissions")
            .collection('userById').doc(yourId)
            .collection("byChatId").doc(chatId);

        return doc.set({permission: permitOrNot}, {merge: true});
    }

    async addOrRemovePhoneNumberPermissionToThatChat(yourId: string, chatId: string, permitOrNot: boolean) {
        const doc: DocumentReference = admin.firestore()
            .collection('userPrivacy').doc("userPhonePermissions")
            .collection('userById').doc(yourId)
            .collection("byChatId").doc(chatId);

        return doc.update("permission",permitOrNot);
    }

    async deletePhoneNumberPermissions(userId: string) {
        const col: FirebaseFirestore.CollectionReference<FirebaseFirestore.DocumentData> = admin.firestore()
            .collection('userPrivacy').doc("userPhonePermissions")
            .collection('userById').doc(userId)
            .collection("byChatId");

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

    getUserActivityToAChat(userId: string): Promise<FirebaseFirestore.DocumentSnapshot<FirebaseFirestore.DocumentData>> {
        const doc: DocumentReference = admin.firestore()
            .collection('userPrivacy').doc("userActivityToAChat")
            .collection('userById').doc(userId);

        return doc.get();
    }

    getUserActivityToAChatTransaction(transaction:Transaction, userId: string): Promise<FirebaseFirestore.DocumentSnapshot<FirebaseFirestore.DocumentData>> {
        const doc: DocumentReference = admin.firestore()
            .collection('userPrivacy').doc("userActivityToAChat")
            .collection('userById').doc(userId);

        return transaction.get(doc);
    }

    updateUserActivityToAChat(transaction:Transaction, userChatAction: UserChatAction): Transaction {
        const doc: FirebaseFirestore.DocumentReference<FirebaseFirestore.DocumentData> = admin.firestore()
            .collection('userPrivacy').doc("userActivityToAChat")
            .collection('userById').doc(userChatAction.getUserId())

        return transaction.set(doc, userChatAction.toObject());
    }

    deleteUserActivityToAChat(userId: string): Promise<FirebaseFirestore.WriteResult> {
        const doc: DocumentReference = admin.firestore()
            .collection('userPrivacy').doc("userActivityToAChat")
            .collection('userById').doc(userId);

        return doc.delete();
    }

    async getBlockedPlayersByUserId(yourId: string) {
        const doc: DocumentReference = admin.firestore()
            .collection('userPrivacy').doc("userBlockedPlayers")
            .collection('userById').doc(yourId);

        return doc.get();
    }

    async updateBlockedPlayersTransaction(transaction:Transaction, blockedPlayers: UserBlockedPlayers): Promise<FirebaseFirestore.Transaction> {
        const doc: DocumentReference = admin.firestore()
            .collection('userPrivacy').doc("userBlockedPlayers")
            .collection('userById').doc(blockedPlayers.getUserId());

        return transaction.set(doc, blockedPlayers.toObject());
    }

    async getUserRatingTransaction(transaction:Transaction, userToRate: string) {
        const doc: DocumentReference = admin.firestore()
            .collection('userPrivacy').doc("userRating")
            .collection('userById').doc(userToRate);

        return transaction.get(doc);
    }

    async rateUserTransaction(transaction: FirebaseFirestore.Transaction, userRatingsList: UserRatingList) {
        const doc: DocumentReference = admin.firestore()
            .collection('userPrivacy').doc("userRating")
            .collection('userById').doc(userRatingsList.getRatedUser());

        return transaction.set(doc, userRatingsList.toObject());
    }
}
