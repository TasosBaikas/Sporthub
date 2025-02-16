/* eslint-disable */

import {NotificationOptions} from "./NotificationOptions";
import {BadModelFieldException} from "../../../exceptions/BadModelFieldException";

export class UserNotification {

    private userId!:string;
    private generalNotificationOptions!:NotificationOptions;
    private notificationsBasedOnChats!:Map<string, NotificationOptions>;

    constructor() {
    }

    public static makeInstanceFromSnapshot(snapshotNeedsParsing: any): UserNotification {

        const userNotification:UserNotification = new UserNotification();

        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(snapshotNeedsParsing);
        }catch (Error){
            parsedSnapshot = snapshotNeedsParsing;
        }


        userNotification.setUserId(parsedSnapshot.userId);

        const generalNotificationOptions: NotificationOptions = NotificationOptions.makeInstanceFromSnapshot(parsedSnapshot.generalNotificationOptions);
        userNotification.setGeneralNotificationOptions(generalNotificationOptions);

        const notificationsBasedOnChatsData = parsedSnapshot.notificationsBasedOnChats;

        // Assuming notificationsBasedOnChats is an object with key-value pairs
        let notificationsBasedOnChats = new Map<string, NotificationOptions>();
        for (let key in notificationsBasedOnChatsData) {
            if (notificationsBasedOnChatsData.hasOwnProperty(key)) {
                notificationsBasedOnChats.set(key, NotificationOptions.makeInstanceFromSnapshot(notificationsBasedOnChatsData[key]));
            }
        }
        userNotification.setNotificationsBasedOnChats(notificationsBasedOnChats);


        return userNotification;
    }


    toObject() {//this is the output that goes to firestore so becareful with the changes in this class

        const object: Record<string, {}> = {};
        this.notificationsBasedOnChats.forEach((value, key) => {
            object[key] = value.toObject();
        });

        return {
            userId: this.userId,
            generalNotificationOptions: this.generalNotificationOptions.toObject(),
            notificationsBasedOnChats: object,
            // add other fields as needed
        };
    }


    public getUserId(): string {
        return this.userId;
    }

    public setUserId(userId: string) {

        if (userId === undefined || typeof userId !== 'string') {
            throw new BadModelFieldException("user id must be a string");
        }

        userId = userId.trim();
        if (userId.length == 0)
            throw new BadModelFieldException("id length is 0");


        let maxLength:number = 60;
        if (userId.length > maxLength){
            throw new BadModelFieldException("user id must be less than " + maxLength + " letters");
        }

        this.userId = userId;
    }

    public getGeneralNotificationOptions(): NotificationOptions {
        return this.generalNotificationOptions;
    }

    public setGeneralNotificationOptions(generalNotificationOptions: NotificationOptions) {

        if (generalNotificationOptions === undefined || !(generalNotificationOptions instanceof NotificationOptions)) {
            throw new BadModelFieldException("something is wrong with general notification options");
        }

        this.generalNotificationOptions = generalNotificationOptions;
    }

    public getNotificationsBasedOnChats(): Map<string, NotificationOptions> {
        return this.notificationsBasedOnChats;
    }

    public setNotificationsBasedOnChats(notificationsBasedOnChats: Map<string, NotificationOptions>) {

        if (notificationsBasedOnChats === undefined || !(notificationsBasedOnChats instanceof Map)) {
            throw new BadModelFieldException("notifications based on chats must be a valid Map");
        }

        for (const key of notificationsBasedOnChats.keys()) {
            if (key === undefined || typeof key !== 'string') {
                throw new BadModelFieldException("something is wrong with the model (notificationsBasedOnChats)");
            }

            const notificationOptions: NotificationOptions | undefined = notificationsBasedOnChats.get(key);
            if (notificationOptions === undefined){
                throw new BadModelFieldException("something is wrong with the model (notificationsBasedOnChats)");
            }

            if (notificationOptions === undefined || !(notificationOptions instanceof NotificationOptions)) {
                throw new BadModelFieldException("something is wrong with notificationOptions");
            }

        }

        this.notificationsBasedOnChats = notificationsBasedOnChats;
    }
}
