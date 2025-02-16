/* eslint-disable */

import {BadModelFieldException} from "../../exceptions/BadModelFieldException";
import {UserShortForm} from "../user/UserShortForm";
import {MessageStatus} from "../constants/MessageStatus";
import {ChatMessageType} from "../constants/ChatMessageType";
import {ObjectWithId} from "../../interfaces/ObjectWithId";
import {v4 as uuidv4} from "uuid";

export class ChatMessage implements ObjectWithId{

    private id!: string;
    private chatMessageBatchId!: string;
    private chatId!: string;
    private userShortForm!: UserShortForm;
    private previousMessageUserId!: string;
    private message!: string;
    private messageStatus!: string;
    private messageType!: string;
    private seenByUsersId!: string[];
    private emojisMap!: Map<string, string[]>;
    private createdAtUTC!: number;
    private previousMessageCreatedAtUTC!: number;
    private lastModifiedTimeInUTC!: number;


    constructor() {

        this.setLastModifiedTimeInUTCEmpty();
    }

    static chatFirstMessageInstance(chatMessageBatchId:string , chatId:string , userShortForm:UserShortForm): ChatMessage {

        const chatMessage:ChatMessage = new ChatMessage();

        chatMessage.setId(uuidv4());
        chatMessage.setChatMessageBatchId(chatMessageBatchId);
        chatMessage.setChatId(chatId);
        chatMessage.setPreviousMessageUserId("");
        chatMessage.setMessage("Η ομάδα μόλις δημιουργήθηκε!");
        chatMessage.setUserShortForm(userShortForm);
        chatMessage.setMessageStatus(MessageStatus.SENT);
        chatMessage.setMessageType(ChatMessageType.ONLY_FOR_FIRST_MESSAGE);
        chatMessage.setCreatedAtUTC(Date.now());
        chatMessage.setPreviousMessageCreatedAtUTC(0);
        chatMessage.setSeenByUsersId([]);
        chatMessage.setEmojisMap(new Map<string, string[]>());

        return chatMessage;
    }

    static makeInstanceFromSnapshot(snapshotNeedsParsing: any): ChatMessage {

        const chatMessage:ChatMessage = new ChatMessage();

        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(snapshotNeedsParsing);
        }catch (Error){
            parsedSnapshot = snapshotNeedsParsing;
        }

        chatMessage.setId(parsedSnapshot.id);
        chatMessage.setChatMessageBatchId(parsedSnapshot.chatMessageBatchId);
        chatMessage.setChatId(parsedSnapshot.chatId);
        chatMessage.setPreviousMessageUserId(parsedSnapshot.previousMessageUserId);
        chatMessage.setPreviousMessageUserId(parsedSnapshot.previousMessageUserId);
        chatMessage.setMessage(parsedSnapshot.message);
        chatMessage.setMessageStatus(parsedSnapshot.messageStatus);
        chatMessage.setMessageType(parsedSnapshot.messageType);
        chatMessage.setSeenByUsersId(parsedSnapshot.seenByUsersId);

        const userShortForm: UserShortForm = UserShortForm.makeInstanceFromSnapshot(parsedSnapshot.userShortForm);
        chatMessage.setUserShortForm(userShortForm);

        let parsedEmojiMap;
        try{
            parsedEmojiMap = JSON.parse(parsedSnapshot.emojisMap);
        }catch (Error){
            parsedEmojiMap = parsedSnapshot.emojisMap;
        }

        const emojisMap: Map<string, string[]> = new Map<string, string[]>();
        for (let key in parsedEmojiMap) {
            if (parsedEmojiMap.hasOwnProperty(key)) {
                emojisMap.set(key, parsedEmojiMap[key]);
            }
        }

        chatMessage.setEmojisMap(emojisMap);
        chatMessage.setCreatedAtUTC(parsedSnapshot.createdAtUTC);
        chatMessage.setPreviousMessageCreatedAtUTC(parsedSnapshot.previousMessageCreatedAtUTC);
        chatMessage.setLastModifiedTimeInUTC(parsedSnapshot.lastModifiedTimeInUTC);


