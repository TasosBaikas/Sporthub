/* eslint-disable */

import * as admin from "firebase-admin";
import {UserNotification} from "../models/user/usernotification/UserNotification";

export class UserNotificationsFirebaseDao {


    public getUserNotificationsByIdTransaction(transaction: FirebaseFirestore.Transaction,userId:string): Promise<FirebaseFirestore.DocumentSnapshot<FirebaseFirestore.DocumentData>> {
        const docRef: FirebaseFirestore.DocumentReference<FirebaseFirestore.DocumentData> =  admin.firestore().collection('userNotifications').doc(userId);

        return transaction.get(docRef);
    }

    public getUserNotificationsByIdPromise(userId:string): Promise<FirebaseFirestore.DocumentSnapshot<FirebaseFirestore.DocumentData>> {
        return admin.firestore().collection('userNotifications').doc(userId).get();
    }

    public saveUserNotificationsPromise(userNotifications: UserNotification ): Promise<FirebaseFirestore.WriteResult> {

        return admin.firestore().collection("userNotifications").doc(userNotifications.getUserId()).set(userNotifications.toObject());
    }

    public updateUserNotificationsTransaction(transaction: FirebaseFirestore.Transaction, userNotifications: UserNotification) {

        const docRef: FirebaseFirestore.DocumentReference<FirebaseFirestore.DocumentData> =  admin.firestore().collection("userNotifications").doc(userNotifications.getUserId());

        return transaction.set(docRef,userNotifications.toObject());
    }

    public deleteUserNotifications(userId:string): Promise<FirebaseFirestore.WriteResult> {

        const docRef: FirebaseFirestore.DocumentReference<FirebaseFirestore.DocumentData> =  admin.firestore().collection("userNotifications").doc(userId);

        return docRef.delete();
    }

}
