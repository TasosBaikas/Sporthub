/* eslint-disable */



import {ValidationException} from "../../../exceptions/ValidationException";

const functions = require("firebase-functions");
import {UserFcmRepository} from "../../../repository/UserFcmRepository";
import {UserFcm} from "../../../models/user/UserFcm";
import {ElementNotFoundException} from "../../../exceptions/ElementNotFoundException";


const userFcmRepository:UserFcmRepository = new UserFcmRepository();


export const fcmRepositorySaveUserFcmToken = functions.region('europe-west1').https.onCall(async (data: any, context: any) => {

    try {

        if (!context.auth)
            throw new ValidationException('You must be authenticated to call this function.');


        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(data);
        }catch (Error){
            parsedSnapshot = data;
        }

        const userFcm:UserFcm = UserFcm.makeInstanceFromSnapshot(parsedSnapshot.userFcm);

        if (userFcm.getUserId() !== context.auth.uid)
            throw new ValidationException('You must be authenticated to call this function.');


        await userFcmRepository.saveUserFcmToken(userFcm);

        return true;

    } catch (error) {

        console.error("error", error);
        if (error instanceof ValidationException)
            throw new functions.https.HttpsError('invalid-argument', error.message);

        if (error instanceof ElementNotFoundException)
            throw new functions.https.HttpsError('not-found', "Error fetching data");


        throw new functions.https.HttpsError('internal', 'An internal error occurred.');
    }
});

