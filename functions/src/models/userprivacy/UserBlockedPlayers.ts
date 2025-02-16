/* eslint-disable */


import {BadModelFieldException} from "../../exceptions/BadModelFieldException";

export class UserBlockedPlayers {

    private userId!: string;
    private blockedPlayers!: string[];

    constructor() {
    }

    public static makeInstance(userId:string, blockedPlayers:string[]):UserBlockedPlayers {

        const userBlockedPlayers:UserBlockedPlayers = new UserBlockedPlayers();

        userBlockedPlayers.setUserId(userId);
        userBlockedPlayers.setBlockedPlayers(blockedPlayers);

        return userBlockedPlayers;
    }

    public static makeInstanceFromSnapshot(snapshotNeedsParsing: any):UserBlockedPlayers {

        const userChatAction:UserBlockedPlayers = new UserBlockedPlayers();

        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(snapshotNeedsParsing);
        }catch (Error){
            parsedSnapshot = snapshotNeedsParsing;
        }

        userChatAction.setUserId(parsedSnapshot.userId);
        userChatAction.setBlockedPlayers(parsedSnapshot.blockedPlayers)

        return userChatAction;
    }


    toObject() {//this is the output that goes to firestore so becareful with the changes in this class

        return {
            userId: this.userId,
            blockedPlayers: this.blockedPlayers,
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
            throw new BadModelFieldException("id length is 0");


        let maxLength:number = 60;
        if (userId.length > maxLength){
            throw new BadModelFieldException("id must be less than " + maxLength + " letters");
        }


        this.userId = userId;
    }

    public getBlockedPlayers(): string[] {
        return this.blockedPlayers;
    }

    public setBlockedPlayers(blockedPlayers: string[]) {

        for (const id of blockedPlayers) {

            if (id === undefined || typeof id !== 'string') {
                throw new BadModelFieldException("something is wrong with blockedPlayers");
            }

        }

        this.blockedPlayers = blockedPlayers;
    }

}


