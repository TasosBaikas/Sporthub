/* eslint-disable */

import {TerrainAddress} from "../TerrainAddress";
import {UserShortForm} from "../user/UserShortForm";
import {ChatMessage} from "./ChatMessage";
import {BadModelFieldException} from "../../exceptions/BadModelFieldException";
import {SportConstants} from "../constants/SportConstants";
import {ChatTypes} from "../constants/ChatTypes";
import {HasTerrainTypes} from "../constants/HasTerrainType";
import {MatchDuration} from "../constants/MatchDuration";

export class Chat {

    private id!: string;
    private sport!: string;
    private adminId!: string;
    private chatType!: string;
    private chatCreatedAtUTC!: number;
    private matchDetailsFromAdmin!: string;
    private matchDateInUTC!: number;
    private matchDuration!: number;
    private modifiedTimeByLastChatMessageUTC!: number;
    private chatMatchIsRelevant!: boolean;
    private hasTerrainType!: string;
    private terrainAddress!: TerrainAddress | null; // You need to define the type for TerrainAddress
    private privateConversation2Users!: UserShortForm[];
    private lastChatMessage!: ChatMessage | null; // You need to define the type for ChatMessage
    private pinnedMessage!: ChatMessage | null;
    private notSeenByUsersId!: string[];
    private membersIds!: string[];
    private lastModifiedTimeInUTC!: number;


    constructor() {

    }


    static makeChatMatchConversation(id: any, sport: any, adminId: string,
                                     matchDetailsFromAdmin: string, matchDateInUTC: number, matchDuration: number,
                                     hasTerrainType: string, terrainAddress: TerrainAddress | null, lastChatMessage: ChatMessage, pinnedMessage: ChatMessage | null,
                                     notSeenByUsersId: string[], chatMembers: string[]): Chat {

        const chat:Chat = new Chat();


        chat.setId(id);
        chat.setChatType(ChatTypes.MATCH_CONVERSATION);

        chat.setAdminId(adminId);


        chat.setSport(sport);
        chat.setMatchDateInUTC(matchDateInUTC);
        chat.setMatchDuration(matchDuration);
        chat.setMatchDetailsFromAdmin(matchDetailsFromAdmin);
        chat.setHasTerrainType(hasTerrainType);
        chat.setTerrainAddress(terrainAddress);

        chat.setChatMatchIsRelevant(matchDateInUTC > Date.now());

        chat.setPrivateConversation2Users([]);

        chat.setChatCreatedAtUTC(Date.now());
        chat.setNotSeenByUsersId(notSeenByUsersId);

        chat.setLastChatMessage(lastChatMessage);
        chat.setPinnedMessage(pinnedMessage);
        chat.setMembersIds(chatMembers);

        chat.setLastModifiedTimeInUTCEmpty();

        return chat;
    }


    static makeChatPrivateConversation(id: any,lastChatMessage: ChatMessage,
                                         notSeenByUsersId: string[], privateConversation2Users: UserShortForm[]): Chat {

        const chat:Chat = new Chat();

        chat.setId(id);
        chat.setChatType(ChatTypes.PRIVATE_CONVERSATION);


        chat.setAdminId(privateConversation2Users[0].getUserId());
        chat.setSport("");
        chat.setMatchDateInUTC(-1);
        chat.setMatchDuration(-1);
        chat.setMatchDetailsFromAdmin("");
        chat.setHasTerrainType("");
        chat.setTerrainAddress(null);

        chat.setChatMatchIsRelevant(false);

        chat.setPrivateConversation2Users(privateConversation2Users);


        chat.setChatCreatedAtUTC(Date.now());
        chat.setNotSeenByUsersId(notSeenByUsersId);

        chat.setLastChatMessage(lastChatMessage);
        chat.setPinnedMessage(null);

        const membersIds:string[] = [];
        for (const userShortForm of privateConversation2Users) {

            membersIds.push(userShortForm.getUserId());
        }

        if (membersIds.length != 2)
            throw new BadModelFieldException('private conversation has only 2 participants');

        chat.setMembersIds(membersIds);
        chat.setLastModifiedTimeInUTCEmpty();

        return chat;
    }


