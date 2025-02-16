/* eslint-disable */


import {logger} from "firebase-functions/v1";

const functions = require("firebase-functions");
import {UserRepository} from "../../../repository/UserRepository";
import {ElementNotFoundException} from "../../../exceptions/ElementNotFoundException";
import {ValidationException} from "../../../exceptions/ValidationException";
import {SizeTooBigException} from "../../../exceptions/SizeTooBigException";


const userRepository:UserRepository = new UserRepository();


export const userRepositorySaveUserProfileImage = functions.region('europe-west1').https.onCall(async (data: any, context: any) => {

    try {

        if (!context.auth)
            throw new ValidationException('You must be authenticated to call this function.');


        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(data);
        }catch (Error){
            parsedSnapshot = data;
        }

        const reference:string = parsedSnapshot.reference;
        if (!reference.includes(context.auth.uid))
            throw new ValidationException('You must be authenticated to call this function.');


        const imageInBase64: Buffer = Buffer.from(parsedSnapshot.image, 'base64');

        logger.log(imageInBase64.length);
        if (imageInBase64.length > 2 * 1024 * 1024)
            throw new SizeTooBigException('The image is larger than 2mb');

        logger.log("uid",context.auth.uid);

        await userRepository.saveUserProfileImagePromise(reference,imageInBase64);

        return true;

    } catch (error) {

        if (error instanceof ValidationException)
            throw new functions.https.HttpsError('invalid-argument', error.message);


        if (error instanceof SizeTooBigException){
            throw new functions.https.HttpsError('invalid-argument', error.message);
        }

        if (error instanceof ElementNotFoundException){
            throw new functions.https.HttpsError('not-found', "Error fetching data");
        }

        console.error("An error occurred:", error);
        throw new functions.https.HttpsError('internal', 'An internal error occurred.');
    }
});

