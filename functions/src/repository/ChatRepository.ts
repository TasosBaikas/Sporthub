/* eslint-disable */


import {ChatFirebaseDao} from "../firebasedao/ChatFirebaseDao";
import {Chat} from "../models/chat/Chat";
import {UserNotificationsFirebaseDao} from "../firebasedao/UserNotificationsFirebaseDao";
import {ValidationException} from "../exceptions/ValidationException";
import {MatchFirebaseDao} from "../firebasedao/MatchFirebaseDao";
import {Match} from "../models/Match";
import {UserNotification} from "../models/user/usernotification/UserNotification";
import {MatchesBatchFirebaseDao} from "../firebasedao/MatchesBatchFirebaseDao";
import {DocumentSnapshot} from "firebase-admin/lib/firestore";
import {UserFirebaseDao} from "../firebasedao/UserFirebaseDao";
import {UserShortForm} from "../models/user/UserShortForm";
import {User} from "../models/user/User";
import {ChatMessage} from "../models/chat/ChatMessage";
import {ChatMessageFirebaseDao} from "../firebasedao/ChatMessageFirebaseDao";
import {v4 as uuidv4} from "uuid";
import {ChatMessageBatch} from "../models/chat/ChatMessageBatch";
import {UserPrivacyFirebaseDao} from "../firebasedao/UserPrivacyFirebaseDao";
import {UserChatAction} from "../models/userprivacy/UserChatAction";
import {UserBlockedPlayers} from "../models/userprivacy/UserBlockedPlayers";

const admin = require('firebase-admin');

export class ChatRepository {

    private chatFirebaseDao:ChatFirebaseDao;
    private chatMessageFirebaseDao:ChatMessageFirebaseDao;
    private userNotificationsFirebaseDao:UserNotificationsFirebaseDao;
    private matchFirebaseDao:MatchFirebaseDao;
    private matchesBatchFirebaseDao:MatchesBatchFirebaseDao;
    private userFirebaseDao:UserFirebaseDao;
    private userPrivacyFirebaseDao:UserPrivacyFirebaseDao;

    constructor() {
        this.chatFirebaseDao = new ChatFirebaseDao();
        this.chatMessageFirebaseDao = new ChatMessageFirebaseDao();
        this.userNotificationsFirebaseDao = new UserNotificationsFirebaseDao();
        this.matchFirebaseDao = new MatchFirebaseDao();
        this.matchesBatchFirebaseDao = new MatchesBatchFirebaseDao();
        this.userFirebaseDao = new UserFirebaseDao();
        this.userPrivacyFirebaseDao = new UserPrivacyFirebaseDao();

    }

    public async updateChatOnlyForInsideCalls(chat:Chat): Promise<FirebaseFirestore.WriteResult> {

        return this.chatFirebaseDao.updateChat(chat);
    }


