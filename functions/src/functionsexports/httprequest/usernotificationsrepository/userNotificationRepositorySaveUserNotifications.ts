/* eslint-disable */


import {UserNotification} from "../../../models/user/usernotification/UserNotification";

const functions = require("firebase-functions");
import {UserNotificationsRepository} from "../../../repository/UserNotificationsRepository";
import {ValidationException} from "../../../exceptions/ValidationException";
import {ElementNotFoundException} from "../../../exceptions/ElementNotFoundException";


const userNotificationsRepository:UserNotificationsRepository = new UserNotificationsRepository();


export const userNotificationRepositorySaveUserNotifications = functions.region('europe-west1').https.onCall(async (data: any, context: any) => {


    try {

        if (!context.auth)
            throw new ValidationException('You must be authenticated to call this function.');


        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(data);
        }catch (Error){
            parsedSnapshot = data;
        }

        const userNotification:UserNotification = UserNotification.makeInstanceFromSnapshot(parsedSnapshot.userNotifications);

        if (userNotification.getUserId() !==  context.auth.uid)
            throw new ValidationException('You must be authenticated to call this function.');

        await userNotificationsRepository.saveUserNotificationsPromise(userNotification);

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

