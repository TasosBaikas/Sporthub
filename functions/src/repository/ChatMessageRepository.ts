/* eslint-disable */


import {ChatFirebaseDao} from "../firebasedao/ChatFirebaseDao";
import {Chat} from "../models/chat/Chat";
import {ValidationException} from "../exceptions/ValidationException";
import {DocumentSnapshot} from "firebase-admin/lib/firestore";
import {ChatMessageFirebaseDao} from "../firebasedao/ChatMessageFirebaseDao";
import {ChatMessage} from "../models/chat/ChatMessage";
import {ChatMessageBatch} from "../models/chat/ChatMessageBatch";
import {v4 as uuidv4} from "uuid";
import {DoNothingException} from "../exceptions/DoNothingException";
import {ChatMessageComparator} from "../helpers/comparators/ChatMessageComparator";
import {firestore} from "firebase-admin";
import QuerySnapshot = firestore.QuerySnapshot;
import {ListOperations} from "../helpers/operations/ListOperations";
import {EmojisTypes} from "../models/constants/EmojisTypes";
import {ChatMessageType} from "../models/constants/ChatMessageType";
import Transaction = firestore.Transaction;
import {UserShortForm} from "../models/user/UserShortForm";
// import {UserShortForm} from "../models/user/UserShortForm";
// import {ChatMessageType} from "../models/constants/ChatMessageType";

const admin = require('firebase-admin');

export class ChatMessageRepository {

    private chatFirebaseDao:ChatFirebaseDao;
    private chatMessageFirebaseDao:ChatMessageFirebaseDao;

    constructor() {
        this.chatFirebaseDao = new ChatFirebaseDao();
        this.chatMessageFirebaseDao = new ChatMessageFirebaseDao();

    }

    public async getLastChatMessageBatch(chatId:string): Promise<ChatMessageBatch | undefined> {

        const batchSnapshot: FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData> = await this.chatMessageFirebaseDao.getLastChatMessageBatch(chatId);

        if (batchSnapshot == null || batchSnapshot.empty || batchSnapshot.docs[0] == null || !batchSnapshot.docs[0].exists)
            return undefined;


        return ChatMessageBatch.makeInstanceFromSnapshot(batchSnapshot.docs[0].data());
    }


    public async saveChatMessage(chatMessage:ChatMessage, enabledConstraints:boolean): Promise<Chat | undefined> {

        if (chatMessage.getMessage() == null || chatMessage.getMessage().trim() === "")
            throw new DoNothingException("string empty");


        console.log("insiede saveCHatMessage");
        const batch: undefined | ChatMessageBatch = await this.getLastChatMessageBatch(chatMessage.getChatId());

        let isBatchMaxSize: boolean = false;
        let previousMessageCreatedAtUTC: number = 0;
        let previousMessageUserId: string = "";
        if (batch !== undefined) {
            isBatchMaxSize = batch.getChatMessages().length > ChatMessageBatch.MAX_MESSAGES;

            previousMessageCreatedAtUTC = batch.getChatMessages()[0].getCreatedAtUTC();
            previousMessageUserId = batch.getChatMessages()[0].getUserShortForm().getUserId();
        }

        if (batch === undefined || isBatchMaxSize) {

            return admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {
                console.log("first if")
                console.log(1);

                const chatSnapshot: DocumentSnapshot = await this.chatFirebaseDao.getChatByIdTransaction(transaction, chatMessage.getChatId());
                if (chatSnapshot == null || !chatSnapshot.exists)
                    throw new ValidationException("Το chat δεν υπάρχει πλέον");

                console.log(2);

                const chat: Chat = Chat.makeInstanceFromSnapshot(chatSnapshot.data());
                if (!chat.isMember(chatMessage.getUserShortForm().getUserId()) && enabledConstraints)
                    throw new ValidationException("User is not member");

                const createdAtUTC: number = Date.now();

                const chatMessageBatchId: string = uuidv4();
                chatMessage.setChatMessageBatchId(chatMessageBatchId);
                chatMessage.setPreviousMessageCreatedAtUTC(previousMessageCreatedAtUTC);
                chatMessage.setPreviousMessageUserId(previousMessageUserId);

                console.log(3);


                const chatMessageBatch: ChatMessageBatch = ChatMessageBatch.makeChatMessageBatch(chatMessageBatchId, chatMessage.getChatId(),
                    [chatMessage], [], [chatMessage.getUserShortForm().getUserId()], createdAtUTC);

                chat.setNotSeenByUsersId(chat.getMembersIds());


                const indexToRemove = chat.getNotSeenByUsersId().indexOf(chatMessage.getUserShortForm().getUserId());
                if (indexToRemove !== -1) {
                    chat.getNotSeenByUsersId().splice(indexToRemove, 1);
                }

                chat.setLastChatMessage(chatMessage);



                this.chatFirebaseDao.updateChatTransactional(transaction, chat);

                this.chatMessageFirebaseDao.saveChatMessageBatchTransactional(transaction, chatMessageBatch);

            });
        }


