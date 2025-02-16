/* eslint-disable */



import {ValidationException} from "../../exceptions/ValidationException";
import {ElementNotFoundException} from "../../exceptions/ElementNotFoundException";

const functions = require("firebase-functions");


export const getServerTime = functions.region('europe-west1').https.onCall(async (data: any, context: any) => {

    // Check if the user is authenticated
    try{
        if (!context.auth)
            throw new ValidationException('You must be authenticated to call this function.');


        const serverTimeUTC = Date.now(); // This will return current time in milliseconds (epoch)
        return { serverTime: serverTimeUTC };
    } catch (error) {

        if (error instanceof ValidationException)
            throw new functions.https.HttpsError('invalid-argument', error.message);


        if (error instanceof ElementNotFoundException){
            throw new functions.https.HttpsError('not-found', "Error fetching data");
        }

        console.error("An error occurred:", error);
        throw new functions.https.HttpsError('internal', 'An internal error occurred.');
    }


})

