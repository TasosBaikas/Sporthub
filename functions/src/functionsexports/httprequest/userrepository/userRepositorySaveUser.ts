/* eslint-disable */


import {User} from "../../../models/user/User";

const functions = require("firebase-functions");
import {UserRepository} from "../../../repository/UserRepository";
import {ElementNotFoundException} from "../../../exceptions/ElementNotFoundException";
import {UserNotification} from "../../../models/user/usernotification/UserNotification";
import {UserNotificationsRepository} from "../../../repository/UserNotificationsRepository";
import {logger} from "firebase-functions/v1";
import {ValidationException} from "../../../exceptions/ValidationException";


const userRepository:UserRepository = new UserRepository();
const userNotificationRepository:UserNotificationsRepository = new UserNotificationsRepository();


export const userRepositorySaveUser = functions.region('europe-west1').https.onCall(async (data: any, context: any) => {


    try {

        if (!context.auth)
            throw new ValidationException('You must be authenticated to call this function.');


        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(data);
        }catch (Error){
            parsedSnapshot = data;
        }

        logger.log("parsedSnapshot",parsedSnapshot);

        const user:User = User.makeInstanceFromSnapshot(parsedSnapshot.user);
        if (context.auth.uid !== user.getUserId())
            throw new ValidationException('You must be authenticated to call this function.');

        logger.log("user",parsedSnapshot.user);
        logger.log("userNotifications",parsedSnapshot.userNotifications);


        // const userRecord = await admin.auth().getUser(context.auth.uid);
        //
        // const phoneNumber: string | undefined = userRecord.phoneNumber;
        // if (phoneNumber == undefined)
        //     return false;



        const userNotification:UserNotification = UserNotification.makeInstanceFromSnapshot(parsedSnapshot.userNotifications);
        await userNotificationRepository.saveUserNotificationsPromise(userNotification);


        await userRepository.saveUserPromise(user);

        return true;

    } catch (error) {

        if (error instanceof ValidationException)
            throw new functions.https.HttpsError('invalid-argument', error.message);


        if (error instanceof ElementNotFoundException){
            throw new functions.https.HttpsError('not-found', "Error fetching data");
        }

        console.error("An error occurred:", error);
        throw new functions.https.HttpsError('internal', 'An internal error occurred.');
    }
});

