/* eslint-disable */


import {ValidationException} from "../../../exceptions/ValidationException";

const functions = require("firebase-functions");
import {UserRepository} from "../../../repository/UserRepository";
import {ElementNotFoundException} from "../../../exceptions/ElementNotFoundException";


const userRepository:UserRepository = new UserRepository();


export const userRepositoryDeleteProfileImageByImagePath = functions.region('europe-west1').https.onCall(async (data: any, context: any) => {


    try {

        if (!context.auth)
            throw new ValidationException('You must be authenticated to call this function.');


        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(data);
        }catch (Error){
            parsedSnapshot = data;
        }

        const imagePath:string = parsedSnapshot.imagePath;

        if (!imagePath.includes(context.auth.uid))
            throw new ValidationException('You must be authenticated to call this function.');

        await userRepository.deleteUserProfileImagePromise(imagePath);

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

