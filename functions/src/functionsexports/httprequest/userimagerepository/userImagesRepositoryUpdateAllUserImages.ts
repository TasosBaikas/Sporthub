/* eslint-disable */


import {UserImages} from "../../../models/user/UserImages";

const functions = require("firebase-functions");
import {ElementNotFoundException} from "../../../exceptions/ElementNotFoundException";
import {UserImageRepository} from "../../../repository/UserImageRepository";
import {ValidationException} from "../../../exceptions/ValidationException";


let userImageRepository:UserImageRepository;


export const userImagesRepositoryUpdateAllUserImages = functions.region('europe-west1').https.onCall(async (data: any, context: any) => {


    // Check if the user is authenticated
    if (!context.auth)
        throw new ValidationException('You must be authenticated to call this function.');

    try {

        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(data);
        }catch (Error){
            parsedSnapshot = data;
        }

        userImageRepository = new UserImageRepository(context.auth.uid, context.auth.uid);


        const userImagesToUpdate:UserImages = UserImages.makeInstanceFromSnapshot(parsedSnapshot.userImages);
        if (userImagesToUpdate.getPhotoDetails().length  > UserImages.MAX_IMAGES_SIZE + 10)//we add 10 if for some reason the images are more than max
            throw new ValidationException("Έχετε πολλές φωτογραφίες. Πρέπει να διαγράψετε κάποιες");

        await userImageRepository.saveUserImageInstance(userImagesToUpdate);

        return true;

    } catch (error) {

        if (error instanceof ValidationException)
            throw new functions.https.HttpsError('invalid-argument', error.message);


        if (error instanceof ElementNotFoundException)
            throw new functions.https.HttpsError('not-found', error.message);

        throw new functions.https.HttpsError('internal', 'An internal error occurred.');
    }
});

