/* eslint-disable */


import {ChatMessage} from "./ChatMessage";
import {BadModelFieldException} from "../../exceptions/BadModelFieldException";

export class ChatMessageBatch {

    private id!:string;
    private chatId!:string;
    private chatMessages!: ChatMessage[];
    private seenByUsersId!: string[];
    private usersWithActivity!: string[];
    private createdAtUTC!:number;
    private lastModifiedTimeInUTC!:number;

    public static readonly MAX_MESSAGES:number = 20;


    constructor() {
        this.setLastModifiedTimeInUTCEmpty();
    }


    static makeChatMessageBatch(chatMessageBatchId: string, chatId: string, chatMessages: ChatMessage[], seenByUsersId: string[], usersWithActivity: string[], createdAtUTC: number) {

        const chatMessageBatch:ChatMessageBatch = new ChatMessageBatch();

        chatMessageBatch.setId(chatMessageBatchId);
        chatMessageBatch.setChatId(chatId);
        chatMessageBatch.setChatMessages(chatMessages);
        chatMessageBatch.setSeenByUsersId(seenByUsersId);
        chatMessageBatch.setUsersWithActivity(usersWithActivity);
        chatMessageBatch.setCreatedAtUTC(createdAtUTC);


        return chatMessageBatch;
    }

    static makeInstanceFromSnapshot(snapshotNeedsParsing: any): ChatMessageBatch {

        const chatMessageBatch:ChatMessageBatch = new ChatMessageBatch();

        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(snapshotNeedsParsing);
        }catch (Error){
            parsedSnapshot = snapshotNeedsParsing;
        }

        chatMessageBatch.setId(parsedSnapshot.id);
        chatMessageBatch.setChatId(parsedSnapshot.chatId);


        const chatMessages: ChatMessage[] = [];
        for (const chatMessage of parsedSnapshot.chatMessages) {
            chatMessages.push(ChatMessage.makeInstanceFromSnapshot(chatMessage));
        }
        chatMessageBatch.setChatMessages(chatMessages);


        chatMessageBatch.setSeenByUsersId(parsedSnapshot.seenByUsersId);
        chatMessageBatch.setUsersWithActivity(parsedSnapshot.usersWithActivity);
        chatMessageBatch.setCreatedAtUTC(parsedSnapshot.createdAtUTC);
        chatMessageBatch.setLastModifiedTimeInUTC(parsedSnapshot.lastModifiedTimeInUTC);

        return chatMessageBatch;
    }

    toObject() {//this is the output that goes to firestore so becareful with the changes in this class


        const chatMessagesForFirebase: {}[] = [];
        this.chatMessages.forEach((value: ChatMessage, key) => {
            chatMessagesForFirebase.push(value.toObject());
        });


        return {
            id: this.id,
            chatId: this.chatId,
            chatMessages: chatMessagesForFirebase,
            seenByUsersId: this.seenByUsersId,
            usersWithActivity: this.usersWithActivity,
            createdAtUTC: this.createdAtUTC,
            lastModifiedTimeInUTC: this.lastModifiedTimeInUTC,
            MAX_MESSAGES: ChatMessageBatch.MAX_MESSAGES,
        };
    }


    public getId(): string {
        return this.id;
    }

    public setId(id: string) {

        if (id === undefined || typeof id !== 'string') {
            throw new BadModelFieldException("id must be a string");
        }

        let idLength:number = 60;
        if (id.length > idLength){
            throw new BadModelFieldException("id must be less than " + idLength + " letters");
        }


        this.id = id;
    }

    public getChatId(): string {
        return this.chatId;
    }

    public setChatId(chatId: string) {

        if (chatId === undefined || typeof chatId !== 'string') {
            throw new BadModelFieldException("chat message batch id must be a string");
        }

        let maxLength:number = 60;
        if (chatId.length > maxLength){
            throw new BadModelFieldException("chat message batch id must be less than " + maxLength + " letters");
        }

        this.chatId = chatId;
    }

    public getChatMessages(): ChatMessage[] {
        return this.chatMessages;
    }

    public setChatMessages(chatMessagesList: ChatMessage[]) {

        for (const chatMessage of chatMessagesList) {

            if (chatMessage === undefined || !(chatMessage instanceof ChatMessage)) {
                throw new BadModelFieldException("something is wrong with chatMessage");
            }

        }

        this.chatMessages = chatMessagesList;
    }

    public getSeenByUsersId(): string[] {
        return this.seenByUsersId;
    }

    public setSeenByUsersId(seenByList: string[]) {

        for (const userId of seenByList) {
            if (userId === undefined || typeof userId !== 'string') {
                throw new BadModelFieldException("seenBy user id must be a valid string");
            }

            let maxLength:number = 60;
            if (userId.length > maxLength){
                throw new BadModelFieldException("seenBy user id must be less than " + maxLength + " letters");
            }
        }

        let deepCopy = JSON.parse(JSON.stringify(seenByList));


        this.seenByUsersId = deepCopy;
    }

    public getUsersWithActivity(): string[] {
        return this.usersWithActivity;
    }

    public setUsersWithActivity(usersWithActivity: string[]) {

        for (const userId of usersWithActivity) {
            if (userId === undefined || typeof userId !== 'string') {
                throw new BadModelFieldException("usersWithActivity user id must be a valid string");
            }

            let maxLength:number = 60;
            if (userId.length > maxLength){
                throw new BadModelFieldException("usersWithActivity user id must be less than " + maxLength + " letters");
            }
        }

        let deepCopy = JSON.parse(JSON.stringify(usersWithActivity));

        this.usersWithActivity = deepCopy;
    }

    public getCreatedAtUTC(): number {
        return this.createdAtUTC;
    }

    public setCreatedAtUTC(createdAtUTC: number) {

        if (createdAtUTC === undefined || typeof createdAtUTC !== 'number' || isNaN(createdAtUTC)) {
            throw new BadModelFieldException("created at must be a number");
        }


        this.createdAtUTC = createdAtUTC;
    }

    public getLastModifiedTimeInUTC(): number {
        return this.lastModifiedTimeInUTC;
    }

    public setLastModifiedTimeInUTC(lastModifiedTimeInUTC: number) {

        const currentDate = new Date();

        this.lastModifiedTimeInUTC = currentDate.getTime();
    }

    public setLastModifiedTimeInUTCEmpty() {

        const currentDate = new Date();

        this.lastModifiedTimeInUTC = currentDate.getTime();
    }


}