        return chatMessage;
    }

    static makeInstanceEnablePhoneNumber(chatMessageBatchId:string , chatId:string , userShortForm:UserShortForm) {

        const chatMessage:ChatMessage = new ChatMessage();


        chatMessage.setId(uuidv4());
        chatMessage.setChatMessageBatchId(chatMessageBatchId);
        chatMessage.setChatId(chatId);
        chatMessage.setPreviousMessageUserId("");

        chatMessage.setMessage("Ο " + "X" + " δήλωσε το κινητό του για άμεση επικοινωνία");
        chatMessage.setUserShortForm(userShortForm);
        chatMessage.setMessageStatus(MessageStatus.SENT);
        chatMessage.setMessageType(ChatMessageType.PHONE_ENABLED);
        chatMessage.setCreatedAtUTC(Date.now());
        chatMessage.setPreviousMessageCreatedAtUTC(0);
        chatMessage.setSeenByUsersId([]);
        chatMessage.setEmojisMap(new Map<string, string[]>());

        return chatMessage;
    }


    static makeInstanceDisablePhoneNumber(chatMessageBatchId: string, chatId: string, userShortForm: UserShortForm) {
        const chatMessage:ChatMessage = new ChatMessage();


        chatMessage.setId(uuidv4());
        chatMessage.setChatMessageBatchId(chatMessageBatchId);
        chatMessage.setChatId(chatId);
        chatMessage.setPreviousMessageUserId("");

        chatMessage.setMessage("Ο " + "X" + " απέσυρε το κινητό του");
        chatMessage.setUserShortForm(userShortForm);
        chatMessage.setMessageStatus(MessageStatus.SENT);
        chatMessage.setMessageType(ChatMessageType.PHONE_DISABLED);
        chatMessage.setCreatedAtUTC(Date.now());
        chatMessage.setPreviousMessageCreatedAtUTC(0);
        chatMessage.setSeenByUsersId([]);
        chatMessage.setEmojisMap(new Map<string, string[]>());

        return chatMessage;
    }


    static ChatFirstMessageInstance(id: string, chatMessageBatchId: string, chatId: string, userShortForm: UserShortForm, createdAtUTC: number): ChatMessage {

        const chatFirstMessageInstance:ChatMessage = new ChatMessage();

        chatFirstMessageInstance.setId(id);
        chatFirstMessageInstance.setChatMessageBatchId(chatMessageBatchId);
        chatFirstMessageInstance.setChatId(chatId);
        chatFirstMessageInstance.setPreviousMessageUserId("");
        chatFirstMessageInstance.setMessage("Η ομάδα μόλις δημιουργήθηκε!");
        chatFirstMessageInstance.setUserShortForm(userShortForm);
        chatFirstMessageInstance.setMessageStatus(MessageStatus.SENT);
        chatFirstMessageInstance.setMessageType(ChatMessageType.ONLY_FOR_FIRST_MESSAGE);
        chatFirstMessageInstance.setCreatedAtUTC(createdAtUTC);
        chatFirstMessageInstance.setPreviousMessageCreatedAtUTC(0);
        chatFirstMessageInstance.setSeenByUsersId([]);
        chatFirstMessageInstance.setEmojisMap(new Map<string, string[]>());

        return chatFirstMessageInstance;
    }

