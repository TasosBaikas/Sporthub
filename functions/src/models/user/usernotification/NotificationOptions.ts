/* eslint-disable */

import {BadModelFieldException} from "../../../exceptions/BadModelFieldException";

export class NotificationOptions {
    private notificationsBeforeMatch!:boolean;
    private snoozeChatMessages!:number;


    constructor() {
    }

    static makeInstanceFromSnapshot(snapshotNeedsParsing: any): NotificationOptions {

        const notificationOptions:NotificationOptions = new NotificationOptions();

        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(snapshotNeedsParsing);
        }catch (Error){
            parsedSnapshot = snapshotNeedsParsing;
        }


        notificationOptions.setNotificationsBeforeMatch(parsedSnapshot.notificationsBeforeMatch);
        notificationOptions.setSnoozeChatMessages(parsedSnapshot.snoozeChatMessages);

        return notificationOptions;
    }


    toObject() {//this is the output that goes to firestore so becareful with the changes in this class

        return {
            notificationsBeforeMatch: this.notificationsBeforeMatch,
            snoozeChatMessages: this.snoozeChatMessages,
        };
    }


    public getNotificationsBeforeMatch(): boolean {
        return this.notificationsBeforeMatch;
    }

    public setNotificationsBeforeMatch(notificationsBeforeMatch: boolean) {

        if (notificationsBeforeMatch === undefined || typeof notificationsBeforeMatch !== 'boolean') {
            throw new BadModelFieldException("notifications before match must be a valid boolean");
        }

        this.notificationsBeforeMatch = notificationsBeforeMatch;
    }

    public getSnoozeChatMessages(): number {
        return this.snoozeChatMessages;
    }

    public setSnoozeChatMessages(snoozeChatMessages: number) {

        if (snoozeChatMessages === undefined || typeof snoozeChatMessages !== 'number' || isNaN(snoozeChatMessages)) {
            throw new BadModelFieldException("snooze chat messages must be a number");
        }

        this.snoozeChatMessages = snoozeChatMessages;
    }


}
