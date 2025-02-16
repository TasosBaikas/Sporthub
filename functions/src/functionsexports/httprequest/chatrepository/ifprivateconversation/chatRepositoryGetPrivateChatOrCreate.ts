/* eslint-disable */


import {ValidationException} from "../../../../exceptions/ValidationException";

const functions = require("firebase-functions");
import {ChatRepository} from "../../../../repository/ChatRepository";
import {Chat} from "../../../../models/chat/Chat";
import {ElementNotFoundException} from "../../../../exceptions/ElementNotFoundException";
import {UserShortForm} from "../../../../models/user/UserShortForm";
import {UserRepository} from "../../../../repository/UserRepository";
import {User} from "../../../../models/user/User";
import {v4 as uuidv4} from "uuid";
import {ChatMessage} from "../../../../models/chat/ChatMessage";
import {ChatTypes} from "../../../../models/constants/ChatTypes";


const chatRepository:ChatRepository = new ChatRepository();
const userRepository:UserRepository = new UserRepository();


export const chatRepositoryGetPrivateChatOrCreate = functions.region('europe-west1').https.onCall(async (data: any, context: any) => {



    try {

        if (!context.auth)
            throw new ValidationException('You must be authenticated to call this function.');


        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(data);
        }catch (Error){
            parsedSnapshot = data;
        }


        const user1Id:string = parsedSnapshot.user1Id;
        const user2Id:string = parsedSnapshot.user2Id;

        if (user1Id !== context.auth.uid && user2Id !== context.auth.uid)
            throw new ValidationException('You must be authenticated to call this function.');


        const chat:Chat | null = await chatRepository.getPrivateChatFromThe2Users(user1Id, user2Id);
        if (chat !== null)
            return chat;

        console.log(chat);

        const member1:User | null = await userRepository.getUserByIdPromise(user1Id,"",false);
        const member2:User | null = await userRepository.getUserByIdPromise(user2Id,"",false);



        if (member1 == null || member2 == null)
            throw new ValidationException("Ενας απο τους δύο χρήστες δεν υπάρχει");

        const member1ShortForm:UserShortForm = UserShortForm.makeShortForFromUser(member1, null);
        const member2ShortForm:UserShortForm = UserShortForm.makeShortForFromUser(member2, null);

        console.log(member1ShortForm);
        console.log(member2ShortForm);

        const chatId:string = ChatTypes.PRIVATE_CONVERSATION + "_" + uuidv4();


        const chatMessageBatchId: string = uuidv4();

        const firstChatMessage: ChatMessage = ChatMessage.chatFirstMessageInstance(chatMessageBatchId, chatId, member1ShortForm);


        const privateConversation:Chat = Chat.makeChatPrivateConversation(chatId, firstChatMessage, [], [member1ShortForm, member2ShortForm]);

        const chatAfterTransaction: Chat = await chatRepository.saveChatIfPrivateConversation(privateConversation, context.auth.uid);

        return chatAfterTransaction;

    } catch (error) {

        console.error("error", error);
        if (error instanceof ValidationException){
            console.error("if 1");
            throw new functions.https.HttpsError('invalid-argument', error.message);
        }

        if (error instanceof ElementNotFoundException){
            console.error("if 2");
            throw new functions.https.HttpsError('not-found', "Error fetching data");
        }

        console.error("An error occurred:", error);
        throw new functions.https.HttpsError('internal', 'An internal error occurred.');
    }
});

