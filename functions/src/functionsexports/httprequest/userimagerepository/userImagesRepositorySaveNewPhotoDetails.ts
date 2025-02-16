/* eslint-disable */


import {UserImages} from "../../../models/user/UserImages";

const functions = require("firebase-functions");
import {ElementNotFoundException} from "../../../exceptions/ElementNotFoundException";
import {UserImageRepository} from "../../../repository/UserImageRepository";
import {PhotoDetails} from "../../../models/PhotoDetails";
import {ValidationException} from "../../../exceptions/ValidationException";


let userImageRepository:UserImageRepository;


export const userImagesRepositorySaveNewPhotoDetails = functions.region('europe-west1').https.onCall(async (data: any, context: any) => {


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


        const userImages:UserImages = await userImageRepository.getUserImagePromise(yourId);
        if (userImages.getUserId() !==  context.auth.uid)
            throw new ValidationException('You must be authenticated to call this function.');

        if (userImages.getPhotoDetails().length + 1 > UserImages.MAX_IMAGES_SIZE)
            throw new ValidationException("Δεν μπορείτε να ανεβάσετε άλλες φωτογραφίες");


        const newPhotoDetails:PhotoDetails = PhotoDetails.makeInstanceFromSnapshot(parsedSnapshot.photoDetails);

        userImages.getPhotoDetails()
            .forEach((photoDetails:PhotoDetails) => photoDetails.setPriority(photoDetails.getPriority() + 1));

        userImages.getPhotoDetails().unshift(newPhotoDetails);

        await userImageRepository.saveUserImageInstance(userImages);

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

