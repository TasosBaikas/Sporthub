/* eslint-disable */


import {ValidationException} from "../../../../exceptions/ValidationException";

const functions = require("firebase-functions");
import {ElementNotFoundException} from "../../../../exceptions/ElementNotFoundException";
import {ChatRepository} from "../../../../repository/ChatRepository";


const chatRepository:ChatRepository = new ChatRepository();


export const chatRepositoryDestroyChatIfMatchConversation = functions.region('europe-west1').https.onCall(async (data: any, context: any) => {

    try {

        // Check if the user is authenticated
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

        await chatRepository.destroyChatIfMatchConversation(yourId,chatId);

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

