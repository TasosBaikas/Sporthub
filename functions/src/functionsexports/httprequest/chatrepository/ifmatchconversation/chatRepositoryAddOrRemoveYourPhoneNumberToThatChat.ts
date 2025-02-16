/* eslint-disable */



import {ValidationException} from "../../../../exceptions/ValidationException";
import {ElementNotFoundException} from "../../../../exceptions/ElementNotFoundException";
import {ChatRepository} from "../../../../repository/ChatRepository";
import {ChatMessageRepository} from "../../../../repository/ChatMessageRepository";
import {ChatMessage} from "../../../../models/chat/ChatMessage";
import {User} from "../../../../models/user/User";
import {UserRepository} from "../../../../repository/UserRepository";
import {UserShortForm} from "../../../../models/user/UserShortForm";

const functions = require("firebase-functions");


const chatRepository:ChatRepository = new ChatRepository();
const chatMessageRepository:ChatMessageRepository = new ChatMessageRepository();
const userRepository:UserRepository = new UserRepository();


export const chatRepositoryAddOrRemoveYourPhoneNumberToThatChat = functions.region('europe-west1').https.onCall(async (data: any, context: any) => {


    try {

        if (!context.auth)
            throw new ValidationException('You must be authenticated to call this function.');


        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(data);
        }catch (Error){
            parsedSnapshot = data;
        }


        const yourId:string = parsedSnapshot.yourId;
        if (yourId !== context.auth.uid)
            throw new ValidationException('You must be authenticated to call this function.');



        const chatId:string = parsedSnapshot.chatId;
        const addOrRemove:boolean = parsedSnapshot.addOrRemove;

        const result:boolean = await chatRepository.addOrRemoveYourPhoneNumberToThatChat(yourId, chatId, addOrRemove);

        const user:User | null = await userRepository.getUserByIdPromise(yourId,"",false);
        if (user === null)
            return null;

        const userShortForm:UserShortForm = UserShortForm.makeShortForFromUser(user, null);

        let chatMessage:ChatMessage;
        if (addOrRemove){
            chatMessage = ChatMessage.makeInstanceEnablePhoneNumber("", chatId, userShortForm);
        }else{
            chatMessage = ChatMessage.makeInstanceDisablePhoneNumber("", chatId, userShortForm);
        }

        await chatMessageRepository.saveChatMessage(chatMessage, true);

        return result;

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

