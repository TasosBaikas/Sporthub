/* eslint-disable */



const functions = require("firebase-functions");
import {ValidationException} from "../../../exceptions/ValidationException";
import {ElementNotFoundException} from "../../../exceptions/ElementNotFoundException";
import {UserPrivacyRepository} from "../../../repository/UserPrivacyRepository";


const userPrivacyRepository:UserPrivacyRepository = new UserPrivacyRepository();


export const userPrivacyRepositoryRateUser = functions.region('europe-west1').https.onCall(async (data: any, context: any) => {


    try {

        if (!context.auth)
            throw new ValidationException('You must be authenticated to call this function.');


        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(data);
        }catch (Error){
            parsedSnapshot = data;
        }

        const userToRate:string = parsedSnapshot.userToRate;
        const rate:number = parsedSnapshot.rate;

        if (userToRate === context.auth.uid)
            throw new ValidationException("Δεν μπορείτε να αξιολογήσετε τον ευατό σας");

        await userPrivacyRepository.rateUser(userToRate, rate, context.auth.uid);

        return true;

    } catch (error) {

        console.error("An error occurred:", error);
        if (error instanceof ValidationException)
            throw new functions.https.HttpsError('invalid-argument', error.message);


        if (error instanceof ElementNotFoundException){
            throw new functions.https.HttpsError('not-found', "Error fetching data");
        }

        throw new functions.https.HttpsError('internal', 'An internal error occurred.');
    }
});

