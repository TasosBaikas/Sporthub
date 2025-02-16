/* eslint-disable */


import {ValidationException} from "../../../exceptions/ValidationException";

const functions = require("firebase-functions");
import {ElementNotFoundException} from "../../../exceptions/ElementNotFoundException";
import {logger} from "firebase-functions";
import {UserImageRepository} from "../../../repository/UserImageRepository";
import {SizeTooBigException} from "../../../exceptions/SizeTooBigException";
import {UserImages} from "../../../models/user/UserImages";


let userImageRepository:UserImageRepository;


export const userImagesRepositorySaveImageToStorage = functions.region('europe-west1').https.onCall(async (data: any, context: any) => {


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
        if (yourId !==  context.auth.uid)
            throw new ValidationException('You must be authenticated to call this function.');

        userImageRepository = new UserImageRepository(yourId,context.auth.uid);


        const imageInBase64: Buffer = Buffer.from(parsedSnapshot.image, 'base64');

        logger.log(imageInBase64.length);
        if (imageInBase64.length > UserImages.MAX_IMAGES_MB)
            throw new SizeTooBigException('The image is larger than 2mb');


        const reference:string = parsedSnapshot.reference;
        const yourIdFromReference: string = reference.substring(0,reference.indexOf("_"));
        if (yourIdFromReference !== yourId)
            throw new ValidationException('You must be authenticated to call this function.');


        const userImages:UserImages = await userImageRepository.getUserImagePromise(yourId);
        if (userImages.getPhotoDetails().length + 10 > UserImages.MAX_IMAGES_SIZE)
            throw new ValidationException("Δεν μπορείτε να ανεβάσετε άλλες φωτογραφίες");


        await userImageRepository.saveImageToStoragePromise(reference, imageInBase64);

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

