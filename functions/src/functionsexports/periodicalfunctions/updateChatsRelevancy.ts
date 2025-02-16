/* eslint-disable */

import {ChatRepository} from "../../repository/ChatRepository";

const functions = require("firebase-functions");
import { logger } from "firebase-functions";
import { WriteResult } from "firebase-admin/firestore";
import {Chat} from "../../models/chat/Chat";

const chatRepository:ChatRepository = new ChatRepository();

export const updateChatsRelevancy = functions.region('europe-west1').pubsub.schedule('*/15 * * * *')
    .timeZone('UTC')
    .onRun(async (context: any) => {


    try{
       // Get a document from Firestore
        const timeNowInEpochMilli:number = Date.now();

        const chats:any[] = await chatRepository.getChatThatShouldChangeToIrrelevantPromise(timeNowInEpochMilli);
        chats.forEach(chat => {

            chat.chatMatchIsRelevant = false;
        })

        if (chats.length == 0){
            return null;
        }
        logger.info("chats",chats);

        const promisesToSaveChats:Promise<WriteResult>[] = updateChats(chats);

        await Promise.allSettled(promisesToSaveChats).then((results) => {
            results.forEach((result, index) => {
                if(result.status === 'fulfilled') {
                    console.log(`Promise ${index} fulfilled with value: ${result.value}`);
                } else {
                    console.error(`Promise ${index} rejected with reason: ${result.reason}`);
                }
            });
        });

        return null;
    } catch (error) {
        console.error('Error fetching or processing data:', error);
        return null;
    };

});

function updateChats(chats: Chat[]): Promise<WriteResult> [] {
    const promises: Promise<WriteResult> [] = [];

    chats.forEach((chat: Chat) => {
        logger.info("chat",chat);
        const promise: Promise<WriteResult> = chatRepository.updateChatOnlyForInsideCalls(chat);

        promises.push(promise);
    });

    logger.info("promises",promises);
    return promises;
}

