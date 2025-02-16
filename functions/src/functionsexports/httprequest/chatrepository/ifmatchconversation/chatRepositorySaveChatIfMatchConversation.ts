/* eslint-disable */


import {ValidationException} from "../../../../exceptions/ValidationException";

const functions = require("firebase-functions");
import {ElementNotFoundException} from "../../../../exceptions/ElementNotFoundException";
import {ChatRepository} from "../../../../repository/ChatRepository";
import {Chat} from "../../../../models/chat/Chat";


const chatRepository:ChatRepository = new ChatRepository();


export const chatRepositorySaveChatIfMatchConversation = functions.region('europe-west1').https.onCall(async (data: any, context: any) => {


    try {

        if (!context.auth)
            throw new ValidationException('You must be authenticated to call this function.');


        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(data);
        }catch (Error){
            parsedSnapshot = data;
        }

        const chat:Chat = Chat.makeInstanceFromSnapshot(parsedSnapshot.chat);
        if (chat.isPrivateConversation())
            throw new ValidationException('It is not a match conversation');

        if (!chat.isAdmin(context.auth.uid))
            throw new ValidationException('you are not the admin');

        if (chat.getMembersIds().length >= 2)
            throw new ValidationException('you must be the only 1 in the chat');


        await chatRepository.saveChatIfMatchConversation(chat,context.auth.uid);

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

