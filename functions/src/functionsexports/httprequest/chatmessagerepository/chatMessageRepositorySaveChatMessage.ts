/* eslint-disable */



import {ValidationException} from "../../../exceptions/ValidationException";

const functions = require("firebase-functions");
// import {ElementNotFoundException} from "../../../exceptions/ElementNotFoundException";
import {ChatMessageRepository} from "../../../repository/ChatMessageRepository";
import {ChatMessage} from "../../../models/chat/ChatMessage";
import {ElementNotFoundException} from "../../../exceptions/ElementNotFoundException";


const chatMessageRepository:ChatMessageRepository = new ChatMessageRepository();


export const chatMessageRepositorySaveChatMessage = functions.region('europe-west1').https.onCall(async (data: any, context: any) => {

    try {

        if (!context.auth)
            throw new ValidationException('You must be authenticated to call this function.');


        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(data);
        }catch (Error){
            parsedSnapshot = data;
        }

        const chatMessage:ChatMessage = ChatMessage.makeInstanceFromSnapshot(parsedSnapshot.chatMessage);
        if (chatMessage.getUserShortForm().getUserId() !== context.auth.uid)
            throw new ValidationException('You must be authenticated to call this function.');


        await chatMessageRepository.saveChatMessage(chatMessage, true);

        console.log("chatMessageRepositorySaveChatMessage ENDED");
        await chatMessageRepository.changeSeenByUser(chatMessage, chatMessage.getUserShortForm().getUserId());

        return true;

    } catch (error) {

        if (error instanceof ValidationException)
            throw new functions.https.HttpsError('invalid-argument', error.message);

        if (error instanceof ElementNotFoundException)
            throw new functions.https.HttpsError('not-found', "Error fetching data");


        throw new functions.https.HttpsError('internal', 'An internal error occurred.');
    }
});


