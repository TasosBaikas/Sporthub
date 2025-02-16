/* eslint-disable */



import {ValidationException} from "../../../exceptions/ValidationException";

const functions = require("firebase-functions");
// import {ElementNotFoundException} from "../../../exceptions/ElementNotFoundException";
import {ChatMessageRepository} from "../../../repository/ChatMessageRepository";
import {ChatMessage} from "../../../models/chat/ChatMessage";
// import {DoNothingException} from "../../../exceptions/DoNothingException";
import {EmojisTypes} from "../../../models/constants/EmojisTypes";
import {DoNothingException} from "../../../exceptions/DoNothingException";
import {ElementNotFoundException} from "../../../exceptions/ElementNotFoundException";


const chatMessageRepository:ChatMessageRepository = new ChatMessageRepository();


export const chatMessageRepositoryUpdateEmojiCount = functions.region('europe-west1').https.onCall(async (data: any, context: any) => {

    try {
        console.log("hi")
        if (!context.auth)
            throw new ValidationException('You must be authenticated to call this function.');


        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(data);
        }catch (Error){
            parsedSnapshot = data;
        }
        console.log("hi2")

        const chatMessage:ChatMessage = ChatMessage.makeInstanceFromSnapshot(parsedSnapshot.chatMessage);

        const emojiClicked:string = parsedSnapshot.emojiClicked;
        if (!EmojisTypes.allPossibleEmojis.includes(emojiClicked))
            throw new ValidationException('That emoji does not exist.');

        const yourId:string = parsedSnapshot.yourId;
        if (yourId !== context.auth.uid)
            throw new ValidationException('You must be authenticated to call this function.');


        await chatMessageRepository.updateEmojiCount(chatMessage,emojiClicked,yourId);

        return true;

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



