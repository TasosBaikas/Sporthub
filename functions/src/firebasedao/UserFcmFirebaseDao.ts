/* eslint-disable */


import * as admin from "firebase-admin";
import {messaging} from "firebase-admin";
import BatchResponse = messaging.BatchResponse;
import {UserFcm} from "../models/user/UserFcm";

export class UserFcmFirebaseDao {

    public getUserFcmTokensPromise(userId:string): Promise<FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData>> {
        return admin.firestore().collection(`userFcm/${userId}/userDevices`).get();
    }

    public sendEachForMulticast(fcmTokens: any, payload: { data: { pushNotificationType: string; [p: string]: any } }): Promise<BatchResponse> {
        return admin.messaging().sendEachForMulticast({ tokens: fcmTokens, ...payload});
    }

    public saveUserFcmToken(userFcm: UserFcm): Promise<FirebaseFirestore.WriteResult> {

        const doc = admin.firestore().collection("userFcm").doc(userFcm.getUserId()).collection("userDevices").doc(userFcm.getDeviceUUID());

        return doc.set(userFcm.toObject());
    }

    public deleteUserFcmToken(yourId: string, deviceUUID: string): Promise<FirebaseFirestore.WriteResult> {

        const doc = admin.firestore().collection("userFcm").doc(yourId)
            .collection("userDevices").doc(deviceUUID);

        return doc.delete();
    }

}