        return admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {

            console.log("second if")
            console.log(1);

            const chatSnapshot: DocumentSnapshot = await this.chatFirebaseDao.getChatByIdTransaction(transaction, chatMessage.getChatId());
            if (chatSnapshot == null || !chatSnapshot.exists)
                throw new ValidationException("Το chat δεν υπάρχει πλέον");

            console.log(2);

            const chat: Chat = Chat.makeInstanceFromSnapshot(chatSnapshot.data());
            if (!chat.isMember(chatMessage.getUserShortForm().getUserId()) && enabledConstraints)
                throw new ValidationException("User is not member");


            const batchId: string = batch.getId();
            const batchSnapshot: DocumentSnapshot = await this.chatMessageFirebaseDao.getChatMessageBatchByIdTransactional(transaction, chatMessage.getChatId(), batchId);
            if (batchSnapshot == null || !batchSnapshot.exists)
                throw new ValidationException("Δοκιμάστε ξανά");

            console.log(3);

            const chatMessageBatch: ChatMessageBatch = ChatMessageBatch.makeInstanceFromSnapshot(batchSnapshot.data());

            chatMessage.setChatMessageBatchId(chatMessageBatch.getId());
            chatMessage.setPreviousMessageCreatedAtUTC(previousMessageCreatedAtUTC);
            chatMessage.setPreviousMessageUserId(previousMessageUserId);

            const comparator:ChatMessageComparator = new ChatMessageComparator();

            chatMessageBatch.getChatMessages().sort((message1, message2) => comparator.compare(message1, message2));
            chatMessageBatch.getChatMessages().unshift(chatMessage);

            if (!chatMessageBatch.getUsersWithActivity().includes(chatMessage.getUserShortForm().getUserId()))
                chatMessageBatch.getUsersWithActivity().push(chatMessage.getUserShortForm().getUserId());


            chat.setNotSeenByUsersId(chat.getMembersIds());


            const indexToRemove = chat.getNotSeenByUsersId().indexOf(chatMessage.getUserShortForm().getUserId());
            if (indexToRemove !== -1) {
                chat.getNotSeenByUsersId().splice(indexToRemove, 1);
            }

            chat.setLastChatMessage(chatMessage);


            console.log(5);


            this.chatFirebaseDao.updateChatTransactional(transaction, chat);

            this.chatMessageFirebaseDao.updateChatMessageBatchTransactional(transaction, chatMessageBatch);

        });
    }

    public async changeSeenByUser(chatMessage:ChatMessage ,yourId:string) {

        if (chatMessage.getSeenByUsersId().includes(yourId))
            throw new DoNothingException("User already has seen the message");

        console.log("changeSeenByUser repo")

        console.log("changeSeenByUser repo 2")

        return admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {

            const batchSnapshots:QuerySnapshot = await this.chatMessageFirebaseDao.getChatMessageBatchByPlayerIdSeenBy(transaction,chatMessage.getChatId(), yourId);

            const batches:ChatMessageBatch[] = [];
            batchSnapshots.docs.forEach((batchSnapshot) => batches.push(ChatMessageBatch.makeInstanceFromSnapshot(batchSnapshot.data())));

            let batch:ChatMessageBatch | null;
            if (batches.length == 0)
                batch = null;
            else
                batch = batches[0];


            const latestMessageBatchSnapshot:DocumentSnapshot = await this.chatMessageFirebaseDao.getChatMessageBatchByIdTransactional(transaction,chatMessage.getChatId(),chatMessage.getChatMessageBatchId());
            const chatSnapshot:DocumentSnapshot = await this.chatFirebaseDao.getChatByIdTransaction(transaction,chatMessage.getChatId());

            if (latestMessageBatchSnapshot == null || !latestMessageBatchSnapshot.exists)
                throw new DoNothingException("Bug");

            if (chatSnapshot == null || !chatSnapshot.exists)
                throw new ValidationException("Το chat δεν υπάρχει");


            console.log("changeSeenByUser repo 3")

            const latestChatMessageBatch:ChatMessageBatch = ChatMessageBatch.makeInstanceFromSnapshot(latestMessageBatchSnapshot.data());
            if (!latestChatMessageBatch.getSeenByUsersId().includes(yourId))
                latestChatMessageBatch.getSeenByUsersId().push(yourId);

            if (!latestChatMessageBatch.getUsersWithActivity().includes(yourId))
                latestChatMessageBatch.getUsersWithActivity().push(yourId);

            console.log("changeSeenByUser repo 4")

            const comparator:ChatMessageComparator = new ChatMessageComparator();

            latestChatMessageBatch.getChatMessages().forEach((chatMessage: ChatMessage) => {
                chatMessage.setSeenByUsersId(chatMessage.getSeenByUsersId().filter(userId => userId !== yourId));
            });

            latestChatMessageBatch.getChatMessages().sort((chatMessage1,chatMessage2) => comparator.compare(chatMessage1,chatMessage2));

            const latestChatMessage:ChatMessage = latestChatMessageBatch.getChatMessages()[0];
            if (latestChatMessage.getSeenByUsersId().includes(yourId))
                throw new DoNothingException("User already has seen the message");


            if (batch != null)
                await this.removePreviousSeenBy(transaction, chatMessage, yourId, batch);


            latestChatMessage.getSeenByUsersId().push(yourId);

            console.log("changeSeenByUser repo 5")

            const chat:Chat = Chat.makeInstanceFromSnapshot(chatSnapshot.data());

            const index = chat.getNotSeenByUsersId().indexOf(yourId);
            if (index > -1) {
                chat.getNotSeenByUsersId().splice(index, 1);
            }

            console.log("changeSeenByUser repo 6")


            this.chatMessageFirebaseDao.updateChatMessageBatchTransactional(transaction,latestChatMessageBatch);

            console.log("changeSeenByUser repo 7")

            this.chatFirebaseDao.updateChatTransactional(transaction,chat);

            console.log("changeSeenByUser repo 8")

        });

    }


    private async removePreviousSeenBy(transaction: Transaction, chatMessage:ChatMessage , yourId:string, chatMessageBatch:ChatMessageBatch) {

        const batchId:string = chatMessageBatch.getId();
        const batchToDeleteSeenBySnapshot:DocumentSnapshot = await this.chatMessageFirebaseDao.getChatMessageBatchByIdTransactional(transaction,chatMessage.getChatId(),batchId);

        const previousChatMessageBatch:ChatMessageBatch  = ChatMessageBatch.makeInstanceFromSnapshot(batchToDeleteSeenBySnapshot.data());


        const filteredSeenByUsersId:string[] = previousChatMessageBatch.getSeenByUsersId().filter(id => id !== yourId);
        previousChatMessageBatch.setSeenByUsersId(filteredSeenByUsersId);

        for (const chatMessage1 of previousChatMessageBatch.getChatMessages()) {

            const index = chatMessage1.getSeenByUsersId().indexOf(yourId);
            if (index > -1) {
                chatMessage1.getSeenByUsersId().splice(index, 1);
            }
        }

        this.chatMessageFirebaseDao.updateChatMessageBatchTransactional(transaction,previousChatMessageBatch);
    }

    async updateEmojiCount(chatMessage: ChatMessage, emojiClicked: string, yourId: string) {

        if (!EmojisTypes.allPossibleEmojis.includes(emojiClicked))
            throw new ValidationException('That emoji does not exist.');


        return admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {


            const chatBatchSnapshot:DocumentSnapshot = await this.chatMessageFirebaseDao.getChatMessageBatchByIdTransactional(transaction, chatMessage.getChatId(), chatMessage.getChatMessageBatchId());
            if (chatBatchSnapshot == null || !chatBatchSnapshot.exists)
                throw new ValidationException("Το μήνυμα δεν υπάρχει πλέον");


            const chatMessageBatch:ChatMessageBatch = ChatMessageBatch.makeInstanceFromSnapshot(chatBatchSnapshot.data());
            console.log("chatMessageBatch",chatMessageBatch);
            const chatMessageFromFirebase: ChatMessage | undefined = chatMessageBatch.getChatMessages().find((chatMessageFromList:ChatMessage ) => chatMessageFromList.getId() === chatMessage.getId());
            if (chatMessageFromFirebase === undefined)
                throw new ValidationException("Το μήνυμα δεν υπάρχει πλέον");


            let usersThatHaveClickedEmoji:string[] | undefined = chatMessageFromFirebase.getEmojisMap().get(emojiClicked);
            if (usersThatHaveClickedEmoji === undefined)
                throw new Error("");
            console.log("usersThatHaveClickedEmoji 1",usersThatHaveClickedEmoji);

            if (usersThatHaveClickedEmoji.includes(yourId)){

                const index = usersThatHaveClickedEmoji.indexOf(yourId);
                if (index > -1) {
                    usersThatHaveClickedEmoji.splice(index, 1);
                }
            }else{
                usersThatHaveClickedEmoji.push(yourId);
            }

            console.log("usersThatHaveClickedEmoji 2",usersThatHaveClickedEmoji);


            const comparator:ChatMessageComparator = new ChatMessageComparator();

            ListOperations.updateOrAddAndSort(chatMessageFromFirebase, chatMessageBatch.getChatMessages(), comparator);

            console.log("hi 1");

            this.chatMessageFirebaseDao.updateChatMessageBatchTransactional(transaction, chatMessageBatch);

            console.log("hi 2");
        });


    }

    async virtualDeleteMessage(chatMessage: ChatMessage, yourIdFromContext: string): Promise<ChatMessage> {

        if (chatMessage.getUserShortForm().getUserId() !== yourIdFromContext)
            throw new ValidationException('You cant delete another one message.');


        return admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {


            const chatBatchSnapshot:DocumentSnapshot = await this.chatMessageFirebaseDao.getChatMessageBatchByIdTransactional(transaction, chatMessage.getChatId(), chatMessage.getChatMessageBatchId());
            if (chatBatchSnapshot == null || !chatBatchSnapshot.exists)
                throw new ValidationException("Το μήνυμα δεν υπάρχει πλέον");


            const chatMessageBatch:ChatMessageBatch = ChatMessageBatch.makeInstanceFromSnapshot(chatBatchSnapshot.data());

            const chatMessageFromFirebase: ChatMessage | undefined = chatMessageBatch.getChatMessages().find((chatMessageFromList:ChatMessage ) => chatMessageFromList.getId() === chatMessage.getId());
            if (chatMessageFromFirebase === undefined)
                throw new ValidationException("Το μήνυμα δεν υπάρχει πλέον");


            if (chatMessageFromFirebase.getMessageType() === ChatMessageType.DELETED)
                throw new ValidationException("Το μήνυμα έχει ήδη διαγραφεί");


            chatMessageFromFirebase.setMessage("Το μήνυμα διαγράφηκε");
            chatMessageFromFirebase.setMessageType(ChatMessageType.DELETED);

            console.log("chatMessageFromFirebase",chatMessageFromFirebase);

            const comparator:ChatMessageComparator = new ChatMessageComparator();

            ListOperations.updateOrAddAndSort(chatMessageFromFirebase, chatMessageBatch.getChatMessages(), comparator);

            console.log("hi 1");

            this.chatMessageFirebaseDao.updateChatMessageBatchTransactional(transaction, chatMessageBatch);

            return chatMessageFromFirebase;
        });

    }

    async getChatMessagePinnedMessage(chatId: string, yourId: string) {

        return admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {

            const chatSnapshot:DocumentSnapshot = await this.chatFirebaseDao.getChatByIdTransaction(transaction, chatId);
            if (chatSnapshot == null || !chatSnapshot.exists)
                throw new ValidationException('Δεν υπάρχει το chat...');

            const chat:Chat = Chat.makeInstanceFromSnapshot(chatSnapshot.data());

            if (!chat.isMember(yourId))
                throw new ValidationException('Δεν είστε στην ομάδα');

            const pinnedMessage:ChatMessage | null = chat.getPinnedMessage();
            if (pinnedMessage === null)
                throw new ValidationException('Δεν υπάρχει καρφιτσωμένο μήνυμα');


            const chatBatchSnapshot:DocumentSnapshot = await this.chatMessageFirebaseDao.getChatMessageBatchByIdTransactional(transaction, pinnedMessage.getChatId(), pinnedMessage.getChatMessageBatchId());
            if (chatBatchSnapshot == null || !chatBatchSnapshot.exists)
                throw new ValidationException("Το μήνυμα δεν υπάρχει πλέον");


            const chatMessageBatch:ChatMessageBatch = ChatMessageBatch.makeInstanceFromSnapshot(chatBatchSnapshot.data());

            const chatMessageFromFirebase: ChatMessage | undefined = chatMessageBatch.getChatMessages().find((chatMessageFromList:ChatMessage ) => chatMessageFromList.getId() === pinnedMessage.getId());
            if (chatMessageFromFirebase === undefined)
                throw new ValidationException("Το μήνυμα δεν υπάρχει πλέον");


            return chatMessageFromFirebase;
        });

    }

    async deleteChatMessagesOrSpoofData_AccountDeletion(id: string, chatId: string): Promise<void> {

        const chatSnapshot:DocumentSnapshot = await this.chatFirebaseDao.getChatByIdPromise(chatId);
        if (chatSnapshot == null || !chatSnapshot.exists){
            console.log("inside chatMessageRepository deleteAccount logic -1");
            await this.chatMessageFirebaseDao.deleteAllChatMessagesBatch(chatId);
            console.log("inside chatMessageRepository deleteAccount logic 0");
            return;
        }

        return admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {

            const chatSnapshot:DocumentSnapshot = await this.chatFirebaseDao.getChatByIdTransaction(transaction, chatId);
            if (chatSnapshot == null || !chatSnapshot.exists){
                console.log("inside chatMessageRepository deleteAccount logic 0.5");
                return;
            }

            const batchSnapshots:QuerySnapshot = await this.chatMessageFirebaseDao.getChatMessageBatchByUserAction(transaction, chatId, id);

            console.log("inside chatMessageRepository deleteAccount logic 1");

            const chatMessageBatches:ChatMessageBatch[] = [];
            batchSnapshots.docs.forEach((batchSnapshot) => chatMessageBatches.push(ChatMessageBatch.makeInstanceFromSnapshot(batchSnapshot.data())));

            if (chatMessageBatches.length == 0)
                return;


            console.log("inside chatMessageRepository deleteAccount logic 2");

            chatMessageBatches.forEach((chatMessageBatch: ChatMessageBatch) => {//adds much latency

                const chatMessages: ChatMessage[] = chatMessageBatch.getChatMessages();

                for (const chatMessage of chatMessages) {

                    if (chatMessage.getUserShortForm().getUserId() !== id)
                        continue;

                    if (chatMessage.getMessageType() === ChatMessageType.ONLY_FOR_FIRST_MESSAGE)
                        continue;


                    const nullUser: UserShortForm = UserShortForm.makeDeletedUserShortForm(id);

                    chatMessage.setUserShortForm(nullUser);
                }

                let index = chatMessageBatch.getUsersWithActivity().findIndex((userId:string) => userId === id);
                if (index !== -1) {
                    chatMessageBatch.getUsersWithActivity().splice(index, 1);
                }


                this.chatMessageFirebaseDao.updateChatMessageBatchTransactional(transaction, chatMessageBatch);
            });

        });

    }
}

