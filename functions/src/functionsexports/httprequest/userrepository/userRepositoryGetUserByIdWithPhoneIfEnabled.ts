/* eslint-disable */




import {User} from "../../../models/user/User";

const functions = require("firebase-functions");
import {UserRepository} from "../../../repository/UserRepository";
import {ElementNotFoundException} from "../../../exceptions/ElementNotFoundException";
import {ValidationException} from "../../../exceptions/ValidationException";


const userRepository:UserRepository = new UserRepository();


export const userRepositoryGetUserByIdWithPhoneIfEnabled = functions.region('europe-west1').https.onCall(async (data: any, context: any) => {


    try {

        if (!context.auth)
            throw new ValidationException('You must be authenticated to call this function.');


        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(data);
        }catch (Error){
            parsedSnapshot = data;
        }


        const id:string = parsedSnapshot.id;
        const chatId:string = parsedSnapshot.chatId;


        const userWithPhone:User | null = await userRepository.getUserByIdWithPhoneIfEnabled(id, context.auth.uid, chatId);
        if (userWithPhone === null)
            return null;

        return userWithPhone.toObject();

    } catch (error) {

        if (error instanceof ValidationException)
            throw new functions.https.HttpsError('invalid-argument', error.message);


        if (error instanceof ElementNotFoundException){
            throw new functions.https.HttpsError('not-found',  error.message);
        }

        console.error("An error occurred:", error);
        throw new functions.https.HttpsError('internal', 'An internal error occurred.');
    }
});

