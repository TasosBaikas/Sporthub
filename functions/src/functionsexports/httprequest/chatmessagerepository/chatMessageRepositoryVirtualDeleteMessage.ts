/* eslint-disable */



import {ValidationException} from "../../../exceptions/ValidationException";

const functions = require("firebase-functions");
// import {ElementNotFoundException} from "../../../exceptions/ElementNotFoundException";
import {ChatMessageRepository} from "../../../repository/ChatMessageRepository";
import {ChatMessage} from "../../../models/chat/ChatMessage";
import {DoNothingException} from "../../../exceptions/DoNothingException";
import {ElementNotFoundException} from "../../../exceptions/ElementNotFoundException";
// import {DoNothingException} from "../../../exceptions/DoNothingException";


const chatMessageRepository:ChatMessageRepository = new ChatMessageRepository();


export const chatMessageRepositoryVirtualDeleteMessage = functions.region('europe-west1').https.onCall(async (data: any, context: any) => {

    try {

        if (!context.auth)
            throw new ValidationException('You must be authenticated to call this function.');


        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(data);
        }catch (Error){
            parsedSnapshot = data;
        }

        console.log("inside virtual delete",1);

        const chatMessage:ChatMessage = ChatMessage.makeInstanceFromSnapshot(parsedSnapshot.chatMessage);

        console.log("inside virtual delete",2);

        const yourId:string = parsedSnapshot.yourId;
        if (yourId !== context.auth.uid)
            throw new ValidationException('You must be authenticated to call this function.');

        if (chatMessage.getUserShortForm().getUserId() !== yourId)
            throw new ValidationException('You cant delete another one message.');

        console.log("inside virtual delete",3);

        const chatMessageVirtualDeleted:ChatMessage = await chatMessageRepository.virtualDeleteMessage(chatMessage,yourId);


        return chatMessageVirtualDeleted.toObject();

    } catch (error) {

        if (error instanceof DoNothingException)
            throw new functions.https.HttpsError('invalid-argument', "");

        if (error instanceof ValidationException)
            throw new functions.https.HttpsError('invalid-argument', error.message);

        if (error instanceof ElementNotFoundException)
            throw new functions.https.HttpsError('not-found', "Error fetching data");


        throw new functions.https.HttpsError('internal', 'An internal error occurred.');
    }
});

