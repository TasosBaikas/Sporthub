/* eslint-disable */


import {ValidationException} from "../../../../exceptions/ValidationException";

const functions = require("firebase-functions");
import {ElementNotFoundException} from "../../../../exceptions/ElementNotFoundException";
import {ChatRepository} from "../../../../repository/ChatRepository";
import {ChatMessageRepository} from "../../../../repository/ChatMessageRepository";
import {UserRepository} from "../../../../repository/UserRepository";
import {User} from "../../../../models/user/User";
import {ChatMessage} from "../../../../models/chat/ChatMessage";
import {v4 as uuidv4} from "uuid";
import {UserShortForm} from "../../../../models/user/UserShortForm";
import {MessageStatus} from "../../../../models/constants/MessageStatus";


const chatRepository:ChatRepository = new ChatRepository();
const chatMessageRepository:ChatMessageRepository = new ChatMessageRepository();
const userRepository:UserRepository = new UserRepository();


export const chatRepositoryKickUserIfMatchConversation = functions.region('europe-west1').https.onCall(async (data: any, context: any) => {

    try {

        if (!context.auth)
            throw new ValidationException('You must be authenticated to call this function.');

        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(data);
        }catch (Error){
            parsedSnapshot = data;
        }

        const userToKick:string = parsedSnapshot.userToKick;
        if (userToKick === context.auth.uid)
            throw new ValidationException('Δεν μπορείτε να διώξετε τον ευατό σας');

        const chatId:string = parsedSnapshot.chatId;

        await chatRepository.kickUserIfMatchConversation(userToKick, context.auth.uid, chatId);


        const user:User|null = await userRepository.getUserByIdPromise(userToKick,context.auth.uid,false);
        if (user === null)
            throw new ValidationException("Ο χρήστης δεν υπάρχει");


        const createdAtUTC:number = Date.now();

        const messageId:string = uuidv4();

        const userShortForm:UserShortForm  = UserShortForm.makeShortForFromUser(user,"");

        const chatMessageUserLeft:ChatMessage = ChatMessage.UserLeftChatMessageInstance(messageId, "", chatId, userShortForm,"", MessageStatus.SENT,
            createdAtUTC, 0);

        await chatMessageRepository.saveChatMessage(chatMessageUserLeft, false);

        return true;

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

