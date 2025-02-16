/* eslint-disable */



import {ValidationException} from "../../../exceptions/ValidationException";

const functions = require("firebase-functions");
import {MatchRepository} from "../../../repository/MatchRepository";
import {ElementNotFoundException} from "../../../exceptions/ElementNotFoundException";
import {Match} from "../../../models/Match";


const matchRepository:MatchRepository = new MatchRepository();


export const matchRepositoryIgnoreRequesterIfAdmin = functions.region('europe-west1').https.onCall(async (data: any, context: any) => {


    try {

        if (!context.auth)
            throw new ValidationException('You must be authenticated to call this function.');


        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(data);
        }catch (Error){
            parsedSnapshot = data;
        }

        const userToIgnore:string = parsedSnapshot.userToIgnore;

        const adminId:string = parsedSnapshot.adminId;
        if (adminId !== context.auth.uid)
            throw new ValidationException('You must be authenticated to call this function.');

        const matchId:string = parsedSnapshot.matchId;
        const sport:string = parsedSnapshot.sport;


        const matchToReturn:Match = await matchRepository.ignoreRequesterIfAdmin(userToIgnore,adminId,matchId,sport);

        return matchToReturn.toObject();

    } catch (error) {

        if (error instanceof ValidationException)
            throw new functions.https.HttpsError('invalid-argument', error.message);

        if (error instanceof ElementNotFoundException)
            throw new functions.https.HttpsError('not-found', "Error fetching data");


        throw new functions.https.HttpsError('internal', 'An internal error occurred.');
    }
});

