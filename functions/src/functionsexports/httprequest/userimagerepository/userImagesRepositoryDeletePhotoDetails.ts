/* eslint-disable */


import {PhotoDetails} from "../../../models/PhotoDetails";

const functions = require("firebase-functions");
import {ElementNotFoundException} from "../../../exceptions/ElementNotFoundException";
import {UserImageRepository} from "../../../repository/UserImageRepository";
import {UserImages} from "../../../models/user/UserImages";
import {ValidationException} from "../../../exceptions/ValidationException";


let userImageRepository:UserImageRepository;


export const userImagesRepositoryDeletePhotoDetails = functions.region('europe-west1').https.onCall(async (data: any, context: any) => {

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



        const photoDetailsToDelete:PhotoDetails = PhotoDetails.makeInstanceFromSnapshot(parsedSnapshot.photoDetails);


        userImages.getPhotoDetails()
            .filter((photoDetails:PhotoDetails) => photoDetails.getPriority() > photoDetailsToDelete.getPriority())
            .forEach((photoDetails:PhotoDetails) => photoDetails.setPriority(photoDetails.getPriority() - 1));

        const filteredPhotoDetails:PhotoDetails[] = userImages.getPhotoDetails().filter((photoDetail:PhotoDetails) => photoDetail.getFilePath() !== photoDetailsToDelete.getFilePath());
        userImages.setPhotoDetails(filteredPhotoDetails);

        await userImageRepository.saveUserImageInstance(userImages);

        await userImageRepository.deleteImageFromStorageProfile(photoDetailsToDelete.getFilePath());

        return true;

    } catch (error) {

        if (error instanceof ValidationException)
            throw new functions.https.HttpsError('invalid-argument', error.message);

        if (error instanceof ElementNotFoundException)
            throw new functions.https.HttpsError('not-found', "Error fetching data");

        throw new functions.https.HttpsError('internal', 'An internal error occurred.');
    }
});