    static makeInstanceFromSnapshot(snapshotNeedsParsing: any): Chat {

        const chat:Chat = new Chat();

        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(snapshotNeedsParsing);
        }catch (Error){
            parsedSnapshot = snapshotNeedsParsing;
        }

        chat.setId(parsedSnapshot.id)
        chat.setChatType(parsedSnapshot.chatType)

        chat.setAdminId(parsedSnapshot.adminId)

        let sport: string;
        let matchDateInUTC: number;
        let matchDuration: number;
        let matchDetailsFromAdmin: string;
        let hasTerrainType: string;
        let terrainAddress: TerrainAddress | null = null;
        let chatMatchIsRelevant: boolean;
        let privateConversation2Users: UserShortForm[] = [];

        if (chat.isPrivateConversation()){
            console.log("private")
            sport = "";
            matchDateInUTC = -1;
            matchDuration = -1;

            matchDetailsFromAdmin = "";
            hasTerrainType = "";
            terrainAddress = null;
            chatMatchIsRelevant = false;

            if (parsedSnapshot.privateConversation2Users) {

                for (const rawUser of parsedSnapshot.privateConversation2Users) {
                    if (rawUser === undefined)
                        continue;

                    privateConversation2Users.push(UserShortForm.makeInstanceFromSnapshot(rawUser));
                }
            }
        }else{
            console.log("not private")

            sport = parsedSnapshot.sport;
            matchDateInUTC = parsedSnapshot.matchDateInUTC;
            matchDuration = parsedSnapshot.matchDuration;
            matchDetailsFromAdmin = parsedSnapshot.matchDetailsFromAdmin;

            hasTerrainType = parsedSnapshot.hasTerrainType;

            if (parsedSnapshot.terrainAddress)
                terrainAddress = TerrainAddress.makeInstanceFromSnapshot(parsedSnapshot.terrainAddress);

            chatMatchIsRelevant = matchDateInUTC > Date.now();
            privateConversation2Users = [];
        }

        chat.setSport(sport);
        chat.setMatchDateInUTC(matchDateInUTC);
        chat.setMatchDuration(matchDuration);
        chat.setMatchDetailsFromAdmin(matchDetailsFromAdmin);
        chat.setHasTerrainType(hasTerrainType);
        chat.setTerrainAddress(terrainAddress);
        chat.setChatMatchIsRelevant(chatMatchIsRelevant);
        chat.setPrivateConversation2Users(privateConversation2Users);


        chat.setChatCreatedAtUTC(parsedSnapshot.chatCreatedAtUTC);
        chat.setModifiedTimeByLastChatMessageUTC(parsedSnapshot.modifiedTimeByLastChatMessageUTC);


        let lastChatMessage: ChatMessage | null = null;
        if (parsedSnapshot.lastChatMessage)
            lastChatMessage = ChatMessage.makeInstanceFromSnapshot(parsedSnapshot.lastChatMessage);

        chat.setLastChatMessage(lastChatMessage);


        let pinnedMessage: ChatMessage | null = null;
        if (parsedSnapshot.pinnedMessage)
            pinnedMessage = ChatMessage.makeInstanceFromSnapshot(parsedSnapshot.pinnedMessage);


        chat.setPinnedMessage(pinnedMessage);

        chat.setNotSeenByUsersId(parsedSnapshot.notSeenByUsersId);
        chat.setMembersIds(parsedSnapshot.membersIds);

        chat.setLastModifiedTimeInUTCEmpty();