static UserLeftChatMessageInstance(id: string, chatMessageBatchId: string, chatId: string, userShortForm: UserShortForm, previousMessageUserId: string,
        messageStatus: string, createdAtUTC: number, previousMessageCreatedAtUTC: number): ChatMessage {

        const chatMessageUserLeft:ChatMessage = new ChatMessage();

        chatMessageUserLeft.setId(id);
        chatMessageUserLeft.setChatMessageBatchId(chatMessageBatchId);
        chatMessageUserLeft.setChatId(chatId);
        chatMessageUserLeft.setPreviousMessageUserId(previousMessageUserId);
        chatMessageUserLeft.setMessage("Ο χρήστης έφυγε");
        chatMessageUserLeft.setUserShortForm(userShortForm);
        chatMessageUserLeft.setMessageStatus(messageStatus);
        chatMessageUserLeft.setMessageType(ChatMessageType.USER_LEFT);
        chatMessageUserLeft.setCreatedAtUTC(createdAtUTC);
        chatMessageUserLeft.setPreviousMessageCreatedAtUTC(previousMessageCreatedAtUTC);
        chatMessageUserLeft.setSeenByUsersId([]);
        chatMessageUserLeft.setEmojisMap(new Map<string, string[]>());

        return chatMessageUserLeft;
    }


    toObject() {//this is the output that goes to firestore so becareful with the changes in this class


        const emojisMap: Record<string, {}> = {};
        this.emojisMap.forEach((value, key) => {
            emojisMap[key] = value;
        });


        return {
            id: this.id,
            chatMessageBatchId: this.chatMessageBatchId,
            chatId: this.chatId,
            userShortForm: this.userShortForm.toObject(),
            previousMessageUserId: this.previousMessageUserId,
            message: this.message,
            messageStatus: this.messageStatus,
            messageType: this.messageType,
            seenByUsersId: this.seenByUsersId,
            emojisMap: emojisMap,
            createdAtUTC: this.createdAtUTC,
            previousMessageCreatedAtUTC: this.previousMessageCreatedAtUTC,
            lastModifiedTimeInUTC: this.lastModifiedTimeInUTC,
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

    public getChatMessageBatchId(): string {
        return this.chatMessageBatchId;
    }

    public setChatMessageBatchId(chatMessageBatchId: string) {

        if (chatMessageBatchId === undefined || typeof chatMessageBatchId !== 'string') {
            throw new BadModelFieldException("chat message batch id must be a string");
        }

        let maxLength:number = 60;
        if (chatMessageBatchId.length > maxLength){
            throw new BadModelFieldException("chat message batch id must be less than " + maxLength + " letters");
        }

        this.chatMessageBatchId = chatMessageBatchId;
    }

    public getChatId(): string {
        return this.chatId;
    }

    public setChatId(chatId: string) {

        if (chatId === undefined || typeof chatId !== 'string') {
            throw new BadModelFieldException("chat id must be a string");
        }

        let maxLength:number = 60;
        if (chatId.length > maxLength){
            throw new BadModelFieldException("chat id must be less than " + maxLength + " letters");
        }

        this.chatId = chatId;
    }


    public getUserShortForm(): UserShortForm {
        return this.userShortForm;
    }

    public setUserShortForm(userShortForm: UserShortForm) {

        if (userShortForm === undefined || !(userShortForm instanceof UserShortForm)) {
            throw new BadModelFieldException("something is wrong with terrainAddress");
        }

        this.userShortForm = userShortForm;
    }

    public getPreviousMessageUserId(): string {
        return this.previousMessageUserId;
    }

    public setPreviousMessageUserId(previousMessageUserId: string) {

        if (previousMessageUserId === undefined || typeof previousMessageUserId !== 'string') {
            throw new BadModelFieldException("id must be a string");
        }

        let maxLength:number = 60;
        if (previousMessageUserId.length > maxLength){
            throw new BadModelFieldException("id must be less than " + maxLength + " letters");
        }

        this.previousMessageUserId = previousMessageUserId;
    }

    public getMessage(): string {
        return this.message;
    }

    public setMessage(message: string) {

        if (message === undefined || typeof message !== 'string') {
            throw new BadModelFieldException("message must be a string");
        }

        let maxLength:number = 1000;
        if (message.length > maxLength){
            throw new BadModelFieldException("message must be less than " + maxLength + " letters");
        }

        this.message = message;
    }

    public getMessageStatus(): string {
        return this.messageStatus;
    }

    public setMessageStatus(messageStatus: string) {

        if (messageStatus === undefined || typeof messageStatus !== 'string') {
            throw new BadModelFieldException("message status must be a string");
        }

        let maxLength:number = 50;
        if (messageStatus.length > maxLength){
            throw new BadModelFieldException("message status must be less than " + maxLength + " letters");
        }

        const found: boolean = MessageStatus.STATUS_OPTIONS_LIST.includes(messageStatus);
        if (!found)
            throw new BadModelFieldException("message status bad value");


        this.messageStatus = messageStatus;
    }

    public getMessageType(): string {
        return this.messageType;
    }

    public setMessageType(messageType: string) {

        if (messageType === undefined || typeof messageType !== 'string') {
            throw new BadModelFieldException("message type must be a string");
        }

        let maxLength:number = 50;
        if (messageType.length > maxLength){
            throw new BadModelFieldException("message type must be less than " + maxLength + " letters");
        }

        const found: boolean = ChatMessageType.CHAT_MESSAGE_TYPES_LIST.includes(messageType);
        if (!found)
            throw new BadModelFieldException("message type bad value");


        this.messageType = messageType;
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

    public getEmojisMap(): Map<string, string[]> {
        return this.emojisMap;
    }

    public setEmojisMap(emojisMap: Map<string, string[]>) {

        if (emojisMap === undefined || !(emojisMap instanceof Map)) {
            throw new BadModelFieldException("emojis map must be a valid Map");
        }

        for (const key of emojisMap.keys()) {
            if (key === undefined || typeof key !== 'string') {
                throw new BadModelFieldException("something is wrong with the model (emojisMap)");
            }

            const emoji: string[] | undefined = emojisMap.get(key);
            if (emoji === undefined){
                throw new BadModelFieldException("something is wrong with the model (emojisMap)");
            }

            for (const userId of emoji) {
                if (userId === undefined || typeof userId !== 'string') {
                    throw new BadModelFieldException("something is wrong with the model (emojisMap)");
                }

                let maxLength:number = 60;
                if (userId.length > maxLength){
                    throw new BadModelFieldException("something is wrong with the model (emojisMap)");
                }
            }
        }

        this.emojisMap = emojisMap;
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

    public getPreviousMessageCreatedAtUTC(): number {
        return this.previousMessageCreatedAtUTC;
    }

    public setPreviousMessageCreatedAtUTC(previousMessageCreatedAtUTC: number) {

        if (previousMessageCreatedAtUTC === undefined || typeof previousMessageCreatedAtUTC !== 'number' || isNaN(previousMessageCreatedAtUTC)) {
            throw new BadModelFieldException("previous created at must be a number");
        }


        this.previousMessageCreatedAtUTC = previousMessageCreatedAtUTC;
    }

    public getLastModifiedTimeInUTC(): number {
        return this.lastModifiedTimeInUTC;
    }

    public setLastModifiedTimeInUTC(lastModifiedTimeInUTC: number): void {

        const currentDate = new Date();

        this.lastModifiedTimeInUTC = currentDate.getTime();
    }

    public setLastModifiedTimeInUTCEmpty(): void {
        const currentDate = new Date();

        this.lastModifiedTimeInUTC = currentDate.getTime();
    }

    public isFirstMessage() {
        return this.getMessageType() === ChatMessageType.ONLY_FOR_FIRST_MESSAGE;
    }


}
