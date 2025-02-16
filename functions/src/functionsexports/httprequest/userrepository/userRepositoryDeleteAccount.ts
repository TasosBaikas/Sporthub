/* eslint-disable */




import {UserPrivacyRepository} from "../../../repository/UserPrivacyRepository";

const functions = require("firebase-functions");
import * as admin from "firebase-admin";

import {ElementNotFoundException} from "../../../exceptions/ElementNotFoundException";
import {ValidationException} from "../../../exceptions/ValidationException";
import {UserChatAction} from "../../../models/userprivacy/UserChatAction";
import {ChatRepository} from "../../../repository/ChatRepository";
import {ChatMessageRepository} from "../../../repository/ChatMessageRepository";
import {UserNotificationsRepository} from "../../../repository/UserNotificationsRepository";
import {TerrainAddressRepository} from "../../../repository/TerrainAddressRepository";
import {UserRepository} from "../../../repository/UserRepository";
import {User} from "../../../models/user/User";


const userRepository:UserRepository = new UserRepository();
const userPrivacyRepository:UserPrivacyRepository = new UserPrivacyRepository();
const chatRepository:ChatRepository = new ChatRepository();
const chatMessageRepository:ChatMessageRepository = new ChatMessageRepository();
const userNotificationsRepository:UserNotificationsRepository = new UserNotificationsRepository();
const terrainAddressRepository:TerrainAddressRepository = new TerrainAddressRepository();


export const userRepositoryDeleteAccount = functions.region('europe-west1').https.onCall(async (data: any, context: any) => {


    try {

        if (!context.auth)
            throw new ValidationException('You must be authenticated to call this function.');


        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(data);
        }catch (Error){
            parsedSnapshot = data;
        }

        const id:string = parsedSnapshot.id;
        if (id !== context.auth.uid)
            throw new ValidationException('You must be authenticated to call this function.');

        //user images is not implemented in deletion

        const userChatAction:UserChatAction | null = await userPrivacyRepository.getUserActivityToAChat(id);

        let promises: Promise<any>[] = [];

        let chatsIds: string[] = userChatAction.getChatsIds();

        for (const chatId of chatsIds) {

            promises.push(chatRepository.destroyChatIfAdminElseLeave_AccountDeletion(id, chatId));
        }

        await Promise.allSettled(promises);

        console.log("endedFirstPromise");


        promises = [];
        for (const chatId of chatsIds) {

            promises.push(chatMessageRepository.deleteChatMessagesOrSpoofData_AccountDeletion(id, chatId));
        }


        await Promise.allSettled(promises);
        console.log("ended2ndPromise");



        promises = [];

        const user: User | null = await userRepository.getUserByIdPromise(id,id,false);
        if (user != null)
            promises.push(userRepository.deleteUserProfileImagePromise(user.getProfileImageUrl()));


        promises.push(userPrivacyRepository.deleteUserActivityToAChat(id));
        promises.push(userPrivacyRepository.deletePhoneNumberPermissions(id));

        promises.push(terrainAddressRepository.deleteAllTerrainsForThatUser(id));

        promises.push(userNotificationsRepository.deleteUserNotifications(id));


        promises.push(userRepository.deleteUserById(id));

        await Promise.allSettled(promises);

        console.log("ended3rdPromise");

        await admin.auth().deleteUser(id);


        console.log("ended4thPromise");

        return;

    } catch (error) {

        if (error instanceof ValidationException)
            throw new functions.https.HttpsError('invalid-argument', error.message);


        if (error instanceof ElementNotFoundException){
            throw new functions.https.HttpsError('not-found',  error.message);
        }

        console.error("An error occurred:", error);
        throw new functions.https.HttpsError('internal', 'An internal error occurred.');
    }
});