        return chat;
    }

    toObject() {//this is the output that goes to firestore so becareful with the changes in this class

        let terrainAddressForFirestore:{} | null = null;
        if (this.terrainAddress !== null)
            terrainAddressForFirestore = this.terrainAddress.toObject();


        let lastChatMessageForFirestore:{} | null = null;
        if (this.lastChatMessage !== null)
            lastChatMessageForFirestore = this.lastChatMessage.toObject();

        let pinnedMessageForFirestore:{} | null = null;
        if (this.pinnedMessage !== null)
            pinnedMessageForFirestore = this.pinnedMessage.toObject();

        const privateConversation2UsersForFirestore:{}[] = [];
        this.privateConversation2Users.forEach((userShortForm) => {
            privateConversation2UsersForFirestore.push(userShortForm.toObject());
        });


        return {
            id: this.id,
            sport: this.sport,
            adminId: this.adminId,
            chatType: this.chatType,
            chatCreatedAtUTC: this.chatCreatedAtUTC,
            matchDetailsFromAdmin: this.matchDetailsFromAdmin,
            matchDateInUTC: this.matchDateInUTC,
            matchDuration: this.matchDuration,
            modifiedTimeByLastChatMessageUTC: this.modifiedTimeByLastChatMessageUTC,
            chatMatchIsRelevant: this.chatMatchIsRelevant,
            hasTerrainType: this.hasTerrainType,
            terrainAddress: terrainAddressForFirestore,
            privateConversation2Users: privateConversation2UsersForFirestore,
            lastChatMessage: lastChatMessageForFirestore,
            pinnedMessage: pinnedMessageForFirestore,
            notSeenByUsersId: this.notSeenByUsersId,
            membersIds: this.membersIds,
            lastModifiedTimeInUTC: this.lastModifiedTimeInUTC,
            // add other fields as needed
        };
    }


    public getId(): string {
        return this.id;
    }

    public setId(id: string) {

        if (id === undefined || typeof id !== 'string') {
            throw new BadModelFieldException("id must be a string");
        }

        let maxLength:number = 60;
        if (id.length > maxLength){
            throw new BadModelFieldException("id must be less than " + maxLength + " letters");
        }

        this.id = id;
    }

    public getSport(): string {
        return this.sport;
    }

    public setSport(sport: string) {

        if (sport === undefined || typeof sport !== 'string') {
            throw new BadModelFieldException("sport must be a valid string");
        }

        if (this.isPrivateConversation()){
            this.sport = sport;
            return;
        }

        let maxLength:number = 50;
        if (sport.length > maxLength){
            throw new BadModelFieldException("sport must be less than " + maxLength + " letters");
        }

        const found: boolean = SportConstants.SPORTSLIST.includes(sport);
        if (!found)
            throw new BadModelFieldException("sport bad value");


        this.sport = sport;
    }

    public getAdminId(): string {
        return this.adminId;
    }

    public setAdminId(adminId: string) {

        if (this.isPrivateConversation()){
            this.adminId = adminId;
            return;
        }

        if (adminId === undefined || adminId === "" || typeof adminId !== 'string') {
            throw new BadModelFieldException("id must be a string");
        }


        let maxLength:number = 60;
        if (adminId.length > maxLength){
            throw new BadModelFieldException("id must be less than " + maxLength + " letters");
        }

        this.adminId = adminId;
    }

    public isPrivateConversation(): boolean {
        return this.getChatType() == ChatTypes.PRIVATE_CONVERSATION;
    }

    public getChatType(): string {
        return this.chatType;
    }

    public setChatType(chatType: string) {

        if (chatType === undefined || typeof chatType !== 'string') {
            throw new BadModelFieldException("sport must be a valid string");
        }

        let maxLength:number = 50;
        if (chatType.length > maxLength){
            throw new BadModelFieldException("sport must be less than " + maxLength + " letters");
        }

        const found: boolean = ChatTypes.CHAT_TYPES_LIST.includes(chatType);
        if (!found)
            throw new BadModelFieldException("sport bad value");


        this.chatType = chatType;
    }

    public getChatCreatedAtUTC(): number {
        return this.chatCreatedAtUTC;
    }

    public setChatCreatedAtUTC(chatCreatedAtUTC: number) {

        if (chatCreatedAtUTC === undefined || typeof chatCreatedAtUTC !== 'number' || isNaN(chatCreatedAtUTC)) {
            throw new BadModelFieldException("created at must be a number");
        }

        this.chatCreatedAtUTC = chatCreatedAtUTC;
    }

    public getMatchDetailsFromAdmin(): string {
        return this.matchDetailsFromAdmin;
    }

    public setMatchDetailsFromAdmin(matchDetailsFromAdmin: string) {
        if (matchDetailsFromAdmin === undefined || matchDetailsFromAdmin === null){
            this.matchDetailsFromAdmin = "";
            return
        }

        if (matchDetailsFromAdmin === undefined || typeof matchDetailsFromAdmin !== 'string') {
            throw new BadModelFieldException("match details from admin must be a valid string");
        }

        let maxLength:number = 1000;
        if (matchDetailsFromAdmin.length > maxLength){
            throw new BadModelFieldException("match details from admin must be less than " + maxLength + " letters");
        }


        this.matchDetailsFromAdmin = matchDetailsFromAdmin;
    }

    public getMatchDateInUTC(): number {
        return this.matchDateInUTC;
    }

    public setMatchDateInUTC(matchDateInUTC: number) {

        if (matchDateInUTC === undefined || typeof matchDateInUTC !== 'number' || isNaN(matchDateInUTC)) {
            throw new BadModelFieldException("match date must be a number");
        }


        this.matchDateInUTC = matchDateInUTC;
    }

    public getMatchDuration(): number {
        return this.matchDuration;
    }

    public setMatchDuration(matchDuration: number) {

        if (matchDuration === undefined || typeof matchDuration !== 'number') {
            throw new BadModelFieldException("match duration must be a number");
        }

        if (matchDuration == -1){
            this.matchDuration = -1;
            return;
        }

        const minDuration:number = 0;
        if (matchDuration < minDuration)
            throw new BadModelFieldException("match duration must be higher than " + minDuration);

        if (!MatchDuration.checkDurationThatIsValidReturnBoolean(matchDuration))
            throw new BadModelFieldException("match duration is not valid");


        this.matchDuration = matchDuration;
    }

    public getModifiedTimeByLastChatMessageUTC(): number {
        return this.modifiedTimeByLastChatMessageUTC;
    }

    public setModifiedTimeByLastChatMessageUTC(modifiedTimeByLastChatMessageUTC: number) {

        if (modifiedTimeByLastChatMessageUTC === undefined || typeof modifiedTimeByLastChatMessageUTC !== 'number' || isNaN(modifiedTimeByLastChatMessageUTC)) {
            throw new BadModelFieldException("created at must be a number");
        }

        this.modifiedTimeByLastChatMessageUTC = modifiedTimeByLastChatMessageUTC;
    }

    public getChatMatchIsRelevant(): boolean {
        return this.chatMatchIsRelevant;
    }

    public setChatMatchIsRelevant(chatMatchIsRelevant: boolean) {
        if (chatMatchIsRelevant === undefined || typeof chatMatchIsRelevant !== 'boolean') {
            throw new BadModelFieldException("chat match is relevant must be a valid boolean");
        }

        if (this.matchDateInUTC){

            this.chatMatchIsRelevant = this.matchDateInUTC > Date.now()
            return;
        }

        this.chatMatchIsRelevant = chatMatchIsRelevant;
    }

    public getHasTerrainType(): string {
        return this.hasTerrainType;
    }

    public setHasTerrainType(hasTerrainType: string) {

        if (hasTerrainType === undefined || typeof hasTerrainType !== 'string') {
            throw new BadModelFieldException("hasTerrainType must be a string");
        }

        if (hasTerrainType === ""){
            this.hasTerrainType = "";
            return;
        }

        let maxLength:number = 50;
        if (hasTerrainType.length > maxLength){
            throw new BadModelFieldException("has terrain type must be less than " + maxLength + " letters");
        }

        const found: boolean = HasTerrainTypes.TERRAIN_OPTIONS_ENGLISH_LIST.includes(hasTerrainType);
        if (!found)
            throw new BadModelFieldException("has terrain type bad value");


        this.hasTerrainType = hasTerrainType;
    }

    public getTerrainAddress(): TerrainAddress | null {
        return this.terrainAddress;
    }

    public setTerrainAddress(terrainAddress: TerrainAddress | null) {
        if (terrainAddress === null) {
            this.terrainAddress = null;
            return
        }

        if (terrainAddress === undefined || !(terrainAddress instanceof TerrainAddress)) {
            throw new BadModelFieldException("something is wrong with terrainAddress");
        }

        this.terrainAddress = terrainAddress;
    }

    public getPrivateConversation2Users(): UserShortForm[] {
        return this.privateConversation2Users;
    }

    public setPrivateConversation2Users(users: UserShortForm[]) {

        for (const userShortForm of users) {
            if (userShortForm === undefined || !(userShortForm instanceof UserShortForm)) {
                throw new BadModelFieldException("something is wrong with user short form");
            }
        }


        this.privateConversation2Users = users;
    }

    public getLastChatMessage(): ChatMessage | null{
        return this.lastChatMessage;
    }

    public setLastChatMessage(chatMessage: ChatMessage | null) {
        if (chatMessage == null){
            this.lastChatMessage = null;
            return;
        }

        if (chatMessage === undefined || !(chatMessage instanceof ChatMessage)) {
            throw new BadModelFieldException("something is wrong with chat message");
        }


        this.lastChatMessage = chatMessage;

        this.setModifiedTimeByLastChatMessageUTC(chatMessage.getCreatedAtUTC());
    }

    public getPinnedMessage(): ChatMessage | null{
        return this.pinnedMessage;
    }

    public setPinnedMessage(pinnedMessage: ChatMessage | null): void {
        if (pinnedMessage == null){
            this.pinnedMessage = null;
            return;
        }

        if (pinnedMessage === undefined || !(pinnedMessage instanceof ChatMessage)) {
            throw new BadModelFieldException("something is wrong with pinnedMessage");
        }


        this.pinnedMessage = pinnedMessage;
    }

    public getNotSeenByUsersId(): string[] {
        return this.notSeenByUsersId;
    }

    public setNotSeenByUsersId(notSeenBy: string[]) {

        for (const userId of notSeenBy) {
            if (userId === undefined || typeof userId !== 'string') {
                throw new BadModelFieldException("notSeenBy something is wrong");
            }

            let maxLength:number = 60;
            if (userId.length > maxLength){
                throw new BadModelFieldException("the individual id in notSeenBy must be less than " + maxLength + " letters");
            }
        }

        let deepCopy = JSON.parse(JSON.stringify(notSeenBy));

        this.notSeenByUsersId = deepCopy;
    }

    public getMembersIds(): string[] {
        return this.membersIds;
    }

    public setMembersIds(membersIds: string[]) {

        for (const userId of membersIds) {
            if (userId === undefined || typeof userId !== 'string') {
                throw new BadModelFieldException("membersIds something is wrong");
            }

            let maxLength:number = 60;
            if (userId.length > maxLength){
                throw new BadModelFieldException("the individual id in membersIds must be less than " + maxLength + " letters");
            }
        }

        let deepCopy = JSON.parse(JSON.stringify(membersIds));


        this.membersIds = deepCopy;
    }

    public isAdmin(userId:string):boolean {
        if (this.isPrivateConversation())
            return true;

        return this.adminId == userId;
    }

    public isMember(userId:string):boolean {
       return this.membersIds.includes(userId);
    }

    public getLastModifiedTimeInUTC(): number{
        return this.lastModifiedTimeInUTC;
    }

    public setLastModifiedTimeInUTC(lastModifiedTimeInUTC: number) {
        this.lastModifiedTimeInUTC = Date.now();
    }

    public setLastModifiedTimeInUTCEmpty() {
        this.lastModifiedTimeInUTC = Date.now();
    }
}

