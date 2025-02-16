/* eslint-disable */


import {UserImages} from "../../../models/user/UserImages";

const functions = require("firebase-functions");
import {ElementNotFoundException} from "../../../exceptions/ElementNotFoundException";
import {UserImageRepository} from "../../../repository/UserImageRepository";
import {PhotoDetails} from "../../../models/PhotoDetails";
import {ValidationException} from "../../../exceptions/ValidationException";


let userImageRepository:UserImageRepository;


export const userImagesRepositoryUpdatePhotoDetails = functions.region('europe-west1').https.onCall(async (data: any, context: any) => {


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

        const yourId:string = parsedSnapshot.yourId;
        if (yourId !==  context.auth.uid)
            throw new ValidationException('You must be authenticated to call this function.');

        userImageRepository = new UserImageRepository(yourId,context.auth.uid);


        const userImages:UserImages = await userImageRepository.getUserImagePromise(yourId);
        if (userImages.getUserId() !==  context.auth.uid)
            throw new ValidationException('You must be authenticated to call this function.');


        const previousFilePath:string = parsedSnapshot.previousFilePath;

        const updatedPhotoDetails:PhotoDetails = PhotoDetails.makeInstanceFromSnapshot(parsedSnapshot.photoDetails);


        const indexToRemove: number = userImages.getPhotoDetails().findIndex(photoDetail => photoDetail.getPriority() === updatedPhotoDetails.getPriority());

        if (indexToRemove !== -1) {
            userImages.getPhotoDetails().splice(indexToRemove, 1);
        }

        userImages.getPhotoDetails().push(updatedPhotoDetails);
        if (userImages.getPhotoDetails().length  > UserImages.MAX_IMAGES_SIZE + 10)//we add 10 if for some reason the images are more than max
            throw new ValidationException("Έχετε πολλές φωτογραφίες. Πρέπει να διαγράψετε κάποιες");


        await userImageRepository.saveUserImageInstance(userImages);

        await userImageRepository.deleteImageFromStorageProfile(previousFilePath);

        return true;

    } catch (error) {

        if (error instanceof ValidationException)
            throw new functions.https.HttpsError('invalid-argument', error.message);


        if (error instanceof ElementNotFoundException)
            throw new functions.https.HttpsError('not-found', error.message);

        throw new functions.https.HttpsError('internal', 'An internal error occurred.');
    }
});

