/* eslint-disable */



import {ElementNotFoundException} from "../../exceptions/ElementNotFoundException";

const {onCall} = require("firebase-functions/v2/https");
const {logger} = require("firebase-functions/v2");
const functions = require("firebase-functions");


import { FetchMatchesData } from "../../models/FetchMatchesData";
import { FetchMatchesService } from "../../servicess/fetchMatchesService";
import {MatchesBatchRepository} from "../../repository/MatchesBatchRepository";
import {ValidationException} from "../../exceptions/ValidationException";
import {User} from "../../models/user/User";
import {Match} from "../../models/Match";


const matchesBatchRepository:MatchesBatchRepository = new MatchesBatchRepository();


export const fetchMatchesPaginatedByRadiusAndDate = onCall(async (request:any) => {

    try {
        //   logger.info("hello");

        if (!request.auth)
            throw new ValidationException('You must be authenticated to call this function.');


        // logger.info("passed Auth");

        const fetchMatchesData: FetchMatchesData = FetchMatchesData.makeInstanceFromSnapshot(request.data.requestMatchesFromServer);
        const user:User = User.makeInstanceFromSnapshot(request.data.user);

        const promises:Promise<Match[]>[] = [];
        promises.push(matchesBatchRepository.getAllMatchesFromBatchesThatAreNotEmpty(fetchMatchesData.getSport()));

        promises.push(matchesBatchRepository.getFromTemporalMatches(fetchMatchesData.getSport()));

        const matchList:Match[] = (await Promise.all(promises)).flat();
        logger.log("matchList",matchList);


        const uniqueMatches: Map<string, Match> = new Map();

        for (const match of matchList) {

            const matchUnique: Match | undefined = uniqueMatches.get(match.getId());
            if (matchUnique === undefined){
                uniqueMatches.set(match.getId(), match);
                continue;
            }

            if (!matchUnique.getInTemporalMatch())
                uniqueMatches.set(match.getId(), match);

        }
        logger.info("uniqueMatches",uniqueMatches);


        let allMatches: Match[] = Array.from(uniqueMatches.values());
        // logger.info("allMatches",allMatches);


        const fetchMatchesService:FetchMatchesService = new FetchMatchesService();

        const paginatedListMatches:Match[] | null = fetchMatchesService.fetchMatchesService(allMatches,user,fetchMatchesData);
        logger.info("paginatedListMatches",paginatedListMatches);

        if (paginatedListMatches === null){
            logger.info("inside main method exports feetchMatches inside if statement",paginatedListMatches);
            return null;
        }


        // const finalUpdatedMatches:Match[] = await ifUpdatesInTemporalHoldingUpdateMatches(paginatedListMatches);
        // logger.info("finalUpdatedMatches",finalUpdatedMatches);

        return paginatedListMatches;

    } catch (error) {

        console.error("error", error);
      if (error instanceof ValidationException)
        throw new functions.https.HttpsError('invalid-argument', error.message);


      if (error instanceof ElementNotFoundException)
        throw new functions.https.HttpsError('not-found', "Error fetching data");

      throw new functions.https.HttpsError('internal', 'An internal error occurred.');
    }

});


// async function ifUpdatesInTemporalHoldingUpdateMatches(paginatedList: Match[]): Promise<any[]> {
//
//   const promises: Promise<Match | undefined>[] = [];
//   paginatedList.forEach((match: Match) => {
//
//       const matchFromTemporalMatchHolder: Promise<Match | undefined> = matchesBatchRepository.getMatchFromTemporalMatchHolder(match.getId(),match.getSport());
//       promises.push(matchFromTemporalMatchHolder);
//   });
//
//   if (promises.length == 0) {
//       // logger.debug("seeIfUpdatesInTemporalHolding", "inside if", "return paginatedList", promises);
//       return paginatedList;
//   }
//
//   let matchesToUpdate: (Match | undefined)[] = await Promise.all(promises);
//   // logger.info("matchesToUpdate", matchesToUpdate);
//
//   return paginatedList.map(baseMatch => {
//       // Find the element in array2 with the same id as item1
//       const resultMatch: any = matchesToUpdate.find(matchesToUpdate => matchesToUpdate !== undefined && matchesToUpdate.getId() === baseMatch.getId());
//
//       // If there is a matching element in array2, return it; otherwise, return item1
//       return resultMatch ? resultMatch : baseMatch;
//   });
// }


