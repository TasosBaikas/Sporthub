/* eslint-disable */


import {ValidationException} from "../../../exceptions/ValidationException";

const functions = require("firebase-functions");
import {ElementNotFoundException} from "../../../exceptions/ElementNotFoundException";
import {UserImageRepository} from "../../../repository/UserImageRepository";


let userImageRepository:UserImageRepository;


export const userImagesRepositoryDeleteImageFromStorage = functions.region('europe-west1').https.onCall(async (data: any, context: any) => {

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


        const previousReference:string = parsedSnapshot.previousFilePath;
        const yourIdFromReference: string = previousReference.substring(0,previousReference.indexOf("_"));
        if (yourIdFromReference !== yourId)
            throw new ValidationException('You must be authenticated to call this function.');


        await userImageRepository.deleteImageFromStorageProfile(previousReference);

        return true;

    } catch (error) {

        console.error("error", error);
        if (error instanceof ValidationException){
            console.error("if 1");
            throw new functions.https.HttpsError('invalid-argument', error.message);
        }

        if (error instanceof ElementNotFoundException){
            throw new functions.https.HttpsError('not-found', "Error fetching data");
        }

        console.error("An error occurred:", error);
        throw new functions.https.HttpsError('internal', 'An internal error occurred.');
    }
});