    public async updateChatLastMessageUserThatSendMessage(chat:Chat,userShortForm:UserShortForm): Promise<null> {


        return admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {

            if (!chat.isMember(userShortForm.getUserId()))
                return null;


            const lastMessage: ChatMessage | null = chat.getLastChatMessage();
            if (lastMessage == null)
                return null;

            lastMessage.setUserShortForm(userShortForm);

            this.chatFirebaseDao.updateChatTransactional(transaction,chat);
            return null;
        });
    }

    public async getChatById(chatId:string): Promise<Chat | undefined> {

        const chatSnapshot: FirebaseFirestore.DocumentSnapshot<FirebaseFirestore.DocumentData> = await this.chatFirebaseDao.getChatByIdPromise(chatId);

        const data: FirebaseFirestore.DocumentData | undefined = chatSnapshot.data();
        if (data === undefined)
            return undefined;

        return Chat.makeInstanceFromSnapshot(data);
    }

    public async getChatThatShouldChangeToIrrelevantPromise(timeNowInEpochMilli:number): Promise<Chat[]> {

        const snapshots: FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData> = await this.chatFirebaseDao.getChatThatShouldChangeToIrrelevantPromise(timeNowInEpochMilli);

        const chatList:Chat[] = [];
        snapshots.docs.forEach((snapshot: FirebaseFirestore.QueryDocumentSnapshot<FirebaseFirestore.DocumentData>) => {
            const data: FirebaseFirestore.DocumentData | undefined = snapshot.data();
            if (data === undefined)
                return;

            chatList.push(Chat.makeInstanceFromSnapshot(data));
        })


        return chatList;
    }

    public destroyChatIfPrivateConversation(yourId:string,chatId:string): Promise<void> {

        return admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {

            const chatSnapshot: FirebaseFirestore.DocumentSnapshot<FirebaseFirestore.DocumentData> = await this.chatFirebaseDao.getChatByIdTransaction(transaction,chatId);

            if (chatSnapshot == null || !chatSnapshot.exists){
                throw new ValidationException("Δεν υπάρχει το chat...");
            }

            const chat:Chat = Chat.makeInstanceFromSnapshot(chatSnapshot.data());
            if (!chat.isMember(yourId)){
                throw new ValidationException("Δεν είστε στην ομάδα");
            }

            if (!chat.isAdmin(yourId)) {
                throw new Error("Δεν είστε διαχειριστής!");
            }

            if (!chat.isPrivateConversation()){
                throw new Error("Το chat είναι match chat...");
            }

            this.chatFirebaseDao.deleteChatTransaction(transaction,chatId);
        });

    }

    public async leaveChatIfMatchConversation(yourId:string,chatId:string): Promise<void> {

        return admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {

            const chatSnapshot: FirebaseFirestore.DocumentSnapshot<FirebaseFirestore.DocumentData> = await this.chatFirebaseDao.getChatByIdTransaction(transaction,chatId);
            if (chatSnapshot == null || !chatSnapshot.exists){
                throw new ValidationException("Δεν υπάρχει το chat...");
            }

            const chat:Chat = Chat.makeInstanceFromSnapshot(chatSnapshot.data());
            if (!chat.isMember(yourId)){
                throw new ValidationException("Δεν είστε στην ομάδα");
            }

            if (chat.isAdmin(yourId)) {
                throw new ValidationException("Πρέπει να δώσετε την διαχείριση πρώτα!");
            }

            if (chat.isPrivateConversation()){
                throw new ValidationException("Το chat είναι private...");
            }

            const matchSnapshot: FirebaseFirestore.DocumentSnapshot<FirebaseFirestore.DocumentData> = await this.matchFirebaseDao.getMatchByIdTransaction(transaction,chat.getId(),chat.getSport());
            await this.matchesBatchFirebaseDao.getMatchFromTemporalMatchHolderTransaction(transaction,chat.getId(),chat.getSport());
            if (matchSnapshot == null || !matchSnapshot.exists){
                throw new ValidationException("Η ομάδα δεν υπάρχει πλεον...");
            }

            const match:Match = Match.makeInstanceFromSnapshot(matchSnapshot.data());

            const userNotificationSnapshot: FirebaseFirestore.DocumentSnapshot<FirebaseFirestore.DocumentData> = await this.userNotificationsFirebaseDao.getUserNotificationsByIdTransaction(transaction,chatId);
            if (userNotificationSnapshot != null && userNotificationSnapshot.exists){
                const userNotifications:UserNotification = UserNotification.makeInstanceFromSnapshot(userNotificationSnapshot.data());
                userNotifications.getNotificationsBasedOnChats().delete(chatId);

                this.userNotificationsFirebaseDao.updateUserNotificationsTransaction(transaction,userNotifications);
            }

            let index = chat.getMembersIds().findIndex(id => id === yourId);
            if (index !== -1) {
                chat.getMembersIds().splice(index, 1);
            }

            index = match.getUsersInChat().findIndex(id => id === yourId);
            if (index !== -1) {
                match.getUsersInChat().splice(index, 1);
            }

            index = chat.getNotSeenByUsersId().findIndex(id => id === yourId);
            if (index !== -1) {
                chat.getNotSeenByUsersId().splice(index, 1);
            }

            this.chatFirebaseDao.updateChatTransactional(transaction,chat);
            this.matchFirebaseDao.updateMatchTransaction(transaction,match);
            this.matchesBatchFirebaseDao.updateMatchInTemporalHolderTransaction(transaction,match);
        });

    }


    async kickUserIfMatchConversation(userToKick: string, adminIdFromContext:string, chatId: string): Promise<void> {

        return admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {

            const chatSnapshot: FirebaseFirestore.DocumentSnapshot<FirebaseFirestore.DocumentData> = await this.chatFirebaseDao.getChatByIdTransaction(transaction,chatId);
            if (chatSnapshot == null || !chatSnapshot.exists){
                throw new ValidationException("Δεν υπάρχει το chat...");
            }

            const chat:Chat = Chat.makeInstanceFromSnapshot(chatSnapshot.data());
            if (!chat.isMember(userToKick)){
                throw new ValidationException("Ο χρήστης δεν είναι στην ομάδα");
            }

            if (!chat.isAdmin(adminIdFromContext)) {
                throw new ValidationException("Δεν είστε διαχειριστής!");
            }

            if (chat.isPrivateConversation()){
                throw new ValidationException("Το chat είναι private...");
            }

            const matchSnapshot: FirebaseFirestore.DocumentSnapshot<FirebaseFirestore.DocumentData> = await this.matchFirebaseDao.getMatchByIdTransaction(transaction,chat.getId(),chat.getSport());
            await this.matchesBatchFirebaseDao.getMatchFromTemporalMatchHolderTransaction(transaction,chat.getId(),chat.getSport());
            if (matchSnapshot == null || !matchSnapshot.exists){
                throw new ValidationException("Η ομάδα δεν υπάρχει πλεον...");
            }

            const match:Match = Match.makeInstanceFromSnapshot(matchSnapshot.data());

            const userNotificationSnapshot: FirebaseFirestore.DocumentSnapshot<FirebaseFirestore.DocumentData> = await this.userNotificationsFirebaseDao.getUserNotificationsByIdTransaction(transaction,chatId);
            if (userNotificationSnapshot != null && userNotificationSnapshot.exists){
                const userNotifications:UserNotification = UserNotification.makeInstanceFromSnapshot(userNotificationSnapshot.data());
                userNotifications.getNotificationsBasedOnChats().delete(chatId);

                this.userNotificationsFirebaseDao.updateUserNotificationsTransaction(transaction,userNotifications);
            }

            let index = chat.getMembersIds().findIndex(id => id === userToKick);
            if (index !== -1) {
                chat.getMembersIds().splice(index, 1);
            }

            index = match.getUsersInChat().findIndex(id => id === userToKick);
            if (index !== -1) {
                match.getUsersInChat().splice(index, 1);
            }

            index = chat.getNotSeenByUsersId().findIndex(id => id === userToKick);
            if (index !== -1) {
                chat.getNotSeenByUsersId().splice(index, 1);
            }


            this.chatFirebaseDao.updateChatTransactional(transaction,chat);
            this.matchFirebaseDao.updateMatchTransaction(transaction,match);
            this.matchesBatchFirebaseDao.updateMatchInTemporalHolderTransaction(transaction,match);
        });


    }


    async destroyChatIfMatchConversation(yourId: string, chatId: string) {

        return admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {

                const chatSnapshot:DocumentSnapshot = await this.chatFirebaseDao.getChatByIdTransaction(transaction,chatId);
                if (chatSnapshot == null || !chatSnapshot.exists)
                    throw new ValidationException("Η ομάδα δεν υπάρχει πλεον...");

                const chat:Chat = Chat.makeInstanceFromSnapshot(chatSnapshot.data());
                if (!chat.isMember(yourId))
                    throw new ValidationException("Δεν είστε στην ομάδα");

                if (!chat.isAdmin(yourId))
                    throw new ValidationException("Δεν είστε διαχειριστής");


                const matchSnapshot:DocumentSnapshot = await this.matchFirebaseDao.getMatchByIdTransaction(transaction,chat.getId(),chat.getSport());
                await this.matchesBatchFirebaseDao.getMatchFromTemporalMatchHolderTransaction(transaction,chat.getId(),chat.getSport());
                if (matchSnapshot == null || !matchSnapshot.exists)
                    throw new ValidationException("Η ομάδα δεν υπάρχει πλεον...");

                const match:Match = Match.makeInstanceFromSnapshot(matchSnapshot.data());


                this.matchFirebaseDao.deleteMatchTransaction(transaction,match.getId(), match.getSport());
                this.chatFirebaseDao.deleteChatTransaction(transaction,chat.getId());

                match.setMatchDateInUTC(87965);
                this.matchesBatchFirebaseDao.updateMatchInTemporalHolderTransaction(transaction,match);
        });


    }


    async destroyChatIfAdminElseLeave_AccountDeletion(yourId: string, chatId: string): Promise<void> {

        return admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {

            const chatSnapshot:DocumentSnapshot = await this.chatFirebaseDao.getChatByIdTransaction(transaction,chatId);
            if (chatSnapshot == null || !chatSnapshot.exists){
                return;
            }

            console.log("inside chatRepo deleteAccount logic 1");

            const chat:Chat = Chat.makeInstanceFromSnapshot(chatSnapshot.data());
            if (!chat.isMember(yourId))
                return;

            console.log("inside chatRepo deleteAccount logic 2");

            if (chat.isPrivateConversation()){
                this.chatFirebaseDao.deleteChatTransaction(transaction,chatId);
                return;
            }

            console.log("inside chatRepo deleteAccount logic 3");

            const lastChatMessage:ChatMessage | null = chat.getLastChatMessage();
            if (lastChatMessage != null && lastChatMessage.getUserShortForm().getUserId() === yourId){
                lastChatMessage.setUserShortForm(UserShortForm.makeDeletedUserShortForm(yourId));
            }


            const matchSnapshot:DocumentSnapshot = await this.matchFirebaseDao.getMatchByIdTransaction(transaction,chat.getId(),chat.getSport());
            await this.matchesBatchFirebaseDao.getMatchFromTemporalMatchHolderTransaction(transaction,chat.getId(),chat.getSport());
            if (matchSnapshot == null || !matchSnapshot.exists){
                this.chatFirebaseDao.deleteChatTransaction(transaction,chatId);
                return;
            }

            const match:Match = Match.makeInstanceFromSnapshot(matchSnapshot.data());

            console.log("inside chatRepo deleteAccount logic 4");

            if (chat.isAdmin(yourId)){

                this.matchFirebaseDao.deleteMatchTransaction(transaction,match.getId(), match.getSport());

                match.setMatchDateInUTC(87965);
                this.matchesBatchFirebaseDao.updateMatchInTemporalHolderTransaction(transaction,match);

                this.chatFirebaseDao.deleteChatTransaction(transaction,chatId);

                console.log("inside chatRepo deleteAccount logic 5");

                return;
            }

            let index = match.getUsersInChat().findIndex(userId => userId === yourId);

            // If the user was found, remove them from the array
            if (index !== -1) {
                match.getUsersInChat().splice(index, 1);
            }

            console.log("inside chatRepo deleteAccount logic 6");


            index = chat.getMembersIds().findIndex(userId => userId === yourId);

            // If the user was found, remove them from the array
            if (index !== -1) {
                chat.getMembersIds().splice(index, 1);
            }

            console.log("inside chatRepo deleteAccount logic 7");

            this.chatFirebaseDao.updateChatTransactional(transaction,chat);
            this.matchFirebaseDao.updateMatchTransaction(transaction,match);

            this.matchesBatchFirebaseDao.updateMatchInTemporalHolderTransaction(transaction,match);

        });


    }

    async saveChatIfMatchConversation(chat:Chat,yourId:string): Promise<FirebaseFirestore.WriteResult> {

        const chatSeeIfExist:Chat | undefined = await this.getChatById(chat.getId());
        if (chatSeeIfExist !== undefined)
            throw new ValidationException('chat already exists');

        return admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {


            if (chat.isPrivateConversation())
                throw new ValidationException('It is not a match conversation');

            if (!chat.isAdmin(yourId))
                throw new ValidationException('you are not the admin');

            if (chat.getMembersIds().length >= 2)
                throw new ValidationException('you must be the only 1 in the chat');

            chat.setChatCreatedAtUTC(Date.now());

            console.log("chat1",chat);

            //now we make the first chatmessage
            const chatMessageBatchId: string = uuidv4();

            const youUserShortForm: UserShortForm = chat.getPrivateConversation2Users()
                .filter((user: UserShortForm) => user.getUserId() === yourId)[0];

            const firstChatMessage: ChatMessage = ChatMessage.chatFirstMessageInstance(chatMessageBatchId, chat.getId(), youUserShortForm);


            const chatMessageBatch: ChatMessageBatch = ChatMessageBatch.makeChatMessageBatch(chatMessageBatchId, firstChatMessage.getChatId(),
                [firstChatMessage], [], [firstChatMessage.getUserShortForm().getUserId()], Date.now());


            chat.setLastChatMessage(firstChatMessage);

            this.chatMessageFirebaseDao.saveChatMessageBatchTransactional(transaction, chatMessageBatch);

            return this.chatFirebaseDao.saveChatTransaction(transaction,chat);
        });
    }

    async saveChatIfPrivateConversation(chat: Chat, yourId:string): Promise<Chat> {

        const chatSeeIfExist: Chat | undefined = await this.getChatById(chat.getId());
        if (chatSeeIfExist !== undefined)
            throw new ValidationException('Το chat υπάρχει ήδη');

        return admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {

            const firstUserActivitySnapshot:DocumentSnapshot = await this.userPrivacyFirebaseDao.getUserActivityToAChatTransaction(transaction, yourId);

            const otherUser:UserShortForm = chat.getPrivateConversation2Users()
                .filter((user: UserShortForm) => user.getUserId() !== yourId)[0];

            const otherUserActivitySnapshot:DocumentSnapshot = await this.userPrivacyFirebaseDao.getUserActivityToAChatTransaction(transaction, otherUser.getUserId());


            const yourBlockedSnapshot:DocumentSnapshot = await this.userPrivacyFirebaseDao.getBlockedPlayersByUserId(yourId);
            if (yourBlockedSnapshot != null && yourBlockedSnapshot.exists) {

                const blockedPlayers: UserBlockedPlayers = UserBlockedPlayers.makeInstanceFromSnapshot(yourBlockedSnapshot.data());

                const blockedPlayersList:string[] = blockedPlayers.getBlockedPlayers();

                if (blockedPlayersList.includes(otherUser.getUserId()))
                    throw new ValidationException("Έχετε κάνει block τον παίκτη");
            }


            const otherBlockedSnapshot:DocumentSnapshot = await this.userPrivacyFirebaseDao.getBlockedPlayersByUserId(otherUser.getUserId());
            if (otherBlockedSnapshot != null && otherBlockedSnapshot.exists) {

                const blockedPlayers: UserBlockedPlayers = UserBlockedPlayers.makeInstanceFromSnapshot(otherBlockedSnapshot.data());

                const blockedPlayersList:string[] = blockedPlayers.getBlockedPlayers();

                if (blockedPlayersList.includes(yourId))
                    throw new ValidationException("Δεν γίνεται να φτιάξετε αυτό το chat");
            }


            if (!chat.isPrivateConversation())
                throw new ValidationException('It is not a private conversation');

            if (!chat.isMember(yourId))
                throw new ValidationException('you are not member');


            const found: number = chat.getPrivateConversation2Users()
                .filter((user: UserShortForm) => user.getUserId() === yourId)
                .length;

            if (found != 1)
                throw new ValidationException('you are not member');

            if (chat.getMembersIds().length != 2)
                throw new ValidationException('chat should have 2 members');


            chat.setChatCreatedAtUTC(Date.now());


            //now we make the first chatmessage
            const firstChatMessage:ChatMessage | null = chat.getLastChatMessage();
            if (firstChatMessage === null)
                throw new Error("Δεν υπάρχει first message");

            const chatMessageBatch: ChatMessageBatch = ChatMessageBatch.makeChatMessageBatch(firstChatMessage.getChatMessageBatchId(), firstChatMessage.getChatId(),
                [firstChatMessage], [], [firstChatMessage.getUserShortForm().getUserId()], Date.now());


            let firstUserChatAction:UserChatAction;
            if (firstUserActivitySnapshot == null || !firstUserActivitySnapshot.exists){
                firstUserChatAction = UserChatAction.makeInstance(yourId, []);
            }else{
                firstUserChatAction = UserChatAction.makeInstanceFromSnapshot(firstUserActivitySnapshot.data());
            }



            let otherUserChatAction:UserChatAction;
            if (otherUserActivitySnapshot == null || !otherUserActivitySnapshot.exists){
                otherUserChatAction = UserChatAction.makeInstance(yourId, []);
            }else{
                otherUserChatAction = UserChatAction.makeInstanceFromSnapshot(otherUserActivitySnapshot.data());
            }


            if (!firstUserChatAction.getChatsIds().includes(chat.getId())){

                firstUserChatAction.getChatsIds().push(chat.getId());

                this.userPrivacyFirebaseDao.updateUserActivityToAChat(transaction, firstUserChatAction);
            }

            if (!otherUserChatAction.getChatsIds().includes(chat.getId())){

                otherUserChatAction.getChatsIds().push(chat.getId());

                this.userPrivacyFirebaseDao.updateUserActivityToAChat(transaction, otherUserChatAction);
            }

            this.chatMessageFirebaseDao.saveChatMessageBatchTransactional(transaction, chatMessageBatch);

            this.chatFirebaseDao.saveChatTransaction(transaction,chat);

            return chat;
        });
    }


    async changeAdminIfMatchConversation(userToGiveAdmin:string, yourId:string, chatId:string): Promise<void> {

        return admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {

            const chatSnapshot:DocumentSnapshot = await this.chatFirebaseDao.getChatByIdTransaction(transaction,chatId);
            if (chatSnapshot == null || !chatSnapshot.exists)
                throw new ValidationException('Δεν υπάρχει το chat...');

            const chat:Chat = Chat.makeInstanceFromSnapshot(chatSnapshot.data());
            if (chat.isPrivateConversation())
                throw new ValidationException('Δεν είναι match conversation');

            if (!chat.isMember(userToGiveAdmin))
                throw new ValidationException('Ο παίκτης που θέλετε να δώσετε την διαχείριση δεν είναι στην ομάδα');


            if (!chat.isAdmin(yourId))
                throw new ValidationException('Δεν είστε διαχειριστής!');


            if (userToGiveAdmin === yourId)
                throw new ValidationException('Είστε ήδη διαχειριστής');


            const matchSnapshot:DocumentSnapshot = await this.matchFirebaseDao.getMatchByIdTransaction(transaction,chat.getId(),chat.getSport());
            await this.matchesBatchFirebaseDao.getMatchFromTemporalMatchHolderTransaction(transaction,chat.getId(),chat.getSport());
            if (matchSnapshot == null || !matchSnapshot.exists)
                throw new ValidationException('Δεν υπάρχει η ομάδα');

            const match:Match = Match.makeInstanceFromSnapshot(matchSnapshot.data());

            const newAdminSnap:DocumentSnapshot = await this.userFirebaseDao.getUserByIdTransaction(transaction, userToGiveAdmin);
            if (newAdminSnap == null || !newAdminSnap.exists)
                throw new ValidationException('Ο χρήστης έχει διαγράψει τον λογαριασμό του');

            const newAdmin:User = User.makeInstanceFromSnapshot(newAdminSnap.data());
            match.setAdmin(UserShortForm.makeShortForFromUser(newAdmin,match.getSport()));
            chat.setAdminId(userToGiveAdmin);


            this.chatFirebaseDao.updateChatTransactional(transaction,chat);
            this.matchFirebaseDao.updateMatchTransaction(transaction,match);
            this.matchesBatchFirebaseDao.updateMatchInTemporalHolderTransaction(transaction,match);
        });
    }

    async getChatsByUserThatSendLastMessage(userId: string): Promise<Chat[]> {

        const querySnapshot: FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData> = await this.chatFirebaseDao.getChatsByUserThatSendLastMessage(userId);

        const chats: Chat[] = [];
        querySnapshot.docs.forEach((snapshot: FirebaseFirestore.DocumentData) => {

            chats.push(Chat.makeInstanceFromSnapshot(snapshot.data()));
        });

        return chats;
    }

    async getPrivateChatsByUserId(userId: string): Promise<Chat[]> {

        const querySnapshot: FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData> = await this.chatFirebaseDao.getPrivateChatsByUserId(userId);

        const chats: Chat[] = [];
        querySnapshot.docs.forEach((snapshot: FirebaseFirestore.DocumentData) => {

            chats.push(Chat.makeInstanceFromSnapshot(snapshot.data()));
        });

        return chats;

    }


    async updateChatPrivate(privateChatUpdated: Chat): Promise<null> {

        return admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {

            if (!privateChatUpdated.isPrivateConversation())
                throw new ValidationException('It is not a private conversation');

            if (privateChatUpdated.getMembersIds().length != 2)
                throw new ValidationException('chat should have 2 members');

            const privateChatFromFirebaseSnapshot: FirebaseFirestore.DocumentSnapshot<FirebaseFirestore.DocumentData> = await this.chatFirebaseDao.getChatByIdTransaction(transaction, privateChatUpdated.getId());

            if (privateChatFromFirebaseSnapshot.data() == null)
                return null;

            const privateChatFromFirebase: Chat = Chat.makeInstanceFromSnapshot(privateChatFromFirebaseSnapshot.data());

            privateChatFromFirebase.setPrivateConversation2Users(privateChatUpdated.getPrivateConversation2Users());


            this.chatFirebaseDao.updateChatTransactional(transaction,privateChatFromFirebase);
            return null;
        });
    }

    async addOrRemoveYourPhoneNumberToThatChat(yourId: string, chatId: string, addOrRemove:boolean): Promise<boolean> {

        console.log(1);

        return admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {

            const chatSnapshot:DocumentSnapshot = await this.chatFirebaseDao.getChatByIdTransaction(transaction,chatId);
            if (chatSnapshot == null || !chatSnapshot.exists)
                throw new ValidationException('Δεν υπάρχει το chat...');

            const chat:Chat = Chat.makeInstanceFromSnapshot(chatSnapshot.data());
            if (chat.isPrivateConversation())
                throw new ValidationException('Δεν είναι match conversation');

            if (!chat.isMember(yourId))
                throw new ValidationException('Δεν είστε στην ομάδα');


            console.log(2);
            const permissionsDoc:DocumentSnapshot = await this.userPrivacyFirebaseDao.getPhoneNumberPermissionToThatChat(yourId, chatId);
            if (permissionsDoc === null || permissionsDoc === undefined || !permissionsDoc.exists){
                console.log(3);

                await this.userPrivacyFirebaseDao.setPhoneNumberPermissionToThatChat(yourId, chatId, addOrRemove);
                return true;
            }

            await this.userPrivacyFirebaseDao.addOrRemovePhoneNumberPermissionToThatChat(yourId, chatId, addOrRemove);

            return addOrRemove;
        });

    }

    async updatePinnedMessage(pinnedMessage:ChatMessage, yourId: string) {

        return admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {

            const chatSnapshot:DocumentSnapshot = await this.chatFirebaseDao.getChatByIdTransaction(transaction, pinnedMessage.getChatId());
            if (chatSnapshot == null || !chatSnapshot.exists)
                throw new ValidationException('Δεν υπάρχει το chat...');

            const chatFromFirebase:Chat = Chat.makeInstanceFromSnapshot(chatSnapshot.data());
            if (!chatFromFirebase.isAdmin(yourId))
                throw new ValidationException('Δεν είστε διαχειριστής');


            //I want fresh chatMessage from db
            const chatBatchSnapshot:DocumentSnapshot = await this.chatMessageFirebaseDao.getChatMessageBatchByIdTransactional(transaction, pinnedMessage.getChatId(), pinnedMessage.getChatMessageBatchId());
            if (chatBatchSnapshot == null || !chatBatchSnapshot.exists)
                throw new ValidationException("Το μήνυμα δεν υπάρχει πλέον");

            const chatMessageBatch:ChatMessageBatch = ChatMessageBatch.makeInstanceFromSnapshot(chatBatchSnapshot.data());

            const chatMessageFromFirebase: ChatMessage | undefined = chatMessageBatch.getChatMessages().find((chatMessageFromList:ChatMessage ) => chatMessageFromList.getId() === pinnedMessage.getId());
            if (chatMessageFromFirebase === undefined)
                throw new ValidationException("Το μήνυμα δεν υπάρχει πλέον");

            chatFromFirebase.setPinnedMessage(chatMessageFromFirebase);

            this.chatFirebaseDao.updateChatTransactional(transaction, chatFromFirebase);
            return null;
        });


    }

    async deletePinnedMessage(pinnedMessage: ChatMessage, yourId: string) {

        return admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {

            const chatSnapshot:DocumentSnapshot = await this.chatFirebaseDao.getChatByIdTransaction(transaction, pinnedMessage.getChatId());
            if (chatSnapshot == null || !chatSnapshot.exists)
                throw new ValidationException('Δεν υπάρχει το chat...');

            const chatFromFirebase:Chat = Chat.makeInstanceFromSnapshot(chatSnapshot.data());
            if (!chatFromFirebase.isAdmin(yourId))
                throw new ValidationException('Δεν είστε διαχειριστής');

            if (chatFromFirebase.getPinnedMessage() == null)
                throw new ValidationException('Δεν υπάρχει πλέον καρφιτσωμένο μήνυμα');


            chatFromFirebase.setPinnedMessage(null);

            this.chatFirebaseDao.updateChatTransactional(transaction, chatFromFirebase);
            return null;
        });

    }


    async getPrivateChatFromThe2Users(user1Id: string, user2Id: string): Promise<Chat|null> {

        const promises:Promise<FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData>>[] = [];

        promises.push(this.chatFirebaseDao.seeIfPrivateChatAlreadyExist(user1Id, user2Id));
        promises.push(this.chatFirebaseDao.seeIfPrivateChatAlreadyExist(user2Id, user1Id));

        const querySnapshots:FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData>[] = await Promise.all(promises);

        console.log(user1Id);
        console.log(user2Id);


        let chat:Chat|null = null;
        querySnapshots.forEach((querySnapshot: FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData>) => {

            querySnapshot.docs.forEach((snapshot: FirebaseFirestore.QueryDocumentSnapshot<FirebaseFirestore.DocumentData>) => {
                console.log("found");
                chat = Chat.makeInstanceFromSnapshot(snapshot.data());
            });

        });

        return chat;
    }
}

