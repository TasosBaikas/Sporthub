/* eslint-disable */


import {UserNotificationsFirebaseDao} from "../firebasedao/UserNotificationsFirebaseDao";
import {UserNotification} from "../models/user/usernotification/UserNotification";

export class UserNotificationsRepository {

    private userNotificationsFirebaseDao:UserNotificationsFirebaseDao;

    constructor() {
        this.userNotificationsFirebaseDao = new UserNotificationsFirebaseDao();
    }

    public async getUserNotificationsByIdPromise(userId:string): Promise<UserNotification | undefined> {
        const snapshots: FirebaseFirestore.DocumentSnapshot<FirebaseFirestore.DocumentData> = await this.userNotificationsFirebaseDao.getUserNotificationsByIdPromise(userId);

        const data = snapshots.data();
        if (data === undefined)
            return undefined;

        return UserNotification.makeInstanceFromSnapshot(data);
    }

    public saveUserNotificationsPromise(userNotification: UserNotification): Promise<FirebaseFirestore.WriteResult> {
        return this.userNotificationsFirebaseDao.saveUserNotificationsPromise(userNotification);
    }

    public deleteUserNotifications(userId:string): Promise<FirebaseFirestore.WriteResult> {
        return this.userNotificationsFirebaseDao.deleteUserNotifications(userId);
    }

}
