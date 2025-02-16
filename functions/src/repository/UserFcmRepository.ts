/* eslint-disable */


import {UserFcmFirebaseDao} from "../firebasedao/UserFcmFirebaseDao";
import {messaging} from "firebase-admin";
import {UserFcm} from "../models/user/UserFcm";

export class UserFcmRepository {

    private userFcmFirebaseDao:UserFcmFirebaseDao;

    constructor() {
        this.userFcmFirebaseDao = new UserFcmFirebaseDao();
    }

    public async getUserFcmTokens(userId:string): Promise<string[]> {

        const userDevicesSnapshot = await this.userFcmFirebaseDao.getUserFcmTokensPromise(userId);

        // Sending notifications to each device
        const fcmTokens:any = [];
        userDevicesSnapshot.docs.forEach(doc => {
            const fcmToken = doc.data().fcmToken;

            fcmTokens.push(fcmToken);
        });

        return fcmTokens;
    }

    public async saveUserFcmToken(userFcm:UserFcm): Promise<FirebaseFirestore.WriteResult> {
        return this.userFcmFirebaseDao.saveUserFcmToken(userFcm);
    }


    public sendEachForMulticast(fcmTokens: any, payload: { data: { pushNotificationType: string; [key: string]: any; } }): Promise<messaging.BatchResponse> {

        return this.userFcmFirebaseDao.sendEachForMulticast(fcmTokens,payload);
    }

    async deleteUserFcmToken(yourId: string, deviceUUID: string) {

        return this.userFcmFirebaseDao.deleteUserFcmToken(yourId,deviceUUID);
    }

}
