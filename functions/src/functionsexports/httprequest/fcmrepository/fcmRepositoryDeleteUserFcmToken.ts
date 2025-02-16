/* eslint-disable */



import {ValidationException} from "../../../exceptions/ValidationException";

const functions = require("firebase-functions");
import {ElementNotFoundException} from "../../../exceptions/ElementNotFoundException";
import {UserFcmRepository} from "../../../repository/UserFcmRepository";


const userFcmRepository:UserFcmRepository = new UserFcmRepository();


export const fcmRepositoryDeleteUserFcmToken = functions.region('europe-west1').https.onCall(async (data: any, context: any) => {

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


        const deviceUUID:string = parsedSnapshot.deviceUUID;

        await userFcmRepository.deleteUserFcmToken(yourId,deviceUUID);

        return true;

    } catch (error) {

        if (error instanceof ValidationException)
            throw new functions.https.HttpsError('invalid-argument', error.message);

        if (error instanceof ElementNotFoundException)
            throw new functions.https.HttpsError('not-found', "Error fetching data");


        throw new functions.https.HttpsError('internal', 'An internal error occurred.');
    }
});

