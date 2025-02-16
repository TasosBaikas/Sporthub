/* eslint-disable */


import {BadModelFieldException} from "../../exceptions/BadModelFieldException";

export class UserChatAction{

    private userId!: string;
    private chatsIds!: string[];

    constructor() {
    }

    public static makeInstance(userId:string, chatsIds:string[]):UserChatAction {

        const userChatAction:UserChatAction = new UserChatAction();

        userChatAction.setUserId(userId);
        userChatAction.setChatsIds(chatsIds);

        return userChatAction;
    }

    public static makeInstanceFromSnapshot(snapshotNeedsParsing: any):UserChatAction {

        const userChatAction:UserChatAction = new UserChatAction();

        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(snapshotNeedsParsing);
        }catch (Error){
            parsedSnapshot = snapshotNeedsParsing;
        }

        userChatAction.setUserId(parsedSnapshot.userId);
        userChatAction.setChatsIds(parsedSnapshot.chatsIds)

        return userChatAction;
    }


    toObject() {//this is the output that goes to firestore so becareful with the changes in this class

        return {
            userId: this.userId,
            chatsIds: this.chatsIds,
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

    public getChatsIds(): string[] {
        return this.chatsIds;
    }

    public setChatsIds(chatsIds: string[]) {

        for (const id of chatsIds) {

            if (id === undefined || typeof id !== 'string') {
                throw new BadModelFieldException("something is wrong with UserChatAction");
            }

        }

        this.chatsIds = chatsIds;
    }

}


