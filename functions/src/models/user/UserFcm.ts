/* eslint-disable */


import {BadModelFieldException} from "../../exceptions/BadModelFieldException";

export class UserFcm {

    private userId!:string;
    private fcmToken!:string;
    private lastTimeFetched!:number;
    private deviceUUID!:string;


    constructor() {
        this.setLastTimeFetchedEmpty();
    }

    static makeInstanceFromSnapshot(snapshotNeedsParsing: any): UserFcm {

        const userFcm:UserFcm = new UserFcm();

        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(snapshotNeedsParsing);
        }catch (Error){
            parsedSnapshot = snapshotNeedsParsing;
        }

        userFcm.setUserId(parsedSnapshot.userId);
        userFcm.setFcmToken(parsedSnapshot.fcmToken);
        userFcm.setLastTimeFetched(parsedSnapshot.lastTimeFetched);
        userFcm.setDeviceUUID(parsedSnapshot.deviceUUID);

        return userFcm;
    }

    toObject() {//this is the output that goes to firestore so becareful with the changes in this class

        return {

            userId: this.userId,
            fcmToken: this.fcmToken,
            lastTimeFetched: this.lastTimeFetched,
            deviceUUID: this.deviceUUID,
        };
    }


    public getUserId(): string {
        return this.userId;
    }

    public setUserId(userId: string) {
        if (userId === undefined || typeof userId !== 'string') {
            throw new BadModelFieldException("id must be a string");
        }

        userId = userId.trim();
        if (userId.length == 0)
            throw new BadModelFieldException("userId length is 0");

        let idLength:number = 60;
        if (userId.length > idLength){
            throw new BadModelFieldException("id must be less than " + idLength + " letters");
        }

        this.userId = userId;
    }

    public getFcmToken(): string {
        return this.fcmToken;
    }

    public setFcmToken(fcmToken: string) {
        if (fcmToken === undefined || typeof fcmToken !== 'string') {
            throw new BadModelFieldException("fcm token must be a string");
        }

        let idLength:number = 200;
        if (fcmToken.length > idLength){
            throw new BadModelFieldException("fcm token must be less than " + idLength + " letters");
        }

        this.fcmToken = fcmToken;
    }

    public getLastTimeFetched(): number {
        return this.lastTimeFetched;
    }

    public setLastTimeFetched(lastTimeFetched: number) {
        // if (lastTimeFetched === undefined || typeof lastTimeFetched !== 'number' || isNaN(lastTimeFetched)) {
        //     throw new BadModelFieldException("last time fetched must be a valid number");
        // }

        this.lastTimeFetched = Date.now();
    }

    private setLastTimeFetchedEmpty() {
        this.lastTimeFetched = Date.now();
    }

    public getDeviceUUID(): string {
        return this.deviceUUID;
    }

    public setDeviceUUID(deviceUUID: string) {

        if (deviceUUID === undefined || typeof deviceUUID !== 'string') {
            throw new BadModelFieldException("deviceUUID must be a string");
        }

        let maxLength:number = 120;
        if (deviceUUID.length > maxLength){
            throw new BadModelFieldException("deviceUUID must be less than " + maxLength + " letters");
        }

        this.deviceUUID = deviceUUID;
    }


}
