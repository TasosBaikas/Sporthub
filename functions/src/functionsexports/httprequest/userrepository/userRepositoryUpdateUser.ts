/* eslint-disable */


import {User} from "../../../models/user/User";

const functions = require("firebase-functions");
import {UserRepository} from "../../../repository/UserRepository";
import {ElementNotFoundException} from "../../../exceptions/ElementNotFoundException";
import {ValidationException} from "../../../exceptions/ValidationException";


const userRepository:UserRepository = new UserRepository();


export const userRepositoryUpdateUser = functions.region('europe-west1').https.onCall(async (data: User, context: any) => {

    try {

        if (!context.auth)
            throw new ValidationException('You must be authenticated to call this function.');


        const user:User = User.makeInstanceFromSnapshot(data);
        if (context.auth.uid !== user.getUserId())
            throw new ValidationException('You must be authenticated to call this function.');


        await userRepository.updateUserPromise(user);

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

