/* eslint-disable */



import {ValidationException} from "../../../exceptions/ValidationException";

const functions = require("firebase-functions");
import {Match} from "../../../models/Match";
import {MatchRepository} from "../../../repository/MatchRepository";
import {ElementNotFoundException} from "../../../exceptions/ElementNotFoundException";


const matchRepository:MatchRepository = new MatchRepository();


export const matchRepositorySaveMatch = functions.region('europe-west1').https.onCall(async (data: any, context: any) => {


    try {

        if (!context.auth)
            throw new ValidationException('You must be authenticated to call this function.');


        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(data);
        }catch (Error){
            parsedSnapshot = data;
        }


        const match:Match = Match.makeInstanceFromSnapshot(parsedSnapshot.match);
        if (!match.isAdmin(context.auth.uid))
            throw new ValidationException('You must be admin to create a match.');

        if (!match.isMember(context.auth.uid))
            throw new ValidationException('You must be a member.');

        if (match.getUsersInChat().length !== 1)
            throw new ValidationException('You must be the only 1 in the match.');

        await matchRepository.saveMatch(match,context.auth.uid);

        return true;

    } catch (error) {

        console.error("error", error);
        if (error instanceof ValidationException){
            console.error("if 1");
            throw new functions.https.HttpsError('invalid-argument', error.message);
        }

        if (error instanceof ElementNotFoundException){
            console.error("if 2");
            throw new functions.https.HttpsError('not-found', "Error fetching data");
        }

        console.error("An error occurred:", error);
        throw new functions.https.HttpsError('internal', 'An internal error occurred.');
    }
});

