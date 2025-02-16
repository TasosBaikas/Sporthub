/* eslint-disable */


import {ValidationException} from "../../../exceptions/ValidationException";

const functions = require("firebase-functions");
import {ElementNotFoundException} from "../../../exceptions/ElementNotFoundException";
import {UserPrivacyRepository} from "../../../repository/UserPrivacyRepository";


const userPrivacyRepository:UserPrivacyRepository = new UserPrivacyRepository();

export const userPrivacyBlockPlayer = functions.region('europe-west1').https.onCall(async (data: any, context: any) => {


    try {
        console.log("started");
        if (!context.auth)
            throw new ValidationException('You must be authenticated to call this function.');

        console.log("passed verif");


        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(data);
        }catch (Error){
            parsedSnapshot = data;
        }

        const userIdToBlock:string = parsedSnapshot.userIdToBlock;

        if (userIdToBlock === context.auth.uid)
            throw new ValidationException("Δεν μπορείτε να μπλοκάρετε τον ευατό σας");


        console.log("userIdToBlock");

        await userPrivacyRepository.blockUser(userIdToBlock, context.auth.uid);

        return true;

    } catch (error) {

        console.error("error", error);
        if (error instanceof ValidationException){
            console.error("if 1");
            throw new functions.https.HttpsError('invalid-argument', error.message);
        }

        if (error instanceof ElementNotFoundException){
            console.error("if 2");
            throw new functions.https.HttpsError('not-found', "Error fetching data");
        }

        console.error("An error occurred:", error);
        throw new functions.https.HttpsError('internal', 'An internal error occurred.');
    }
});


